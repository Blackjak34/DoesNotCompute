package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.block.BlockConsole;
import com.github.blackjak34.compute.enums.CharacterComputer;
import com.github.blackjak34.compute.enums.InstructionComputer;
import com.github.blackjak34.compute.item.ItemFloppy;
import com.github.blackjak34.compute.packet.MessageUpdateCursor;
import com.github.blackjak34.compute.packet.MessageUpdateDisplay;
import com.google.common.io.Files;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.github.blackjak34.compute.enums.GuiConstantComputer.*;

public class TileEntityEmulator extends TileEntity implements IUpdatePlayerListBox {

    private int cursorX = 0;
    private int cursorY = 0;
    private int registerA = 0;
    private int registerX = 0;
    private int registerY = 0;
    private int registerI = 0;
    private int registerD = 0;
    private int pStackPointer = 255;
    private int rStackPointer = 255;
    private int programCounter = 512;
    private int cyclesElapsed = 0;

    private boolean running = false;
    private boolean floppyInDrive = false;
    private boolean flagCarry = false;
    private boolean flagZero = false;
    private boolean flagInterrupt = false;
    private boolean flagDecimal = false;
    private boolean flagBreak = false;
    private boolean flagAccumulator = false;
    private boolean flagOverflow = false;
    private boolean flagSign = false;
    private boolean flagEmulate = false;

    private byte[] keyBuffer = new byte[16];
    private byte[] memory = new byte[32768];
    private byte[] floppyData = new byte[32768];

    private String floppyFilename;

    public TileEntityEmulator(World world) {
        Arrays.fill(memory, (byte) 255);
        Arrays.fill(floppyData, (byte) 255);
        copyFileIntoArray(world, "boot", memory, 512, 28250);
    }

    @SuppressWarnings("unused")
    public TileEntityEmulator() {}

    public void onKeyTyped(char keyTyped) {
        if(CharacterComputer.getCharacter(keyTyped) == CharacterComputer.INVALID) {return;}

        int keyBufferPos = readMemory(28764);
        keyBuffer[keyBufferPos] = (byte) keyTyped;

        writeMemory(28764, keyBufferPos+1);
    }

    public void onActionPerformed(int buttonId) {
        if(buttonId == BUTTON_STP.getValue()) {
            setRunning(false);
        } else if(buttonId == BUTTON_START.getValue()) {
            setRunning(true);
        } else if(buttonId == BUTTON_RST.getValue()) {
            setRunning(false);
            setProgramCounter(512);
        } else if(buttonId == BUTTON_EJECT.getValue()) {
            ejectFloppy();
        }
    }

    public boolean insertFloppy(String filename) {
        if(floppyInDrive) {return false;}

        floppyFilename = filename;
        setFloppyInDrive(true);

        copyFileIntoArray(worldObj, filename, floppyData, 0, floppyData.length);
        worldObj.markBlockForUpdate(pos);

        return true;
    }

    public void ejectFloppy() {
        if(!floppyInDrive) {return;}

        copyArrayIntoFile(worldObj, floppyFilename, floppyData);
        Arrays.fill(floppyData, (byte) 255);

        worldObj.spawnEntityInWorld(new EntityItem(worldObj, pos.getX(), pos.getY(), pos.getZ(),
                ItemFloppy.setFloppyFilename(new ItemStack(DoesNotCompute.floppy), floppyFilename)));

        floppyFilename = null;
        setFloppyInDrive(false);

        worldObj.markBlockForUpdate(pos);
    }

    private boolean copyFileIntoArray(World world, String filename, byte[] dest, int index, int maxLength) {
        File modDirectory = new File(world.getSaveHandler().getWorldDirectory(), "/doesnotcompute/");
        File dataFile = new File(modDirectory, filename);

        if(!modDirectory.exists() && !modDirectory.mkdir()) {
            System.err.println("Could not generate the mod directory. Is the world folder read only?");
            return false;
        }

        if(!dataFile.exists()) {
            System.err.println("The requested file at " + dataFile.getAbsolutePath() + " does not exist.");
            return false;
        }

        byte[] data;
        try {
            data = Files.toByteArray(dataFile);
        } catch(IOException e) {
            System.err.println("There was an error reading data from the file at" + dataFile.getAbsolutePath() + ":");
            e.printStackTrace();
            return false;
        }

        if(data.length < maxLength) {maxLength = data.length;}

        System.arraycopy(data, 0, dest, index, maxLength);
        return true;
    }

