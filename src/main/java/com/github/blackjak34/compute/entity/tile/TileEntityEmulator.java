package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.block.BlockConsole;
import com.github.blackjak34.compute.enums.CharacterComputer;
import com.github.blackjak34.compute.enums.InstructionComputer;
import com.github.blackjak34.compute.item.ItemFloppy;
import com.github.blackjak34.compute.packet.MessageUpdateCursor;
import com.github.blackjak34.compute.packet.MessageUpdateDisplay;
import com.google.common.io.Files;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
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
    private boolean flagOverflow = false;
    private boolean flagSign = false;

    private byte[] keyBuffer = new byte[16];
    private byte[] memory = new byte[32768];
    private byte[] floppyData = new byte[32768];

    private String floppyFilename;

    public TileEntityEmulator(World world) {
        Arrays.fill(memory, (byte) 0xFF);
        Arrays.fill(floppyData, (byte) 0xFF);
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
        Arrays.fill(floppyData, (byte) 0xFF);

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
        InstructionComputer instruction = InstructionComputer.getInstruction(readMemory(programCounter)&255);
        switch(instruction) {
            case ADC_ABS:
                performAddition(readImmAddress(programCounter+1), false);
                break;
            case ADC_ABS_X:
                performAddition(readImmAddress(programCounter+1)+registerX, false);
                break;
            case ADC_ABS_Y:
                performAddition(readImmAddress(programCounter+1)+registerY, false);
                break;
            case ADC_IMM:
                performAddition(programCounter+1, false);
                break;
            case ADC_IND_X:
                performAddition(readImmAddress(readMemory(programCounter+1)+registerX & 255), false);
                break;
            case ADC_IND_Y:
                performAddition(readImmAddress(readMemory(programCounter+1))+registerY, false);
                break;
            case ADC_ZP:
                performAddition(readMemory(programCounter+1), false);
                break;
            case ADC_ZP_X:
                performAddition(readMemory(programCounter+1)+registerX & 255, false);
                break;
            case AND_ABS:
                registerA &= readMemory(readImmAddress(programCounter+1));
                setSignZeroFlags(registerA);
                break;
            case AND_ABS_X:
                registerA &= readMemory(readImmAddress(programCounter+1)+registerX);
                setSignZeroFlags(registerA);
                break;
            case AND_ABS_Y:
                registerA &= readMemory(readImmAddress(programCounter+1)+registerY);
                setSignZeroFlags(registerA);
                break;
            case AND_IMM:
                registerA &= readMemory(programCounter+1);
                setSignZeroFlags(registerA);
                break;
            case AND_IND_X:
                registerA &= readMemory(readImmAddress(readMemory(programCounter+1)+registerX & 255));
                setSignZeroFlags(registerA);
                break;
            case AND_IND_Y:
                registerA &= readMemory(readImmAddress(readMemory(programCounter+1))+registerY);
                setSignZeroFlags(registerA);
                break;
            case AND_ZP:
                registerA &= readMemory(readMemory(programCounter+1));
                setSignZeroFlags(registerA);
                break;
            case AND_ZP_X:
                registerA &= readMemory(readMemory(programCounter+1)+registerX & 255);
                setSignZeroFlags(registerA);
                break;
            case ASL_A:
                flagCarry = registerA<0;

                registerA <<= 1;
                setSignZeroFlags(registerA);
                break;
            case ASL_ABS:
                int aslAbsAddress = readImmAddress(programCounter+1);
                int aslAbsValue = readMemory(aslAbsAddress);
                flagCarry = aslAbsValue>127;

                aslAbsValue <<= 1;
                setSignZeroFlags(aslAbsValue);

                writeMemory(aslAbsAddress, aslAbsValue);
                break;
            case ASL_ABS_X:
                int aslAbsXAddress = readImmAddress(programCounter+1)+registerX;
                int aslAbsXValue = readMemory(aslAbsXAddress);
                flagCarry = aslAbsXValue>127;

                aslAbsXValue <<= 1;
                setSignZeroFlags(aslAbsXValue);

                writeMemory(aslAbsXAddress, aslAbsXValue);
                break;
            case ASL_ZP:
                int aslZpAddress = readMemory(programCounter+1);
                int aslZpValue = readMemory(aslZpAddress);
                flagCarry = aslZpValue>127;

                aslZpValue <<= 1;
                setSignZeroFlags(aslZpValue);

                writeMemory(aslZpAddress, aslZpValue);
                break;
            case ASL_ZP_X:
                int aslZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int aslZpXValue = readMemory(aslZpXAddress);
                flagCarry = aslZpXValue>127;

                aslZpXValue <<= 1;
                setSignZeroFlags(aslZpXValue);

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
                int bitAbsValue = readMemory(readImmAddress(programCounter+1));

                flagZero = (registerA & bitAbsValue) == 0;
                flagSign = testBit(bitAbsValue, 6);
                flagOverflow = bitAbsValue>127;
                break;
            case BIT_ZP:
                int bitZpValue = readMemory(readMemory(programCounter+1));

                flagZero = (registerA & bitZpValue) == 0;
                flagSign = testBit(bitZpValue, 6);
                flagOverflow = bitZpValue>127;
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
                performComparation(readImmAddress(programCounter+1), registerA);
                break;
            case CMP_ABS_X:
                performComparation(readImmAddress(programCounter+1)+registerX, registerA);
                break;
            case CMP_ABS_Y:
                performComparation(readImmAddress(programCounter+1)+registerY, registerA);
                break;
            case CMP_IMM:
                performComparation(programCounter+1, registerA);
                break;
            case CMP_IND_X:
                performComparation(readImmAddress(readMemory(programCounter+1)+registerX & 255), registerA);
                break;
            case CMP_IND_Y:
                performComparation(readImmAddress(readMemory(programCounter+1))+registerY, registerA);
                break;
            case CMP_ZP:
                performComparation(readMemory(programCounter+1), registerA);
                break;
            case CMP_ZP_X:
                performComparation(readMemory(programCounter+1)+registerX & 255, registerA);
                break;
            case CPX_ABS:
                performComparation(readImmAddress(programCounter+1), registerX);
                break;
            case CPX_IMM:
                performComparation(programCounter+1, registerX);
                break;
            case CPX_ZP:
                performComparation(readMemory(programCounter+1), registerX);
                break;
            case CPY_ABS:
                performComparation(readImmAddress(programCounter+1), registerY);
                break;
            case CPY_IMM:
                performComparation(programCounter+1, registerY);
                break;
            case CPY_ZP:
                performComparation(readMemory(programCounter+1), registerY);
                break;
            case DEC_ABS:
                int decAbsAddress = readImmAddress(programCounter+1);
                int decAbsValue = readMemory(decAbsAddress);

                decAbsValue--;
                setSignZeroFlags(decAbsValue);

                writeMemory(decAbsAddress, decAbsValue);
                break;
            case DEC_ABS_X:
                int decAbsXAddress = readImmAddress(programCounter+1)+registerX;
                int decAbsXValue = readMemory(decAbsXAddress);

                decAbsXValue--;
                setSignZeroFlags(decAbsXValue);

                writeMemory(decAbsXAddress, decAbsXValue);
                break;
            case DEC_ZP:
                int decZpAddress = readMemory(programCounter+1);
                int decZpValue = readMemory(decZpAddress);

                decZpValue--;
                setSignZeroFlags(decZpValue);

                writeMemory(decZpAddress, decZpValue);
                break;
            case DEC_ZP_X:
                int decZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int decZpXValue = readMemory(decZpXAddress);

                decZpXValue--;
                setSignZeroFlags(decZpXValue);

                writeMemory(decZpXAddress, decZpXValue);
                break;
            case DEX:
                registerX--;
                setSignZeroFlags(registerX);
                break;
            case DEY:
                registerY--;
                setSignZeroFlags(registerY);
                break;
            case EOR_ABS:
                registerA ^= readMemory(readImmAddress(programCounter+1));
                setSignZeroFlags(registerA);
                break;
            case EOR_ABS_X:
                registerA ^= readMemory(readImmAddress(programCounter+1)+registerX);
                setSignZeroFlags(registerA);
                break;
            case EOR_ABS_Y:
                registerA ^= readMemory(readImmAddress(programCounter+1)+registerY);
                setSignZeroFlags(registerA);
                break;
            case EOR_IMM:
                registerA ^= readMemory(programCounter+1);
                setSignZeroFlags(registerA);
                break;
            case EOR_IND_X:
                registerA ^= readMemory(readImmAddress(readMemory(programCounter+1)+registerX & 255));
                setSignZeroFlags(registerA);
                break;
            case EOR_IND_Y:
                registerA ^= readMemory(readImmAddress(readMemory(programCounter+1))+registerY);
                setSignZeroFlags(registerA);
                break;
            case EOR_ZP:
                registerA ^= readMemory(readMemory(programCounter+1));
                setSignZeroFlags(registerA);
                break;
            case EOR_ZP_X:
                registerA ^= readMemory(readMemory(programCounter+1)+registerX & 255);
                setSignZeroFlags(registerA);
                break;
            case INC_ABS:
                int incAbsAddress = readImmAddress(programCounter+1);
                int incAbsValue = readMemory(incAbsAddress);

                setSignZeroFlags(++incAbsValue);

                writeMemory(incAbsAddress, incAbsValue);
                break;
            case INC_ABS_X:
                int incAbsXAddress = readImmAddress(programCounter+1)+registerX;
                int incAbsXValue = readMemory(incAbsXAddress);

                incAbsXValue++;
                setSignZeroFlags(incAbsXValue);

                writeMemory(incAbsXAddress, incAbsXValue);
                break;
            case INC_ZP:
                int incZpAddress = readMemory(programCounter+1);
                int incZpValue = readMemory(incZpAddress);

                incZpValue++;
                setSignZeroFlags(incZpValue);

                writeMemory(incZpAddress, incZpValue);
                break;
            case INC_ZP_X:
                int incZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int incZpXValue = readMemory(incZpXAddress);

                incZpXValue++;
                setSignZeroFlags(incZpXValue);

                writeMemory(incZpXAddress, incZpXValue);
                break;
            case INX:
                registerX++;
                setSignZeroFlags(registerX);
                break;
            case INY:
                registerY++;
                setSignZeroFlags(registerY);
                break;
            case JMP_ABS:
                setProgramCounter(readImmAddress(programCounter+1));
                break;
            case JMP_IND:
                setProgramCounter(readImmAddress(readImmAddress(programCounter+1)));
                break;
            case JSR_ABS:
                int addressToPush = programCounter + 2;
                pushRStack(addressToPush >>> 8);
                pushRStack(addressToPush);

                setProgramCounter(readImmAddress(programCounter+1));
                break;
            case LDA_ABS:
                registerA = readMemory(readImmAddress(programCounter+1));
                setSignZeroFlags(registerA);
                break;
            case LDA_ABS_X:
                registerA = readMemory(readImmAddress(programCounter+1)+registerX);
                setSignZeroFlags(registerA);
                break;
            case LDA_ABS_Y:
                registerA = readMemory(readImmAddress(programCounter+1)+registerY);
                setSignZeroFlags(registerA);
                break;
            case LDA_IMM:
                registerA = readMemory(programCounter+1);
                setSignZeroFlags(registerA);
                break;
            case LDA_IND_X:
                registerA = readMemory(readImmAddress(readMemory(programCounter+1)+registerX & 255));
                setSignZeroFlags(registerA);
                break;
            case LDA_IND_Y:
                registerA = readMemory(readImmAddress(readMemory(programCounter+1))+registerY);
                setSignZeroFlags(registerA);
                break;
            case LDA_ZP:
                registerA = readMemory(readMemory(programCounter+1));
                setSignZeroFlags(registerA);
                break;
            case LDA_ZP_X:
                registerA = readMemory(readMemory(programCounter+1)+registerX & 255);
                setSignZeroFlags(registerA);
                break;
            case LDX_ABS:
                registerX = readMemory(readImmAddress(programCounter+1));
                setSignZeroFlags(registerX);
                break;
            case LDX_ABS_Y:
                registerX = readMemory(readImmAddress(programCounter+1)+registerY);
                setSignZeroFlags(registerX);
                break;
            case LDX_IMM:
                registerX = readMemory(programCounter+1);
                setSignZeroFlags(registerX);
                break;
            case LDX_ZP:
                registerX = readMemory(readMemory(programCounter+1));
                setSignZeroFlags(registerX);
                break;
            case LDX_ZP_Y:
                registerX = readMemory(readMemory(programCounter+1)+registerY & 255);
                setSignZeroFlags(registerX);
                break;
            case LDY_ABS:
                registerY = readMemory(readImmAddress(programCounter+1));
                setSignZeroFlags(registerY);
                break;
            case LDY_ABS_X:
                registerY = readMemory(readImmAddress(programCounter+1)+registerX);
                setSignZeroFlags(registerY);
                break;
            case LDY_IMM:
                registerY = readMemory(programCounter+1);
                setSignZeroFlags(registerY);
                break;
            case LDY_ZP:
                registerY = readMemory(readMemory(programCounter+1));
                setSignZeroFlags(registerY);
                break;
            case LDY_ZP_X:
                registerY = readMemory(readMemory(programCounter+1)+registerX & 255);
                setSignZeroFlags(registerY);
                break;
            case LSR_A:
                flagCarry = testBit(registerA, 0);

                registerA >>>= 1;
                setSignZeroFlags(registerA);
                break;
            case LSR_ABS:
                int lsrAbsAddress = readImmAddress(programCounter+1);
                int lsrAbsValue = readMemory(lsrAbsAddress);

                flagCarry = testBit(lsrAbsValue, 0);

                lsrAbsValue >>>= 1;
                setSignZeroFlags(lsrAbsValue);

                writeMemory(lsrAbsAddress, lsrAbsValue);
                break;
            case LSR_ABS_X:
                int lsrAbsXAddress = readImmAddress(programCounter+1)+registerX;
                int lsrAbsXValue = readMemory(lsrAbsXAddress);

                flagCarry = testBit(lsrAbsXValue, 0);

                lsrAbsXValue >>>= 1;
                setSignZeroFlags(lsrAbsXValue);

                writeMemory(lsrAbsXAddress, lsrAbsXValue);
                break;
            case LSR_ZP:
                int lsrZpAddress = readMemory(programCounter+1);
                int lsrZpValue = readMemory(lsrZpAddress);

                lsrZpValue >>>= 1;
                setSignZeroFlags(lsrZpValue);

                writeMemory(lsrZpAddress, lsrZpValue);
                break;
            case LSR_ZP_X:
                int lsrZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int lsrZpXValue = readMemory(lsrZpXAddress);

                lsrZpXValue >>>= 1;
                setSignZeroFlags(lsrZpXValue);

                writeMemory(lsrZpXAddress, lsrZpXValue);
                break;
            case NOP:
                break;
            case ORA_ABS:
                registerA |= readMemory(readImmAddress(programCounter+1));
                setSignZeroFlags(registerA);
                break;
            case ORA_ABS_X:
                registerA |= readMemory(readImmAddress(programCounter+1)+registerX);
                setSignZeroFlags(registerA);
                break;
            case ORA_ABS_Y:
                registerA |= readMemory(readImmAddress(programCounter+1)+registerY);
                setSignZeroFlags(registerA);
                break;
            case ORA_IMM:
                registerA |= readMemory(programCounter+1);
                setSignZeroFlags(registerA);
                break;
            case ORA_IND_X:
                registerA |= readMemory(readImmAddress(readMemory(programCounter+1)+registerX & 255));
                setSignZeroFlags(registerA);
                break;
            case ORA_IND_Y:
                registerA |= readMemory(readImmAddress(readMemory(programCounter+1))+registerY);
                setSignZeroFlags(registerA);
                break;
            case ORA_ZP:
                registerA |= readMemory(readMemory(programCounter+1));
                setSignZeroFlags(registerA);
                break;
            case ORA_ZP_X:
                registerA |= readMemory(readMemory(programCounter+1)+registerX & 255);
                setSignZeroFlags(registerA);
                break;
            case PHA:
                pushPStack(registerA);
                break;
            case PHP:
                int pushFlags = flagCarry ? 1 : 0;
                pushFlags += flagZero ? 2 : 0;
                pushFlags += flagInterrupt ? 4 : 0;
                pushFlags += flagDecimal ? 8 : 0;
                pushFlags += flagBreak ? 16 : 0;
                pushFlags += 32;
                pushFlags += flagOverflow ? 64 : 0;
                pushFlags += flagSign ? 128 : 0;

                pushPStack(pushFlags);
                break;
            case PLA:
                registerA = pullPStack();
                break;
            case PLP:
                int pullFlags = pullPStack();

                flagCarry = testBit(pullFlags, 0);
                flagZero = testBit(pullFlags, 1);
                flagInterrupt = testBit(pullFlags, 2);
                flagDecimal = testBit(pullFlags, 3);
                flagBreak = testBit(pullFlags, 4);
                flagOverflow = testBit(pullFlags, 6);
                flagSign = testBit(pullFlags, 7);
                break;
            case ROL_A:
                flagCarry = testBit(registerA, 7);
                registerA <<= 1;
                registerA &= 255;
                registerA += flagCarry ? 1 : 0;

                setSignZeroFlags(registerA);
                break;
            case ROL_ABS:
                int rolAbsAddress = readImmAddress(programCounter+1);
                int rolAbsValue = readMemory(rolAbsAddress);

                flagCarry = rolAbsValue>127;
                rolAbsValue <<= 1;
                rolAbsValue &= 255;
                rolAbsValue += flagCarry ? 1 : 0;

                setSignZeroFlags(rolAbsValue);
                writeMemory(rolAbsAddress, rolAbsValue);
                break;
            case ROL_ABS_X:
                int rolAbsXAddress = readImmAddress(programCounter+1)+registerX;
                int rolAbsXValue = readMemory(rolAbsXAddress);

                flagCarry = testBit(rolAbsXValue, 7);
                rolAbsXValue <<= 1;
                rolAbsXValue &= 255;
                rolAbsXValue += flagCarry ? 1 : 0;

                setSignZeroFlags(rolAbsXValue);
                writeMemory(rolAbsXAddress, rolAbsXValue);
                break;
            case ROL_ZP:
                int rolZpAddress = readMemory(readMemory(programCounter+1));
                int rolZpValue = readMemory(rolZpAddress);

                flagCarry = testBit(rolZpValue, 7);
                rolZpValue <<= 1;
                rolZpValue &= 255;
                rolZpValue += flagCarry ? 1 : 0;

                setSignZeroFlags(rolZpValue);
                writeMemory(rolZpAddress, rolZpValue);
                break;
            case ROL_ZP_X:
                int rolZpXAddress = readMemory(readMemory(programCounter+1)+registerX & 255);
                int rolZpXValue = readMemory(rolZpXAddress);

                flagCarry = testBit(rolZpXValue, 7);
                rolZpXValue <<= 1;
                rolZpXValue &= 255;
                rolZpXValue += flagCarry ? 1 : 0;

                setSignZeroFlags(rolZpXValue);
                writeMemory(rolZpXAddress, rolZpXValue);
                break;
            case ROR_A:
                flagCarry = testBit(registerA, 0);
                registerA >>>= 1;
                registerA += flagCarry ? 128 : 0;

                setSignZeroFlags(registerA);
                break;
            case ROR_ABS:
                int rorAbsAddress = readImmAddress(programCounter+1);
                int rorAbsValue = readMemory(rorAbsAddress);

                flagCarry = testBit(rorAbsValue, 0);
                rorAbsValue >>>= 1;
                rorAbsValue += flagCarry ? 128 : 0;

                setSignZeroFlags(rorAbsValue);
                writeMemory(rorAbsAddress, rorAbsValue);
                break;
            case ROR_ABS_X:
                int rorAbsXAddress = readImmAddress(programCounter+1)+registerX;
                int rorAbsXValue = readMemory(rorAbsXAddress);

                flagCarry = testBit(rorAbsXValue, 0);
                rorAbsXValue >>>= 1;
                rorAbsXValue += flagCarry ? 128 : 0;

                setSignZeroFlags(rorAbsXValue);
                writeMemory(rorAbsXAddress, rorAbsXValue);
                break;
            case ROR_ZP:
                int rorZpAddress = readMemory(programCounter+1);
                int rorZpValue = readMemory(rorZpAddress);

                flagCarry = testBit(rorZpValue, 0);
                rorZpValue >>>= 1;
                rorZpValue += flagCarry ? 128 : 0;

                setSignZeroFlags(rorZpValue);
                writeMemory(rorZpAddress, rorZpValue);
                break;
            case ROR_ZP_X:
                int rorZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int rorZpXValue = readMemory(rorZpXAddress);

                flagCarry = testBit(rorZpXValue, 0);
                rorZpXValue >>>= 1;
                rorZpXValue += flagCarry ? 128 : 0;

                setSignZeroFlags(rorZpXValue);
                writeMemory(rorZpXAddress, rorZpXValue);
                break;
            case RTI:
                int pullFlags2 = pullPStack();

                flagCarry = testBit(pullFlags2, 0);
                flagZero = testBit(pullFlags2, 1);
                flagInterrupt = testBit(pullFlags2, 2);
                flagDecimal = testBit(pullFlags2, 3);
                flagBreak = testBit(pullFlags2, 4);
                flagOverflow = testBit(pullFlags2, 6);
                flagSign = testBit(pullFlags2, 7);

                setProgramCounter(pullImmAddress());
                break;
            case RTS:
                setProgramCounter(pullImmAddress());
                break;
            case SBC_ABS:
                performAddition(readImmAddress(programCounter+1), true);
                break;
            case SBC_ABS_X:
                performAddition(readImmAddress(programCounter+1)+registerX, true);
                break;
            case SBC_ABS_Y:
                performAddition(readImmAddress(programCounter+1)+registerY, true);
                break;
            case SBC_IMM:
                performAddition(programCounter+1, true);
                break;
            case SBC_IND_X:
                performAddition(readImmAddress(readMemory(programCounter+1)+registerX & 255), true);
                break;
            case SBC_IND_Y:
                performAddition(readImmAddress(readMemory(programCounter+1))+registerY, true);
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
                writeMemory(readImmAddress(programCounter+1), registerA);
                break;
            case STA_ABS_X:
                writeMemory(readImmAddress(programCounter+1)+registerX, registerA);
                break;
            case STA_ABS_Y:
                writeMemory(readImmAddress(programCounter+1)+registerY, registerA);
                break;
            case STA_IND_X:
                writeMemory(readImmAddress(readMemory(programCounter+1)+registerX & 255), registerA);
                break;
            case STA_IND_Y:
                writeMemory(readImmAddress(readMemory(programCounter+1))+registerY, registerA);
                break;
            case STA_ZP:
                writeMemory(readMemory(programCounter+1), registerA);
                break;
            case STA_ZP_X:
                writeMemory(readMemory(programCounter+1)+registerX & 255, registerA);
                break;
            case STP:
                setRunning(false);
                break;
            case STX_ABS:
                writeMemory(readImmAddress(programCounter+1), registerX);
                break;
            case STX_ZP:
                writeMemory(readMemory(programCounter+1), registerX);
                break;
            case STX_ZP_Y:
                int stxZpYAddress = readImmAddress(readMemory(programCounter+1))+registerY;
                writeMemory(stxZpYAddress, registerX);
                break;
            case STY_ABS:
                writeMemory(readImmAddress(programCounter+1), registerY);
                break;
            case STY_ZP:
                writeMemory(readMemory(programCounter+1), registerY);
                break;
            case STY_ZP_X:
                writeMemory(readMemory(programCounter+1)+registerX & 255, registerY);
                break;
            case TAX:
                registerX = registerA;
                setSignZeroFlags(registerA);
                break;
            case TAY:
                registerY = registerA;
                setSignZeroFlags(registerA);
                break;
            case TSX:
                registerX = pStackPointer;
                setSignZeroFlags(pStackPointer);
                break;
            case TXA:
                registerA = registerX;
                setSignZeroFlags(registerX);
                break;
            case TXS:
                pStackPointer = registerX;
                setSignZeroFlags(registerX);
                break;
            case TYA:
                registerA = registerY;
                setSignZeroFlags(registerY);
                break;
            case RHI:
                pushRStack(registerI);
                break;
            case ORA_IND:
                registerA |= readMemory(readImmAddress(readMemory(programCounter+1)));
                setSignZeroFlags(registerA);
                break;
            case INC_A:
                registerA++;
                setSignZeroFlags(registerA);
                break;
            case RHX:
                pushRStack(registerX);
                break;
            case RLI:
                registerI = pullRStack();
                setSignZeroFlags(registerI);
                break;
            case AND_IND:
                registerA &= readMemory(readImmAddress(readMemory(programCounter+1)));
                break;
            case BIT_ZP_X:
                int bitZpXValue = readMemory(readMemory(programCounter+1)+registerX & 255);

                flagZero = (registerA & bitZpXValue) == 0;
                flagSign = testBit(bitZpXValue, 6);
                flagOverflow = bitZpXValue>127;
                break;
            case DEC_A:
                registerA--;
                setSignZeroFlags(registerA);
                break;
            case RLX:
                registerX = pullRStack();
                setSignZeroFlags(registerX);
                break;
            case BIT_ABS_X:
                int bitAbsXValue = readMemory(readImmAddress(programCounter + 1)+registerX);

                flagZero = (registerA & bitAbsXValue) == 0;
                flagSign = testBit(bitAbsXValue, 6);
                flagOverflow = bitAbsXValue>127;
                break;
            case RHA:
                pushRStack(registerA);
                break;
            case EOR_IND:
                registerA ^= readMemory(readImmAddress(readMemory(programCounter+1)));
                setSignZeroFlags(registerA);
                break;
            case PHY:
                pushPStack(registerY);
                break;
            case RHY:
                pushRStack(registerY);
                break;
            case TXI:
                registerX = registerI;
                setSignZeroFlags(registerI);
                break;
            case STZ_ZP:
                writeMemory(readMemory(programCounter+1), 0);
                break;
            case RLA:
                registerA = pullRStack();
                setSignZeroFlags(registerA);
                break;
            case ADC_IND:
                performAddition(readImmAddress(readMemory(programCounter+1)), false);
                break;
            case STZ_ZP_X:
                writeMemory(readMemory(programCounter+1)+registerX & 255, 0);
                break;
            case PLY:
                registerY = pullPStack();
                setSignZeroFlags(registerY);
                break;
            case RLY:
                registerY = pullRStack();
                setSignZeroFlags(registerY);
                break;
            case JMP_ABS_X:
                setProgramCounter(readImmAddress(readImmAddress(programCounter+1)+registerX));
                break;
            case BRA_REL:
                byte relValue = (byte) readMemory(programCounter+1);
                setProgramCounter(programCounter + relValue);
                break;
            case BIT_IMM:
                flagZero = (registerA & readMemory(programCounter + 1)) == 0;
                break;
            case TXR:
                rStackPointer = registerX;
                setSignZeroFlags(registerX);
                break;
            case STA_IND:
                writeMemory(readImmAddress(readMemory(programCounter+1)), registerA);
                break;
            case TXY:
                registerY = registerX;
                setSignZeroFlags(registerX);
                break;
            case STZ_ABS:
                writeMemory(readImmAddress(programCounter+1), 0);
                break;
            case STZ_ABS_X:
                writeMemory(readImmAddress(programCounter+1)+registerX, 0);
                break;
            case TRX:
                registerX = rStackPointer;
                setSignZeroFlags(rStackPointer);
                break;
            case TDA:
                registerA = registerD;
                setSignZeroFlags(registerD);
                break;
            case LDA_IND:
                registerA = readMemory(readImmAddress(readMemory(programCounter+1)));
                setSignZeroFlags(registerA);
                break;
            case TYX:
                registerX = registerY;
                setSignZeroFlags(registerY);
                break;
            case TAD:
                registerD = registerA;
                setSignZeroFlags(registerA);
                break;
            case PLD:
                registerD = pullPStack();
                setSignZeroFlags(registerD);
                break;
            case CMP_IND:
                performComparation(readImmAddress(readMemory(programCounter+1)), registerA);
                break;
            case PHX:
                pushPStack(registerX);
                break;
            case TIX:
                registerX = registerI;
                setSignZeroFlags(registerI);
                break;
            case PHD:
                pushPStack(registerD);
                break;
            case SBC_IND:
                performAddition(readImmAddress(readMemory(programCounter+1)), true);
                break;
            case PLX:
                registerX = pullPStack();
                setSignZeroFlags(registerX);
                break;
            case TSB_ZP:
                int tsbZpAddress = readMemory(programCounter+1);
                int tsbZpValue = readMemory(tsbZpAddress);

                flagZero = (registerA & tsbZpValue) == 0;
                tsbZpValue |= registerA;

                writeMemory(tsbZpAddress, tsbZpValue);
                break;
            case TSB_ABS:
                int tsbAbsAddress = readImmAddress(programCounter + 1);
                int tsbAbsValue = readMemory(tsbAbsAddress);

                flagZero = (registerA & tsbAbsValue) == 0;
                tsbAbsValue |= registerA;

                writeMemory(tsbAbsAddress, tsbAbsValue);
                break;
            case TRB_ZP:
                int trbZpAddress = readMemory(programCounter+1);
                int trbZpValue = readMemory(trbZpAddress);

                flagZero = (registerA & trbZpValue) == 0;
                trbZpValue |= ~registerA;

                writeMemory(trbZpAddress, trbZpValue);
                break;
            case TRB_ABS:
                int trbAbsAddress = readImmAddress(programCounter + 1);
                int trbAbsValue = readMemory(trbAbsAddress);

                flagZero = (registerA & trbAbsValue) == 0;
                trbAbsValue |= ~registerA;

                writeMemory(trbAbsAddress, trbAbsValue);
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

    private int readImmAddress(int addressToRead) {
        int low = readMemory(addressToRead);
        int high = readMemory(addressToRead+1);
        return (high << 8) | low;
    }

    private int pullImmAddress() {
        int low = pullRStack();
        int high = pullRStack();
        return (high << 8) | low;
    }

    private void setSignZeroFlags(int testValue) {
        flagSign = testValue>127;
        flagZero = testValue==0;
    }

    private void performAddition(int addressOfValue, boolean subtract) {
        int addValue = readMemory(addressOfValue);
        int result = (flagCarry ? 256 : 0) + registerA + (subtract ? -addValue : addValue);
        registerA = result & 255;

        flagOverflow = (byte) result<-128 || (byte) result>127;
        flagCarry = result>255;
        setSignZeroFlags(registerA);

        if(flagDecimal) {cyclesElapsed++;}
    }

    private void performComparation(int addressOfValue, int valueComparedTo) {
        int compareValue = readMemory(addressOfValue);
        int result = (256 + valueComparedTo) - compareValue;

        flagCarry = result>255;
        setSignZeroFlags(result & 255);
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
        data.setBoolean("flagOverflow", flagOverflow);
        data.setBoolean("flagSign", flagSign);

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
        flagOverflow = data.getBoolean("flagOverflow");
        flagSign = data.getBoolean("flagSign");

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