    private boolean copyArrayIntoFile(World world, String filename, byte[] src) {
        File modDirectory = new File(world.getSaveHandler().getWorldDirectory(), "/doesnotcompute/");
        File dataFile = new File(modDirectory, filename);

        if(!modDirectory.exists() && !modDirectory.mkdir()) {
            System.err.println("Could not generate the mod directory. Is the world folder read only?");
            return false;
        }

        try {
            if(dataFile.createNewFile()) {
                System.out.println("Generated a new data file at" + dataFile.getAbsolutePath() + ".");
            }
        } catch(IOException e) {
            System.err.println("Could not generate a new data file at " + dataFile.getAbsolutePath() +
                    ". Is the world folder read only?");
            return false;
        }

        try {
            Files.write(src, dataFile);
        } catch(IOException e) {
            System.err.println("There was an error writing data to the file at" + dataFile.getAbsolutePath() + ":");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean isEightBit(boolean indexRegister) {
        return flagEmulate || (!indexRegister && flagAccumulator) || (indexRegister && flagBreak);
    }

    private int readDynamic(int index, boolean isIndexRegister) {
        if(isEightBit(isIndexRegister)) {
            return readMemory(index);
        } else {
            return (readMemory(index) << 8) | readMemory(index+1);
        }
    }

    private void writeDynamic(int index, int value, boolean isIndexRegister) {
        if(isEightBit(isIndexRegister)) {
            writeMemory(index, value);
        } else {
            writeMemory(index, (value >>> 8));
            writeMemory(index+1, value);
        }
    }

    private int readMemory(int index) {
        if(index < 0 || index > 65535) {return 255;}

        if(index < 32768) {
            switch(index) {
                case 28763:case 28764:
                    return memory[index] & 15;
                case 28765:
                    int keyBufferStart = readMemory(28763);
                    return keyBuffer[keyBufferStart] & 255;
                default:
                    return memory[index] & 255;
            }
        } else {
            return floppyData[index-32768];
        }
    }

    private void writeMemory(int index, int value) {
        if(index < 0 || index > 65535) {return;}

        if(index < 32768) {
            memory[index] = (byte) value;
            if(index > 28767) {
                DoesNotCompute.networkWrapper.sendToDimension(
                        new MessageUpdateDisplay(index-28768, (byte) value), worldObj.provider.getDimensionId());
            } else if(index > 28765) {
                DoesNotCompute.networkWrapper.sendToDimension(
                        new MessageUpdateCursor(readMemory(28766), readMemory(28767)), worldObj.provider.getDimensionId());
            }
        } else if(floppyInDrive) {
            floppyData[index-32768] = (byte) value;
        }
    }

    private void setProgramCounter(int value) {
        programCounter = value & 65535;
    }

    private void setRunning(boolean value) {
        running = value;

        IBlockState state = worldObj.getBlockState(pos);
        if(state.getBlock() != DoesNotCompute.console) {return;}

        worldObj.setBlockState(pos, state.withProperty(BlockConsole.RUNNING, value), 2);
    }

    private void setFloppyInDrive(boolean value) {
        floppyInDrive = value;

        IBlockState state = worldObj.getBlockState(pos);
        if(state.getBlock() != DoesNotCompute.console) {return;}

        worldObj.setBlockState(pos, state.withProperty(BlockConsole.DISK, value), 2);
    }

    public void update() {
        if(!running) {return;}
        markDirty();

        cyclesElapsed = 0;
        while(cyclesElapsed < 3500) {executeInstruction();}
    }

    public void executeInstruction() {
        InstructionComputer instruction = InstructionComputer.getInstruction(readMemory(programCounter));
        switch(instruction) {
            case ADC_ABS:
                performAddition(getAbsAddress(programCounter+1), false);
                break;
            case ADC_ABS_X:
                performAddition(getAbsAddress(programCounter+1)+registerX, false);
                break;
            case ADC_ABS_Y:
                performAddition(getAbsAddress(programCounter+1)+registerY, false);
                break;
            case ADC_IMM:
                performAddition(programCounter+1, false);
                break;
            case ADC_IND_X:
                performAddition(getAbsAddress(readMemory(programCounter+1)+registerX & 255), false);
                break;
            case ADC_IND_Y:
                performAddition(getAbsAddress(readMemory(programCounter+1))+registerY, false);
                break;
            case ADC_ZP:
                performAddition(readMemory(programCounter+1), false);
                break;
            case ADC_ZP_X:
                performAddition(readMemory(programCounter+1)+registerX & 255, false);
                break;
            case AND_ABS:
                registerA &= readDynamic(getAbsAddress(programCounter+1), false);
                setSignZeroDynamic(registerA, false);
                break;
            case AND_ABS_X:
                registerA &= readDynamic(getAbsAddress(programCounter+1)+registerX, false);
                setSignZeroDynamic(registerA, false);
                break;
            case AND_ABS_Y:
                registerA &= readDynamic(getAbsAddress(programCounter+1)+registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case AND_IMM:
                registerA &= readDynamic(programCounter + 1, false);
                setSignZeroDynamic(registerA, false);
                break;
            case AND_IND_X:
                registerA &= readDynamic(getAbsAddress(readMemory(programCounter+1)+registerX & 255), false);
                setSignZeroDynamic(registerA, false);
                break;
            case AND_IND_Y:
                registerA &= readDynamic(getAbsAddress(readMemory(programCounter+1))+registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case AND_ZP:
                registerA &= readDynamic(readMemory(programCounter+1), false);
                setSignZeroDynamic(registerA, false);
                break;
            case AND_ZP_X:
                registerA &= readDynamic(readMemory(programCounter+1)+registerX & 255, false);
                setSignZeroDynamic(registerA, false);
                break;
            case ASL_A:
                if(isEightBit(false)) {
                    int lowByte = registerA&255;
                    flagCarry = lowByte > 127;
                    registerA = (registerA&65280) | ((lowByte << 1)&255);
                } else {
                    flagCarry = registerA > 32767;
                    registerA <<= 1;
                    registerA &= 65535;
                }

                setSignZeroDynamic(registerA, false);
                break;
            case ASL_ABS:
                int aslAbsAddress = getAbsAddress(programCounter+1);
                int aslAbsValue = readMemory(aslAbsAddress);
                flagCarry = aslAbsValue>127;

                aslAbsValue <<= 1;
                aslAbsValue &= 255;
                setSignZero(aslAbsValue);

                writeMemory(aslAbsAddress, aslAbsValue);
                break;
            case ASL_ABS_X:
                int aslAbsXAddress = getAbsAddress(programCounter+1)+registerX;
                int aslAbsXValue = readMemory(aslAbsXAddress);
                flagCarry = aslAbsXValue>127;

                aslAbsXValue <<= 1;
                aslAbsXValue &= 255;
                setSignZero(aslAbsXValue);

                writeMemory(aslAbsXAddress, aslAbsXValue);
                break;
            case ASL_ZP:
                int aslZpAddress = readMemory(programCounter+1);
                int aslZpValue = readMemory(aslZpAddress);
                flagCarry = aslZpValue>127;

                aslZpValue <<= 1;
                aslZpValue &= 255;
                setSignZero(aslZpValue);

                writeMemory(aslZpAddress, aslZpValue);
                break;
            case ASL_ZP_X:
                int aslZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int aslZpXValue = readMemory(aslZpXAddress);
                flagCarry = aslZpXValue>127;

                aslZpXValue <<= 1;
                aslZpXValue &= 255;
                setSignZero(aslZpXValue);

                writeMemory(aslZpXAddress, aslZpXValue);
                break;
            case BCC_REL:
                if(!flagCarry) {
                    byte relValue = (byte) readMemory(programCounter+1);
                    setProgramCounter(programCounter + relValue);
                    cyclesElapsed++;
                }
                break;
            case BCS_REL:
                if(flagCarry) {
                    byte relValue = (byte) readMemory(programCounter+1);
                    setProgramCounter(programCounter + relValue);
                    cyclesElapsed++;
                }
                break;
            case BEQ_REL:
                if(flagZero) {
                    byte relValue = (byte) readMemory(programCounter+1);
                    setProgramCounter(programCounter + relValue);
                    cyclesElapsed++;
                }
                break;
            case BIT_ABS:
                int bitAbsValue = readDynamic(getAbsAddress(programCounter + 1), false);

                flagZero = (registerA & bitAbsValue) == 0;
                if(isEightBit(false)) {
                    flagSign = bitAbsValue > 127;
                    flagOverflow = testBit(bitAbsValue, 6);
                } else {
                    flagSign = bitAbsValue > 32767;
                    flagOverflow = testBit(bitAbsValue, 14);
                }
                break;
            case BIT_ZP:
                int bitZpValue = readDynamic(readMemory(programCounter + 1), false);

                flagZero = (registerA & bitZpValue) == 0;
                if(isEightBit(false)) {
                    flagSign = bitZpValue > 127;
                    flagOverflow = testBit(bitZpValue, 6);
                } else {
                    flagSign = bitZpValue > 32767;
                    flagOverflow = testBit(bitZpValue, 14);
                }
                break;
            case BMI_REL:
                if(flagSign) {
                    byte relValue = (byte) readMemory(programCounter+1);
                    setProgramCounter(programCounter + relValue);
                    cyclesElapsed++;
                }
                break;
            case BNE_REL:
                if(!flagZero) {
                    byte relValue = (byte) readMemory(programCounter+1);
                    setProgramCounter(programCounter + relValue);
                    cyclesElapsed++;
                }
                break;
            case BPL_REL:
                if(!flagSign) {
                    byte relValue = (byte) readMemory(programCounter+1);
                    setProgramCounter(programCounter + relValue);
                    cyclesElapsed++;
                }
                break;
            case BRK:
                pushRStack(programCounter >>> 8);
                pushRStack(programCounter);

                int pushFlags3 = flagCarry ? 1 : 0;
                pushFlags3 += flagZero ? 2 : 0;
                pushFlags3 += flagInterrupt ? 4 : 0;
                pushFlags3 += flagDecimal ? 8 : 0;
                pushFlags3 += flagBreak ? 16 : 0;
                pushFlags3 += 32;
                pushFlags3 += flagOverflow ? 64 : 0;
                pushFlags3 += flagSign ? 128 : 0;
                pushPStack(pushFlags3);

                flagDecimal = false;
                flagBreak = true;
                flagInterrupt = true;
                break;
            case BVC_REL:
                if(!flagOverflow) {
                    byte relValue = (byte) readMemory(programCounter+1);
                    setProgramCounter(programCounter + relValue);
                    cyclesElapsed++;
                }
                break;
            case BVS_REL:
                if(flagOverflow) {
                    byte relValue = (byte) readMemory(programCounter+1);
                    setProgramCounter(programCounter + relValue);
                    cyclesElapsed++;
                }
                break;
            case CLC:
                flagCarry = false;
                break;
            case CLD:
                flagDecimal = false;
                break;
            case CLI:
                flagInterrupt = false;
                break;
            case CLV:
                flagOverflow = false;
                break;
            case CMP_ABS:
                performComparation(getAbsAddress(programCounter+1), registerA, false);
                break;
            case CMP_ABS_X:
                performComparation(getAbsAddress(programCounter+1)+registerX, registerA, false);
                break;
            case CMP_ABS_Y:
                performComparation(getAbsAddress(programCounter+1)+registerY, registerA, false);
                break;
            case CMP_IMM:
                performComparation(programCounter+1, registerA, false);
                break;
            case CMP_IND_X:
                performComparation(getAbsAddress(readMemory(programCounter+1)+registerX & 255), registerA, false);
                break;
            case CMP_IND_Y:
                performComparation(getAbsAddress(readMemory(programCounter+1))+registerY, registerA, false);
                break;
            case CMP_ZP:
                performComparation(readMemory(programCounter+1), registerA, false);
                break;
            case CMP_ZP_X:
                performComparation(readMemory(programCounter+1)+registerX & 255, registerA, false);
                break;
            case CPX_ABS:
                performComparation(getAbsAddress(programCounter+1), registerX, true);
                break;
            case CPX_IMM:
                performComparation(programCounter+1, registerX, true);
                break;
            case CPX_ZP:
                performComparation(readMemory(programCounter+1), registerX, true);
                break;
            case CPY_ABS:
                performComparation(getAbsAddress(programCounter+1), registerY, true);
                break;
            case CPY_IMM:
                performComparation(programCounter+1, registerY, true);
                break;
            case CPY_ZP:
                performComparation(readMemory(programCounter+1), registerY, true);
                break;
            case DEC_ABS:
                int decAbsAddress = getAbsAddress(programCounter+1);
                int decAbsValue = readMemory(decAbsAddress);

                decAbsValue--;
                setSignZero(decAbsValue);

                writeMemory(decAbsAddress, decAbsValue);
                break;
            case DEC_ABS_X:
                int decAbsXAddress = getAbsAddress(programCounter+1)+registerX;
                int decAbsXValue = readMemory(decAbsXAddress);

                decAbsXValue--;
                setSignZero(decAbsXValue);

                writeMemory(decAbsXAddress, decAbsXValue);
                break;
            case DEC_ZP:
                int decZpAddress = readMemory(programCounter+1);
                int decZpValue = readMemory(decZpAddress);

                decZpValue--;
                setSignZero(decZpValue);

                writeMemory(decZpAddress, decZpValue);
                break;
            case DEC_ZP_X:
                int decZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int decZpXValue = readMemory(decZpXAddress);

                decZpXValue--;
                setSignZero(decZpXValue);

                writeMemory(decZpXAddress, decZpXValue);
                break;
            case DEX:
                registerX--;
                registerX &= (isEightBit(true) ? 255 : 65535);
                setSignZeroDynamic(registerX, true);
                break;
            case DEY:
                registerY--;
                registerY &= (isEightBit(true) ? 255 : 65535);
                setSignZeroDynamic(registerY, true);
                break;
            case EOR_ABS:
                registerA ^= readDynamic(getAbsAddress(programCounter + 1), false);
                setSignZeroDynamic(registerA, false);
                break;
            case EOR_ABS_X:
                registerA ^= readDynamic(getAbsAddress(programCounter + 1) + registerX, false);
                setSignZeroDynamic(registerA, false);
                break;
            case EOR_ABS_Y:
                registerA ^= readDynamic(getAbsAddress(programCounter + 1) + registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case EOR_IMM:
                registerA ^= readDynamic(programCounter + 1, false);
                setSignZeroDynamic(registerA, false);
                break;
            case EOR_IND_X:
                registerA ^= readDynamic(getAbsAddress(readMemory(programCounter + 1) + registerX & 255), false);
                setSignZeroDynamic(registerA, false);
                break;
            case EOR_IND_Y:
                registerA ^= readDynamic(getAbsAddress(readMemory(programCounter + 1)) + registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case EOR_ZP:
                registerA ^= readDynamic(readMemory(programCounter + 1), false);
                setSignZeroDynamic(registerA, false);
                break;
            case EOR_ZP_X:
                registerA ^= readDynamic(readMemory(programCounter + 1) + registerX & 255, false);
                setSignZeroDynamic(registerA, false);
                break;
            case INC_ABS:
                int incAbsAddress = getAbsAddress(programCounter+1);
                int incAbsValue = readMemory(incAbsAddress);

                setSignZero(++incAbsValue);

                writeMemory(incAbsAddress, incAbsValue);
                break;
            case INC_ABS_X:
                int incAbsXAddress = getAbsAddress(programCounter+1)+registerX;
                int incAbsXValue = readMemory(incAbsXAddress);

                incAbsXValue++;
                setSignZero(incAbsXValue);

                writeMemory(incAbsXAddress, incAbsXValue);
                break;
            case INC_ZP:
                int incZpAddress = readMemory(programCounter+1);
                int incZpValue = readMemory(incZpAddress);

                incZpValue++;
                setSignZero(incZpValue);

                writeMemory(incZpAddress, incZpValue);
                break;
            case INC_ZP_X:
                int incZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int incZpXValue = readMemory(incZpXAddress);

                incZpXValue++;
                setSignZero(incZpXValue);

                writeMemory(incZpXAddress, incZpXValue);
                break;
            case INX:
                registerX++;
                registerX &= (isEightBit(true) ? 255 : 65535);
                setSignZeroDynamic(registerX, true);
                break;
            case INY:
                registerY++;
                registerY &= (isEightBit(true) ? 255 : 65535);
                setSignZeroDynamic(registerY, true);
                break;
            case JMP_ABS:
                setProgramCounter(getAbsAddress(programCounter+1));
                break;
            case JMP_IND:
                setProgramCounter(getAbsAddress(getAbsAddress(programCounter+1)));
                break;
            case JSR_ABS:
                int addressToPush = programCounter + 2;
                pushRStack(addressToPush >>> 8);
                pushRStack(addressToPush);

                setProgramCounter(getAbsAddress(programCounter+1));
                break;
            case LDA_ABS:
                registerA = readDynamic(getAbsAddress(programCounter + 1), false);
                setSignZeroDynamic(registerA, false);
                break;
            case LDA_ABS_X:
                registerA = readDynamic(getAbsAddress(programCounter + 1) + registerX, false);
                setSignZeroDynamic(registerA, false);
                break;
            case LDA_ABS_Y:
                registerA = readDynamic(getAbsAddress(programCounter + 1) + registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case LDA_IMM:
                registerA = readDynamic(programCounter + 1, false);
                setSignZeroDynamic(registerA, false);
                break;
            case LDA_IND_X:
                registerA = readDynamic(getAbsAddress(readMemory(programCounter + 1) + registerX & 255), false);
                setSignZeroDynamic(registerA, false);
                break;
            case LDA_IND_Y:
                registerA = readDynamic(getAbsAddress(readMemory(programCounter + 1)) + registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case LDA_ZP:
                registerA = readDynamic(readMemory(programCounter + 1), false);
                setSignZeroDynamic(registerA, false);
                break;
            case LDA_ZP_X:
                registerA = readDynamic(readMemory(programCounter + 1) + registerX & 255, false);
                setSignZeroDynamic(registerA, false);
                break;
            case LDX_ABS:
                registerX = readDynamic(getAbsAddress(programCounter + 1), true);
                setSignZeroDynamic(registerX, true);
                break;
            case LDX_ABS_Y:
                registerX = readDynamic(getAbsAddress(programCounter + 1) + registerY, true);
                setSignZeroDynamic(registerX, true);
                break;
            case LDX_IMM:
                registerX = readDynamic(programCounter + 1, true);
                setSignZeroDynamic(registerX, true);
                break;
            case LDX_ZP:
                registerX = readDynamic(readMemory(programCounter + 1), true);
                setSignZeroDynamic(registerX, true);
                break;
            case LDX_ZP_Y:
                registerX = readDynamic(readMemory(programCounter + 1) + registerY & 255, true);
                setSignZeroDynamic(registerX, true);
                break;
            case LDY_ABS:
                registerY = readDynamic(getAbsAddress(programCounter + 1), true);
                setSignZeroDynamic(registerY, true);
                break;
            case LDY_ABS_X:
                registerY = readDynamic(getAbsAddress(programCounter + 1) + registerX, true);
                setSignZeroDynamic(registerY, true);
                break;
            case LDY_IMM:
                registerY = readDynamic(programCounter + 1, true);
                setSignZeroDynamic(registerY, true);
                break;
            case LDY_ZP:
                registerY = readDynamic(readMemory(programCounter + 1), true);
                setSignZeroDynamic(registerY, true);
                break;
            case LDY_ZP_X:
                registerY = readDynamic(readMemory(programCounter + 1) + registerX & 255, true);
                setSignZeroDynamic(registerY, true);
                break;
            case LSR_A:
                flagCarry = testBit(registerA, 0);
                if(isEightBit(false)) {
                    registerA = (registerA&65280) | (registerA&255 >>> 1);
                } else {
                    registerA >>>= 1;
                }

                setSignZeroDynamic(registerA, false);
                break;
            case LSR_ABS:
                int lsrAbsAddress = getAbsAddress(programCounter+1);
                int lsrAbsValue = readMemory(lsrAbsAddress);

                flagCarry = testBit(lsrAbsValue, 0);

                lsrAbsValue >>>= 1;
                setSignZero(lsrAbsValue);

                writeMemory(lsrAbsAddress, lsrAbsValue);
                break;
            case LSR_ABS_X:
                int lsrAbsXAddress = getAbsAddress(programCounter+1)+registerX;
                int lsrAbsXValue = readMemory(lsrAbsXAddress);

                flagCarry = testBit(lsrAbsXValue, 0);

                lsrAbsXValue >>>= 1;
                setSignZero(lsrAbsXValue);

                writeMemory(lsrAbsXAddress, lsrAbsXValue);
                break;
            case LSR_ZP:
                int lsrZpAddress = readMemory(programCounter+1);
                int lsrZpValue = readMemory(lsrZpAddress);

                lsrZpValue >>>= 1;
                setSignZero(lsrZpValue);

                writeMemory(lsrZpAddress, lsrZpValue);
                break;
            case LSR_ZP_X:
                int lsrZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int lsrZpXValue = readMemory(lsrZpXAddress);

                lsrZpXValue >>>= 1;
                setSignZero(lsrZpXValue);

                writeMemory(lsrZpXAddress, lsrZpXValue);
                break;
            case NOP:
                break;
            case ORA_ABS:
                registerA |= readDynamic(getAbsAddress(programCounter+1), false);
                setSignZeroDynamic(registerA, false);
                break;
            case ORA_ABS_X:
                registerA |= readDynamic(getAbsAddress(programCounter+1)+registerX, false);
                setSignZeroDynamic(registerA, false);
                break;
            case ORA_ABS_Y:
                registerA |= readDynamic(getAbsAddress(programCounter+1)+registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case ORA_IMM:
                registerA |= readDynamic(programCounter + 1, false);
                setSignZeroDynamic(registerA, false);
                break;
            case ORA_IND_X:
                registerA |= readDynamic(getAbsAddress(readMemory(programCounter+1)+registerX & 255), false);
                setSignZeroDynamic(registerA, false);
                break;
            case ORA_IND_Y:
                registerA |= readDynamic(getAbsAddress(readMemory(programCounter+1))+registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case ORA_ZP:
                registerA |= readDynamic(readMemory(programCounter+1), false);
                setSignZeroDynamic(registerA, false);
                break;
            case ORA_ZP_X:
                registerA |= readDynamic(readMemory(programCounter+1)+registerX & 255, false);
                setSignZeroDynamic(registerA, false);
                break;
            case PHA:
                pushPStack(registerA);
                if(!isEightBit(false)) {pushPStack(registerA >>> 8);}
                break;
            case PHP:
                int pushFlags = flagCarry ? 1 : 0;
                pushFlags += flagZero ? 2 : 0;
                pushFlags += flagInterrupt ? 4 : 0;
                pushFlags += flagDecimal ? 8 : 0;
                pushFlags += flagBreak ? 16 : 0;
                pushFlags += flagEmulate||flagAccumulator ? 32 : 0;
                pushFlags += flagOverflow ? 64 : 0;
                pushFlags += flagSign ? 128 : 0;

                pushPStack(pushFlags);
                break;
            case PLA:
                registerA = pullPStack();
                if(!isEightBit(false)) {registerA |= (pullPStack() << 8);}
                break;
            case PLP:
                int pullFlags = pullPStack();

                flagCarry = testBit(pullFlags, 0);
                flagZero = testBit(pullFlags, 1);
                flagInterrupt = testBit(pullFlags, 2);
                flagDecimal = testBit(pullFlags, 3);
                flagBreak = testBit(pullFlags, 4);
                flagAccumulator = testBit(pullFlags, 5);
                flagOverflow = testBit(pullFlags, 6);
                flagSign = testBit(pullFlags, 7);
                break;
            case ROL_A:
                boolean rolATemp;
                if(isEightBit(false)) {
                    int lowByte = registerA&255;
                    rolATemp = lowByte > 127;
                    registerA = (registerA&65280) | ((lowByte << 1)&255);
                } else {
                    rolATemp = registerA > 32767;
                    registerA <<= 1;
                    registerA &= 65535;
                }
                registerA += (flagCarry ? 1 : 0);
                flagCarry = rolATemp;

                setSignZeroDynamic(registerA, false);
                break;
            case ROL_ABS:
                int rolAbsAddress = getAbsAddress(programCounter+1);
                int rolAbsValue = readMemory(rolAbsAddress);

                boolean rolAbsTemp = rolAbsValue>127;
                rolAbsValue <<= 1;
                rolAbsValue &= 255;
                rolAbsValue += flagCarry ? 1 : 0;
                flagCarry = rolAbsTemp;

                setSignZero(rolAbsValue);
                writeMemory(rolAbsAddress, rolAbsValue);
                break;
            case ROL_ABS_X:
                int rolAbsXAddress = getAbsAddress(programCounter+1)+registerX;
                int rolAbsXValue = readMemory(rolAbsXAddress);

                boolean rolAbsXTemp = rolAbsXValue>127;
                rolAbsXValue <<= 1;
                rolAbsXValue &= 255;
                rolAbsXValue += flagCarry ? 1 : 0;
                flagCarry = rolAbsXTemp;

                setSignZero(rolAbsXValue);
                writeMemory(rolAbsXAddress, rolAbsXValue);
                break;
            case ROL_ZP:
                int rolZpAddress = readMemory(readMemory(programCounter+1));
                int rolZpValue = readMemory(rolZpAddress);

                boolean rolZpTemp = rolZpValue>127;
                rolZpValue <<= 1;
                rolZpValue &= 255;
                rolZpValue += flagCarry ? 1 : 0;
                flagCarry = rolZpTemp;

                setSignZero(rolZpValue);
                writeMemory(rolZpAddress, rolZpValue);
                break;
            case ROL_ZP_X:
                int rolZpXAddress = readMemory(readMemory(programCounter+1)+registerX & 255);
                int rolZpXValue = readMemory(rolZpXAddress);

                boolean rolZpXTemp = rolZpXValue>127;
                rolZpXValue <<= 1;
                rolZpXValue &= 255;
                rolZpXValue += flagCarry ? 1 : 0;
                flagCarry = rolZpXTemp;

                setSignZero(rolZpXValue);
                writeMemory(rolZpXAddress, rolZpXValue);
                break;
            case ROR_A:
                boolean rorATemp = testBit(registerA, 0);
                if(isEightBit(false)) {
                    registerA = (registerA&65280) | (registerA&255 >>> 1);
                    registerA += (flagCarry ? 128 : 0);
                } else {
                    registerA >>>= 1;
                    registerA += (flagCarry ? 32768 : 0);
                }
                flagCarry = rorATemp;

                setSignZeroDynamic(registerA, false);
                break;
            case ROR_ABS:
                int rorAbsAddress = getAbsAddress(programCounter+1);
                int rorAbsValue = readMemory(rorAbsAddress);

                boolean rorAbsTemp = testBit(rorAbsValue, 0);
                rorAbsValue >>>= 1;
                rorAbsValue += flagCarry ? 128 : 0;
                flagCarry = rorAbsTemp;

                setSignZero(rorAbsValue);
                writeMemory(rorAbsAddress, rorAbsValue);
                break;
            case ROR_ABS_X:
                int rorAbsXAddress = getAbsAddress(programCounter+1)+registerX;
                int rorAbsXValue = readMemory(rorAbsXAddress);

                boolean rorAbsXTemp = testBit(rorAbsXValue, 0);
                rorAbsXValue >>>= 1;
                rorAbsXValue += flagCarry ? 128 : 0;
                flagCarry = rorAbsXTemp;

                setSignZero(rorAbsXValue);
                writeMemory(rorAbsXAddress, rorAbsXValue);
                break;
            case ROR_ZP:
                int rorZpAddress = readMemory(programCounter+1);
                int rorZpValue = readMemory(rorZpAddress);

                boolean rorZpTemp = testBit(rorZpValue, 0);
                rorZpValue >>>= 1;
                rorZpValue += flagCarry ? 128 : 0;
                flagCarry = rorZpTemp;

                setSignZero(rorZpValue);
                writeMemory(rorZpAddress, rorZpValue);
                break;
            case ROR_ZP_X:
                int rorZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int rorZpXValue = readMemory(rorZpXAddress);

                boolean rorZpXTemp = testBit(rorZpXValue, 0);
                rorZpXValue >>>= 1;
                rorZpXValue += flagCarry ? 128 : 0;
                flagCarry = rorZpXTemp;

                setSignZero(rorZpXValue);
                writeMemory(rorZpXAddress, rorZpXValue);
                break;
            case RTI:
                int pullFlags2 = pullPStack();

                flagCarry = testBit(pullFlags2, 0);
                flagZero = testBit(pullFlags2, 1);
                flagInterrupt = testBit(pullFlags2, 2);
                flagDecimal = testBit(pullFlags2, 3);
                flagBreak = testBit(pullFlags2, 4);
                flagAccumulator = testBit(pullFlags2, 5);
                flagOverflow = testBit(pullFlags2, 6);
                flagSign = testBit(pullFlags2, 7);

                setProgramCounter(pullImmAddress());
                break;
            case RTS:
                setProgramCounter(pullImmAddress());
                break;
            case SBC_ABS:
                performAddition(getAbsAddress(programCounter+1), true);
                break;
            case SBC_ABS_X:
                performAddition(getAbsAddress(programCounter+1)+registerX, true);
                break;
            case SBC_ABS_Y:
                performAddition(getAbsAddress(programCounter+1)+registerY, true);
                break;
            case SBC_IMM:
                performAddition(programCounter+1, true);
                break;
            case SBC_IND_X:
                performAddition(getAbsAddress(readMemory(programCounter+1)+registerX & 255), true);
                break;
            case SBC_IND_Y:
                performAddition(getAbsAddress(readMemory(programCounter+1))+registerY, true);
                break;
            case SBC_ZP:
                performAddition(readMemory(programCounter+1), true);
                break;
            case SBC_ZP_X:
                performAddition(readMemory(programCounter+1)+registerX & 255, true);
                break;
            case SEC:
                flagCarry = true;
                break;
            case SED:
                flagDecimal = true;
                break;
            case SEI:
                flagInterrupt = true;
                break;
            case STA_ABS:
                writeDynamic(getAbsAddress(programCounter + 1), registerA, false);
                break;
            case STA_ABS_X:
                writeDynamic(getAbsAddress(programCounter + 1) + registerX, registerA, false);
                break;
            case STA_ABS_Y:
                writeDynamic(getAbsAddress(programCounter + 1) + registerY, registerA, false);
                break;
            case STA_IND_X:
                writeDynamic(getAbsAddress(readMemory(programCounter + 1) + registerX & 255), registerA, false);
                break;
            case STA_IND_Y:
                writeDynamic(getAbsAddress(readMemory(programCounter + 1)) + registerY, registerA, false);
                break;
            case STA_ZP:
                writeDynamic(readMemory(programCounter + 1), registerA, false);
                break;
            case STA_ZP_X:
                writeDynamic(readMemory(programCounter + 1) + registerX & 255, registerA, false);
                break;
            case STP:
                setRunning(false);

                BlockPos topSide = pos.add(0, 1, 0);
                if (worldObj.getBlockState(topSide).getBlock().getMaterial() == Material.air &&
                        Blocks.fire.canPlaceBlockAt(worldObj, topSide)) {
                    worldObj.setBlockState(topSide, Blocks.fire.getDefaultState());
                }
                break;
            case STX_ABS:
                writeDynamic(getAbsAddress(programCounter + 1), registerX, true);
                break;
            case STX_ZP:
                writeDynamic(readMemory(programCounter + 1), registerX, true);
                break;
            case STX_ZP_Y:
                writeDynamic(getAbsAddress(readMemory(programCounter+1))+registerY, registerX, true);
                break;
            case STY_ABS:
                writeDynamic(getAbsAddress(programCounter+1), registerY, true);
                break;
            case STY_ZP:
                writeDynamic(readMemory(programCounter+1), registerY, true);
                break;
            case STY_ZP_X:
                writeDynamic(readMemory(programCounter+1)+registerX & 255, registerY, true);
                break;
            case TAX:
                registerX = registerA;
                if(isEightBit(true)) {registerX &= 255;}

                setSignZeroDynamic(registerX, true);
                break;
            case TAY:
                registerY = registerA;
                if(isEightBit(true)) {registerY &= 255;}

                setSignZeroDynamic(registerY, true);
                break;
            case TSX:
                registerX = pStackPointer;
                setSignZeroDynamic(registerX, true);
                break;
            case TXA:
                if(isEightBit(false)) {
                    registerA = (registerA&65280) | (registerX&255);
                } else {
                    registerA = registerX;
                }

                setSignZeroDynamic(registerA, false);
                break;
            case TXS:
                pStackPointer = registerX;
                pStackPointer &= 255;
                setSignZero(pStackPointer);
                break;
            case TYA:
                if(isEightBit(false)) {
                    registerA = (registerA&65280) | (registerY&255);
                } else {
                    registerA = registerY;
                }

                setSignZeroDynamic(registerA, false);
                break;
            case RHI:
                pushRStack(registerI);
                pushRStack(registerI >>> 8);
                break;
            case ORA_IND:
                registerA |= readDynamic(getAbsAddress(readMemory(programCounter+1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case INC_A:
                if(isEightBit(false)) {
                    registerA = (registerA&65280) | (++registerA&255);
                } else {
                    registerA++;
                    registerA &= 65535;
                }
                setSignZeroDynamic(registerA, false);
                break;
            case RHX:
                pushRStack(registerX);
                if(!isEightBit(true)) {pushRStack(registerX >>> 8);}
                break;
            case RLI:
                int rliLowByte = pullRStack();
                int rliHighByte = pullRStack();
                registerI = (rliHighByte << 8) | rliLowByte;
                flagSign = registerI > 32767;
                flagZero = registerI == 0;
                break;
            case AND_IND:
                registerA &= readDynamic(getAbsAddress(readMemory(programCounter + 1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case BIT_ZP_X:
                int bitZpXValue = readDynamic(readMemory(programCounter + 1) + registerX & 255, false);

                flagZero = (registerA & bitZpXValue) == 0;
                if(isEightBit(false)) {
                    flagSign = bitZpXValue > 127;
                    flagOverflow = testBit(bitZpXValue, 6);
                } else {
                    flagSign = bitZpXValue > 32767;
                    flagOverflow = testBit(bitZpXValue, 14);
                }
                break;
            case DEC_A:
                if(isEightBit(false)) {
                    registerA = (registerA&65280) | (--registerA&255);
                } else {
                    registerA--;
                    registerA &= 65535;
                }
                setSignZeroDynamic(registerA, false);
                break;
            case RLX:
                registerX = pullRStack();
                if(!isEightBit(true)) {registerX |= (pullRStack() << 8);}
                setSignZeroDynamic(registerX, true);
                break;
            case BIT_ABS_X:
                int bitAbsXValue = readDynamic(getAbsAddress(programCounter + 1)+registerX, false);

                flagZero = (registerA & bitAbsXValue) == 0;
                if(isEightBit(false)) {
                    flagSign = bitAbsXValue > 127;
                    flagOverflow = testBit(bitAbsXValue, 6);
                } else {
                    flagSign = bitAbsXValue > 32767;
                    flagOverflow = testBit(bitAbsXValue, 14);
                }
                break;
            case RHA:
                pushRStack(registerA);
                if(!isEightBit(false)) {pushRStack(registerA >>> 8);}
                break;
            case EOR_IND:
                registerA ^= readDynamic(getAbsAddress(readMemory(programCounter+1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case PHY:
                pushPStack(registerY);
                if(!isEightBit(true)) {pushPStack(registerY >>> 8);}
                break;
            case RHY:
                pushRStack(registerY);
                if(!isEightBit(true)) {pushRStack(registerY >>> 8);}
                break;
            case TXI:
                registerI = registerX;
                flagSign = registerI > 32767;
                flagZero = registerI == 0;
                break;
            case STZ_ZP:
                writeMemory(readMemory(programCounter+1), 0);
                break;
            case RLA:
                registerA = pullRStack();
                if(!isEightBit(false)) {registerA |= (pullRStack() << 8);}
                setSignZeroDynamic(registerA, false);
                break;
            case ADC_IND:
                performAddition(getAbsAddress(readMemory(programCounter+1)), false);
                break;
            case STZ_ZP_X:
                writeMemory(readMemory(programCounter+1)+registerX & 255, 0);
                break;
            case PLY:
                registerY = pullPStack();
                if(!isEightBit(true)) {registerY |= (pullPStack() << 8);}
                setSignZeroDynamic(registerY, true);
                break;
            case RLY:
                registerY = pullRStack();
                if(!isEightBit(true)) {registerY |= (pullRStack() << 8);}
                setSignZeroDynamic(registerY, true);
                break;
            case JMP_ABS_X:
                setProgramCounter(getAbsAddress(getAbsAddress(programCounter+1)+registerX));
                break;
            case BRA_REL:
                byte relValue = (byte) readMemory(programCounter+1);
                setProgramCounter(programCounter + relValue);
                break;
            case BIT_IMM:
                flagZero = (registerA & readDynamic(programCounter + 1, false)) == 0;
                break;
            case TXR:
                rStackPointer = registerX;
                rStackPointer &= 255;
                setSignZero(rStackPointer);
                break;
            case STA_IND:
                writeDynamic(getAbsAddress(readMemory(programCounter + 1)), registerA, false);
                break;
            case TXY:
                registerY = registerX;
                setSignZeroDynamic(registerY, true);
                break;
            case STZ_ABS:
                writeMemory(getAbsAddress(programCounter+1), 0);
                break;
            case STZ_ABS_X:
                writeMemory(getAbsAddress(programCounter+1)+registerX, 0);
                break;
            case TRX:
                registerX = rStackPointer;
                setSignZeroDynamic(registerX, true);
                break;
            case TDA:
                if(isEightBit(false)) {
                    registerA = (registerA&65280) | (registerD&255);
                } else {
                    registerA = registerD;
                }

                setSignZeroDynamic(registerA, false);
                break;
            case LDA_IND:
                registerA = readDynamic(getAbsAddress(readMemory(programCounter + 1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case TYX:
                registerX = registerY;
                setSignZeroDynamic(registerX, true);
                break;
            case TAD:
                registerD = registerA;
                flagSign = registerD > 32767;
                flagZero = registerD == 0;
                break;
            case PLD:
                int pldLowByte = pullPStack();
                int pldHighByte = pullPStack();
                registerD = (pldHighByte << 8) | pldLowByte;
                flagSign = registerD > 32767;
                flagZero = registerD == 0;
                break;
            case CMP_IND:
                performComparation(getAbsAddress(readMemory(programCounter+1)), registerA, false);
                break;
            case PHX:
                pushPStack(registerX);
                if(!isEightBit(true)) {pushPStack(registerX >>> 8);}
                break;
            case TIX:
                registerX = registerI;
                if(isEightBit(true)) {registerX &= 255;}

                setSignZeroDynamic(registerX, true);
                break;
            case PHD:
                pushPStack(registerD);
                pushPStack(registerD >>> 8);
                break;
            case SBC_IND:
                performAddition(getAbsAddress(readMemory(programCounter+1)), true);
                break;
            case PLX:
                registerX = pullPStack();
                if(!isEightBit(true)) {registerX |= ((pullPStack() << 8)*255);}
                setSignZeroDynamic(registerX, true);
                break;
            case TSB_ZP:
                int tsbZpAddress = readMemory(programCounter+1);
                int tsbZpValue = readDynamic(tsbZpAddress, false);
                int tsbZpCompValue = isEightBit(false) ? registerA&255 : registerA;

                flagZero = (tsbZpCompValue & tsbZpValue) == 0;
                tsbZpValue |= tsbZpCompValue;

                writeDynamic(tsbZpAddress, tsbZpValue, false);
                break;
            case TSB_ABS:
                int tsbAbsAddress = getAbsAddress(programCounter + 1);
                int tsbAbsValue = readDynamic(tsbAbsAddress, false);
                int tsbAbsCompValue = isEightBit(false) ? registerA&255 : registerA;

                flagZero = (tsbAbsCompValue & tsbAbsValue) == 0;
                tsbAbsValue |= tsbAbsCompValue;

                writeDynamic(tsbAbsAddress, tsbAbsValue, false);
                break;
            case TRB_ZP:
                int trbZpAddress = readMemory(programCounter + 1);
                int trbZpValue = readDynamic(trbZpAddress, false);
                int trbZpCompValue = isEightBit(false) ? registerA&255 : registerA;

                flagZero = (trbZpCompValue & trbZpValue) == 0;
                trbZpValue |= ~trbZpCompValue;

                writeDynamic(trbZpAddress, trbZpValue, false);
                break;
            case TRB_ABS:
                int trbAbsAddress = getAbsAddress(programCounter + 1);
                int trbAbsValue = readMemory(trbAbsAddress);
                int trbAbsCompValue = isEightBit(false) ? registerA&255 : registerA;

                flagZero = (trbAbsCompValue & trbAbsValue) == 0;
                trbAbsValue |= ~trbAbsCompValue;

                writeMemory(trbAbsAddress, trbAbsValue);
                break;
            case NXT:
                setProgramCounter(getAbsAddress(registerI));
                registerI += 2;
                registerI &= 65535;
                break;
            case ENT:
                pushRStack(registerI);
                registerI = programCounter + 3;
                registerI &= 65535;
                setProgramCounter(getAbsAddress(programCounter+1));
                break;
            case NXA:
                registerA = readDynamic(registerI, false);
                registerI += isEightBit(false) ? 1 : 2;
                registerI &= 65535;
                break;
            case REA_ABS:
                int reaAbsValue = getAbsAddress(programCounter+1);

                pushRStack(reaAbsValue >>> 8);
                pushRStack(reaAbsValue);
                break;
            case REI_IND:
                int reiIndValue = getAbsAddress(readMemory(programCounter+1));

                pushRStack(reiIndValue >>> 8);
                pushRStack(reiIndValue);
                break;
            case PER_REL:
                int perRelAddress = programCounter + ((byte) readMemory(programCounter+1)) + 2;
                int perRelValue = getAbsAddress(perRelAddress);

                pushPStack(perRelValue >>> 8);
                pushPStack(perRelValue);
                break;
            case RER_REL:
                int rerRelAddress = programCounter + ((byte) readMemory(programCounter+1)) + 2;
                int rerRelValue = getAbsAddress(rerRelAddress);

                pushRStack(rerRelValue >>> 8);
                pushRStack(rerRelValue);
                break;
            case ZEA:
                registerD = 0;
                break;
            case SEA:
                if(isEightBit(false)) {
                    registerD = registerA > 127 ? 65535 : 0;
                } else {
                    registerD = registerA > 32767 ? 65535 : 0;
                }
                break;
            case REP_IMM:
                int repImmValue = readMemory(programCounter+1);

                flagCarry &= testBit(repImmValue, 0);
                flagZero &= testBit(repImmValue, 1);
                flagInterrupt &= testBit(repImmValue, 2);
                flagDecimal &= testBit(repImmValue, 3);
                flagBreak &= testBit(repImmValue, 4);
                flagAccumulator &= testBit(repImmValue, 5);
                flagOverflow &= testBit(repImmValue, 6);
                flagSign &= testBit(repImmValue, 7);
                break;
            case PEI_IND:
                int peiIndValue = getAbsAddress(readMemory(programCounter+1));

                pushPStack(peiIndValue >>> 8);
                pushPStack(peiIndValue);
                break;
            case SEP_IMM:
                int sepImmValue = readMemory(programCounter+1);

                flagCarry |= testBit(sepImmValue, 0);
                flagZero |= testBit(sepImmValue, 1);
                flagInterrupt |= testBit(sepImmValue, 2);
                flagDecimal |= testBit(sepImmValue, 3);
                flagBreak |= testBit(sepImmValue, 4);
                flagAccumulator |= testBit(sepImmValue, 5);
                flagOverflow |= testBit(sepImmValue, 6);
                flagSign |= testBit(sepImmValue, 7);
                break;
            case XBA:
                int lowByte = registerA & 255;
                registerA = (lowByte << 8) | (registerA >>> 8);
                break;
            case PEA_ABS:
                int peaAbsValue = getAbsAddress(programCounter+1);

                pushPStack(peaAbsValue >>> 8);
                pushPStack(peaAbsValue);
                break;
            case XCE:
                boolean flagTemp = flagCarry;
                flagCarry = flagEmulate;
                flagEmulate = flagTemp;

                if(flagEmulate) {
                    registerX &= 255;
                    registerY &= 255;
                    flagBreak = false;
                } else {
                    flagBreak = true;
                }
                flagAccumulator = true;

                break;
            case JSR_ABS_X:
                int jsrAbsXValue = programCounter + 2;
                pushRStack(jsrAbsXValue >>> 8);
                pushRStack(jsrAbsXValue);

                setProgramCounter(getAbsAddress(getAbsAddress(programCounter+1)+registerX));
                break;
            case ORA_STK:
                registerA |= readDynamic(256 + pStackPointer + ((byte) readMemory(programCounter + 1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case ORA_RSTK:
                registerA |= readDynamic(512 + rStackPointer + ((byte) readMemory(programCounter + 1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case ORA_STK_Y:
                registerA |= readDynamic(getAbsAddress(256 + pStackPointer + ((byte) readMemory(programCounter + 1)))+registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case ORA_RSTK_Y:
                registerA |= readDynamic(getAbsAddress(512 + rStackPointer + ((byte) readMemory(programCounter + 1))) + registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case AND_STK:
                registerA &= readDynamic(256 + pStackPointer + ((byte) readMemory(programCounter + 1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case AND_RSTK:
                registerA &= readDynamic(512 + rStackPointer + ((byte) readMemory(programCounter + 1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case AND_STK_Y:
                registerA &= readDynamic(getAbsAddress(256 + pStackPointer + ((byte) readMemory(programCounter + 1))) + registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case AND_RSTK_Y:
                registerA &= readDynamic(getAbsAddress(512 + rStackPointer + ((byte) readMemory(programCounter + 1))) + registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case EOR_STK:
                registerA ^= readDynamic(256 + pStackPointer + ((byte) readMemory(programCounter + 1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case EOR_RSTK:
                registerA ^= readDynamic(512 + rStackPointer + ((byte) readMemory(programCounter + 1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case EOR_STK_Y:
                registerA ^= readDynamic(getAbsAddress(256 + pStackPointer + ((byte) readMemory(programCounter + 1))) + registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case EOR_RSTK_Y:
                registerA ^= readDynamic(getAbsAddress(512 + rStackPointer + ((byte) readMemory(programCounter + 1))) + registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case ADC_STK:
                performAddition(256 + pStackPointer + ((byte) readMemory(programCounter + 1)), false);
                break;
            case ADC_RSTK:
                performAddition(512 + rStackPointer + ((byte) readMemory(programCounter + 1)), false);
                break;
            case ADC_STK_Y:
                performAddition(getAbsAddress(256 + pStackPointer + ((byte) readMemory(programCounter + 1)))+registerY, false);
                break;
            case ADC_RSTK_Y:
                performAddition(getAbsAddress(512 + rStackPointer + ((byte) readMemory(programCounter + 1)))+registerY, false);
                break;
            case STA_STK:
                writeDynamic(256 + pStackPointer + ((byte) readMemory(programCounter + 1)), registerA, false);
                break;
            case STA_RSTK:
                writeDynamic(512 + rStackPointer + ((byte) readMemory(programCounter + 1)), registerA, false);
                break;
            case STA_STK_Y:
                writeDynamic(getAbsAddress(256 + pStackPointer + ((byte) readMemory(programCounter + 1)))+registerY, registerA, false);
                break;
            case STA_RSTK_Y:
                writeDynamic(getAbsAddress(512 + rStackPointer + ((byte) readMemory(programCounter + 1)))+registerY, registerA, false);
                break;
            case LDA_STK:
                registerA = readDynamic(256 + pStackPointer + ((byte) readMemory(programCounter + 1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case LDA_RSTK:
                registerA = readDynamic(512 + rStackPointer + ((byte) readMemory(programCounter + 1)), false);
                setSignZeroDynamic(registerA, false);
                break;
            case LDA_STK_Y:
                registerA = readDynamic(getAbsAddress(256 + pStackPointer + ((byte) readMemory(programCounter + 1)))+ registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case LDA_RSTK_Y:
                registerA = readDynamic(getAbsAddress(512 + rStackPointer + ((byte) readMemory(programCounter + 1)))+ registerY, false);
                setSignZeroDynamic(registerA, false);
                break;
            case CMP_STK:
                performComparation(256 + pStackPointer + ((byte) readMemory(programCounter + 1)), registerA, false);
                break;
            case CMP_RSTK:
                performComparation(512 + rStackPointer + ((byte) readMemory(programCounter + 1)), registerA, false);
                break;
            case CMP_STK_Y:
                performComparation(getAbsAddress(256 + pStackPointer + ((byte) readMemory(programCounter + 1)))+registerY, registerA, false);
                break;
            case CMP_RSTK_Y:
                performComparation(getAbsAddress(512 + rStackPointer + ((byte) readMemory(programCounter + 1)))+registerY, registerA, false);
                break;
            case SBC_STK:
                performAddition(256 + pStackPointer + ((byte) readMemory(programCounter + 1)), true);
                break;
            case SBC_RSTK:
                performAddition(512 + rStackPointer + ((byte) readMemory(programCounter + 1)), true);
                break;
            case SBC_STK_Y:
                performAddition(getAbsAddress(256 + pStackPointer + ((byte) readMemory(programCounter + 1)))+registerY, true);
                break;
            case SBC_RSTK_Y:
                performAddition(getAbsAddress(512 + rStackPointer + ((byte) readMemory(programCounter + 1)))+registerY, true);
                break;
            case MUL_ZP:
                break;
            case MUL_ZP_X:
                break;
            case MUL_ABS:
                break;
            case MUL_ABS_X:
                break;
            case DIV_ZP:
                break;
            case DIV_ZP_X:
                break;
            case DIV_ABS:
                break;
            case DIV_ABS_X:
                break;
            case WAI:
                break;
            case MMU:
                break;
            case UNUSED:
                setRunning(false);
                break;
        }

        setProgramCounter(programCounter + instruction.getLength());
        cyclesElapsed += instruction.getNumCycles();
    }

    private int getAbsAddress(int addressToRead) {
        return (readMemory(addressToRead+1) << 8) | readMemory(addressToRead);
    }

    private int pullImmAddress() {
        return (pullRStack() << 8) | pullRStack();
    }

    private void setSignZeroDynamic(int testValue, boolean isIndexRegister) {
        if(isEightBit(isIndexRegister)) {
            testValue &= 255;
            flagSign = testValue > 127;
        } else {
            flagSign = testValue > 32767;
        }
        flagZero = testValue == 0;
    }

    private void setSignZero(int testValue) {
        flagSign = testValue > 127;
        flagZero = testValue == 0;
    }

    private void performAddition(int addressOfValue, boolean subtract) {
        boolean isEightBit = isEightBit(false);

        int addValue = readDynamic(addressOfValue, false);
        int result = (flagCarry ? isEightBit ? 256 : 65536 : 0) + registerA + (subtract ? -addValue : addValue);

        if(isEightBit) {
            registerA = result & 255;
            flagOverflow = (byte) result < -128 || (byte) result > 127;
            flagCarry = result > 255;
        } else {
            registerA = result & 65535;
            flagOverflow = (short) result < -32768 || (short) result > 32767;
            flagCarry = result > 65535;
        }
        setSignZeroDynamic(registerA, false);

        if(flagDecimal) {cyclesElapsed++;}
    }

    private void performComparation(int addressOfValue, int valueComparedTo, boolean isIndexRegister) {
        boolean isEightBit = isEightBit(isIndexRegister);

        int compareValue = readDynamic(addressOfValue, isIndexRegister);
        int result = ((isEightBit ? 256 : 65536) + valueComparedTo) - compareValue;

        if(isEightBit) {
            flagCarry = result > 255;
            result &= 255;
        } else {
            flagCarry = result > 65535;
            result &= 65535;
        }
        setSignZeroDynamic(result, isIndexRegister);
    }

    private static boolean testBit(int target, int offset) {
        return (target&(1<<offset)) != 0;
    }

    private void pushPStack(int value) {
        memory[256 + pStackPointer] = (byte) value;
        pStackPointer--;
    }

    private void pushRStack(int value) {
        memory[512 + rStackPointer] = (byte) value;
        rStackPointer--;
    }

    private int pullPStack() {
        pStackPointer++;
        return memory[256 + pStackPointer]&255;
    }

    private int pullRStack() {
        rStackPointer++;
        return memory[512 + rStackPointer]&255;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("cursorX", cursorX);
        data.setInteger("cursorY", cursorY);
        data.setBoolean("running", running);
        data.setBoolean("floppyInDrive", floppyInDrive);

        return new S35PacketUpdateTileEntity(pos, 1, data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setInteger("cursorX", cursorX);
        data.setInteger("cursorY", cursorY);
        data.setInteger("registerA", registerA);
        data.setInteger("registerX", registerX);
        data.setInteger("registerY", registerY);
        data.setInteger("registerI", registerI);
        data.setInteger("registerD", registerD);
        data.setInteger("pStackPointer", pStackPointer);
        data.setInteger("rStackPointer", rStackPointer);
        data.setInteger("programCounter", programCounter);

        data.setBoolean("running", running);
        data.setBoolean("floppyInDrive", floppyInDrive);
        data.setBoolean("flagCarry", flagCarry);
        data.setBoolean("flagZero", flagZero);
        data.setBoolean("flagInterrupt", flagInterrupt);
        data.setBoolean("flagDecimal", flagDecimal);
        data.setBoolean("flagBreak", flagBreak);
        data.setBoolean("flagAccumulator", flagAccumulator);
        data.setBoolean("flagOverflow", flagOverflow);
        data.setBoolean("flagSign", flagSign);
        data.setBoolean("flagEmulator", flagEmulate);

        if(floppyFilename != null) {data.setString("floppyFilename", floppyFilename);}

        data.setByteArray("memory", memory);
        data.setByteArray("floppyData", floppyData);

        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        cursorX = data.getInteger("cursorX");
        cursorY = data.getInteger("cursorY");
        registerA = data.getInteger("registerA");
        registerX = data.getInteger("registerX");
        registerY = data.getInteger("registerY");
        registerI = data.getInteger("registerI");
        registerD = data.getInteger("registerD");
        pStackPointer = data.getInteger("pStackPointer");
        rStackPointer = data.getInteger("rStackPointer");
        programCounter = data.getInteger("programCounter");

        running = data.getBoolean("running");
        floppyInDrive = data.getBoolean("floppyInDrive");
        flagCarry = data.getBoolean("flagCarry");
        flagZero = data.getBoolean("flagZero");
        flagInterrupt = data.getBoolean("flagInterrupt");
        flagDecimal = data.getBoolean("flagDecimal");
        flagBreak = data.getBoolean("flagBreak");
        flagAccumulator = data.getBoolean("flagAccumulator");
        flagOverflow = data.getBoolean("flagOverflow");
        flagSign = data.getBoolean("flagSign");
        flagEmulate = data.getBoolean("flagEmulate");

        if(data.hasKey("floppyFilename")) {floppyFilename = data.getString("floppyFilename");}

        memory = data.getByteArray("memory");
        floppyData = data.getByteArray("floppyData");

        super.readFromNBT(data);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public String toString() {
        return String.format("A:%X X:%X Y:%X I:%X D:%X P:%X R:%X",
                registerA, registerX, registerY, registerI, registerD, pStackPointer, rStackPointer);
    }
}
