package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.block.BlockCPU;
import com.github.blackjak34.compute.enums.InstructionComputer;
import com.github.blackjak34.compute.redbus.RedbusDataPacket;
import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;

import static com.github.blackjak34.compute.enums.GuiConstantCPU.*;

public class TileEntityCPU extends TileEntity implements IUpdatePlayerListBox, IRedbusCompatible {

    public static final int BUS_ADDR = 0;

    private int registerA = 0;
    private int registerX = 0;
    private int registerY = 0;
    private int registerI = 0;
    private int registerD = 0;
    private int pStackPointer = 255;
    private int rStackPointer = 255;
    private int programCounter = 1024;
    private int cyclesElapsed = 0;
    private int redbusDevice = 0;
    private int redbusOffset = 0;
    private int addressBRK = 0;
    private int addressPOR = 0;

    private boolean running = false;
    private boolean flagCarry = false;
    private boolean flagZero = false;
    private boolean flagInterrupt = false;
    private boolean flagDecimal = false;
    private boolean flagBreak = true;
    private boolean flagAccumulator = true;
    private boolean flagOverflow = false;
    private boolean flagSign = false;
    private boolean flagEmulate = true;
    private boolean redbusEnabled = false;

    private byte[] memory = new byte[65536];

    public TileEntityCPU() {}

    public TileEntityCPU(World worldIn) {
        Arrays.fill(memory, (byte) 255);
        DoesNotCompute.copyFileIntoArray(worldIn, "bootloader", memory, 1024, 256);
    }

    public void onPacketReceived(RedbusDataPacket dataPacket) {
        if(!redbusEnabled || ((dataPacket.address&255) != BUS_ADDR && (dataPacket.address&255) != 255)) {return;}
        markDirty();

        memory[redbusOffset+(dataPacket.index&255)] = dataPacket.data;
    }

    public void onActionPerformed(int buttonId) {
        if(buttonId == BUTTON_STP.getValue()) {
            setRunning(false);
        } else if(buttonId == BUTTON_START.getValue()) {
            if(running) {
                setProgramCounter(1280);
            } else {
                setRunning(true);
            }
        } else if(buttonId == BUTTON_RST.getValue()) {
            setRunning(false);
            setProgramCounter(1024);
            Arrays.fill(memory, (byte) 0xFF);
            DoesNotCompute.copyFileIntoArray(worldObj, "bootloader", memory, 1024, 256);
        } else if(buttonId == BUTTON_DUMP.getValue()) {
            DoesNotCompute.copyArrayIntoFile(worldObj, "memorydump", memory);
        } else {
            return;
        }

        worldObj.markBlockForUpdate(pos);
    }

    private boolean isEightBit(boolean indexRegister) {
        return flagEmulate || (!indexRegister && flagAccumulator) || (indexRegister && flagBreak);
    }

    private int readDynamic(int index, boolean isIndexRegister) {
        if(isEightBit(isIndexRegister)) {
            return readMemory(index);
        } else {
            return (readMemory(index+1) << 8) | readMemory(index);
        }
    }

    private void writeDynamic(int index, int value, boolean isIndexRegister) {
        if(isEightBit(isIndexRegister)) {
            writeMemory(index, value);
        } else {
            writeMemory(index, value);
            writeMemory(index+1, (value >>> 8));
        }
    }

    private int readMemory(int index) {
        if(index < 0 || index > 65535) {return 255;}

        return memory[index] & 255;
    }

    private void writeMemory(int index, int value) {
        if(index < 0 || index > 65535) {return;}

        if(redbusEnabled && (index >= redbusOffset) && (index < redbusOffset+256)) {
            RedbusDataPacket.sendPacket(worldObj, pos, new RedbusDataPacket(redbusDevice, value, index-redbusOffset));
            if(index-redbusOffset == 4 || index-redbusOffset == 5) {
                memory[index] = (byte) (value & 15);
                return;
            }
        }

        memory[index] = (byte) value;
        //System.out.printf("Writing value %2X to location %4X\n", (byte) value, index);
    }

    private void setProgramCounter(int value) {
        programCounter = value & 65535;
    }

    private void setRunning(boolean value) {
        running = value;

        worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockCPU.RUNNING, value), 2);
    }

    public void update() {
        if(!running) {return;}
        markDirty();

        cyclesElapsed = 0;
        while(cyclesElapsed < 3500) {executeInstruction();}
    }

    public void executeInstruction() {
        InstructionComputer instruction = InstructionComputer.getInstruction(readMemory(programCounter));
        //System.out.printf("Executing instruction %s at location %4X\n", instruction.toString(), programCounter);
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
                if(!isEightBit(false)) {setProgramCounter(programCounter+1);}
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
                if(!isEightBit(false)) {setProgramCounter(programCounter+1);}
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
                if(!isEightBit(false)) {setProgramCounter(programCounter+1);}
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
                if(!isEightBit(true)) {setProgramCounter(programCounter+1);}
                break;
            case CPX_ZP:
                performComparation(readMemory(programCounter+1), registerX, true);
                break;
            case CPY_ABS:
                performComparation(getAbsAddress(programCounter+1), registerY, true);
                break;
            case CPY_IMM:
                performComparation(programCounter+1, registerY, true);
                if(!isEightBit(true)) {setProgramCounter(programCounter+1);}
                break;
            case CPY_ZP:
                performComparation(readMemory(programCounter+1), registerY, true);
                break;
            case DEC_ABS:
                int decAbsAddress = getAbsAddress(programCounter+1);
                int decAbsValue = flagEmulate ? readMemory(decAbsAddress) :
                        (readMemory(decAbsAddress+1) << 8) | readMemory(decAbsAddress);

                decAbsValue--;
                decAbsValue &= flagEmulate ? 255 : 65535;

                flagSign = decAbsValue > (flagEmulate ? 127 : 32767);
                flagZero = decAbsValue == 0;

                writeMemory(decAbsAddress, decAbsValue);
                if(!flagEmulate) {writeMemory(decAbsAddress+1, (decAbsValue >>> 8));}
                break;
            case DEC_ABS_X:
                int decAbsXAddress = getAbsAddress(programCounter+1)+registerX;
                int decAbsXValue = flagEmulate ? readMemory(decAbsXAddress) :
                        (readMemory(decAbsXAddress+1) << 8) | readMemory(decAbsXAddress);

                decAbsXValue--;
                decAbsXValue &= flagEmulate ? 255 : 65535;

                flagSign = decAbsXValue > (flagEmulate ? 127 : 32767);
                flagZero = decAbsXValue == 0;

                writeMemory(decAbsXAddress, decAbsXValue);
                if(!flagEmulate) {writeMemory(decAbsXAddress+1, (decAbsXValue >>> 8));}
                break;
            case DEC_ZP:
                int decZpAddress = readMemory(programCounter+1);
                int decZpValue = flagEmulate ? readMemory(decZpAddress) :
                        (readMemory(decZpAddress+1) << 8) | readMemory(decZpAddress);

                decZpValue--;
                decZpValue &= flagEmulate ? 255 : 65535;

                flagSign = decZpValue > (flagEmulate ? 127 : 32767);
                flagZero = decZpValue == 0;

                writeMemory(decZpAddress, decZpValue);
                if(!flagEmulate) {writeMemory(decZpAddress+1, (decZpValue >>> 8));}
                break;
            case DEC_ZP_X:
                int decZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int decZpXValue = flagEmulate ? readMemory(decZpXAddress) :
                        (readMemory(decZpXAddress+1) << 8) | readMemory(decZpXAddress);

                decZpXValue--;
                decZpXValue &= flagEmulate ? 255 : 65535;

                flagSign = decZpXValue > (flagEmulate ? 127 : 32767);
                flagZero = decZpXValue == 0;

                writeMemory(decZpXAddress, decZpXValue);
                if(!flagEmulate) {writeMemory(decZpXAddress+1, (decZpXValue >>> 8));}
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
                if(!isEightBit(false)) {setProgramCounter(programCounter+1);}
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
                int incAbsValue = flagEmulate ? readMemory(incAbsAddress) :
                        (readMemory(incAbsAddress+1) << 8) | readMemory(incAbsAddress);

                incAbsValue++;
                incAbsValue &= flagEmulate ? 255 : 65535;

                flagSign = incAbsValue > (flagEmulate ? 127 : 32767);
                flagZero = incAbsValue == 0;

                writeMemory(incAbsAddress, incAbsValue);
                if(!flagEmulate) {writeMemory(incAbsAddress+1, (incAbsValue >>> 8));}
                break;
            case INC_ABS_X:
                int incAbsXAddress = getAbsAddress(programCounter+1)+registerX;
                int incAbsXValue = flagEmulate ? readMemory(incAbsXAddress) :
                        (readMemory(incAbsXAddress+1) << 8) | readMemory(incAbsXAddress);

                incAbsXValue++;
                incAbsXValue &= flagEmulate ? 255 : 65535;

                flagSign = incAbsXValue > (flagEmulate ? 127 : 32767);
                flagZero = incAbsXValue == 0;

                writeMemory(incAbsXAddress, incAbsXValue);
                if(!flagEmulate) {writeMemory(incAbsXAddress+1, (incAbsXValue >>> 8));}
                break;
            case INC_ZP:
                int incZpAddress = readMemory(programCounter+1);
                int incZpValue = flagEmulate ? readMemory(incZpAddress) :
                        (readMemory(incZpAddress+1) << 8) | readMemory(incZpAddress);

                incZpValue++;
                incZpValue &= flagEmulate ? 255 : 65535;

                flagSign = incZpValue > (flagEmulate ? 127 : 32767);
                flagZero = incZpValue == 0;

                writeMemory(incZpAddress, incZpValue);
                if(!flagEmulate) {writeMemory(incZpAddress+1, (incZpValue >>> 8));}
                break;
            case INC_ZP_X:
                int incZpXAddress = readMemory(programCounter+1)+registerX & 255;
                int incZpXValue = flagEmulate ? readMemory(incZpXAddress) :
                        (readMemory(incZpXAddress+1) << 8) | readMemory(incZpXAddress);

                incZpXValue++;
                incZpXValue &= flagEmulate ? 255 : 65535;

                flagSign = incZpXValue > (flagEmulate ? 127 : 32767);
                flagZero = incZpXValue == 0;

                writeMemory(incZpXAddress, incZpXValue);
                if(!flagEmulate) {writeMemory(incZpXAddress+1, (incZpXValue >>> 8));}
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
                int jmpAbsAddress = getAbsAddress(programCounter+1);
                setProgramCounter(jmpAbsAddress);
                System.out.printf("Jumping to address %4X\n", jmpAbsAddress);
                break;
            case JMP_IND:
                int jmpIndAddress = getAbsAddress(getAbsAddress(programCounter+1));
                setProgramCounter(jmpIndAddress);
                System.out.printf("Jumping to address %4X\n", jmpIndAddress);
                break;
            case JSR_ABS:
                int addressToPush = programCounter + 2;
                pushRStack(addressToPush >>> 8);
                pushRStack(addressToPush);

                System.out.printf("Jumping to subroutine from %4X, pushing address %4X\n",
                        programCounter, programCounter+2);
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
                if(!isEightBit(false)) {setProgramCounter(programCounter+1);}
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
                if(!isEightBit(true)) {setProgramCounter(programCounter+1);}
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
                if(!isEightBit(true)) {setProgramCounter(programCounter+1);}
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
                if(!isEightBit(false)) {setProgramCounter(programCounter+1);}
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

                setProgramCounter(pullAbsAddress());
                break;
            case RTS:
                setProgramCounter(pullAbsAddress());
                System.out.printf("Returning from subroutine, setting program counter to %4X\n", programCounter+1);
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
                int stzZpAddress = readMemory(programCounter+1);
                writeMemory(stzZpAddress, 0);
                if(!flagEmulate) {writeMemory(stzZpAddress+1, 0);}
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
                int stzZpXAddress = readMemory(programCounter+1)+registerX & 255;
                writeMemory(stzZpXAddress, 0);
                if(!flagEmulate) {writeMemory(stzZpXAddress+1, 0);}
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
                int jmpAbsXAddress = getAbsAddress(getAbsAddress(programCounter+1)+registerX);
                setProgramCounter(jmpAbsXAddress);
                System.out.printf("Jumping to address %4X\n", jmpAbsXAddress);
                break;
            case BRA_REL:
                byte relValue = (byte) readMemory(programCounter+1);
                setProgramCounter(programCounter + relValue);
                break;
            case BIT_IMM:
                flagZero = (registerA & readDynamic(programCounter + 1, false)) == 0;
                if(!isEightBit(false)) {setProgramCounter(programCounter+1);}
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
                int stzAbsAddress = getAbsAddress(programCounter+1);
                writeMemory(stzAbsAddress, 0);
                if(!flagEmulate) {writeMemory(stzAbsAddress+1, 0);}
                break;
            case STZ_ABS_X:
                int stzAbsXAddress = getAbsAddress(programCounter+1)+registerX;
                writeMemory(stzAbsXAddress, 0);
                if(!flagEmulate) {writeMemory(stzAbsXAddress+1, 0);}
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

                flagCarry &= !testBit(repImmValue, 0);
                flagZero &= !testBit(repImmValue, 1);
                flagInterrupt &= !testBit(repImmValue, 2);
                flagDecimal &= !testBit(repImmValue, 3);
                flagBreak &= !testBit(repImmValue, 4);
                flagAccumulator &= !testBit(repImmValue, 5);
                flagOverflow &= !testBit(repImmValue, 6);
                flagSign &= !testBit(repImmValue, 7);
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

                System.out.printf("Jumping to subroutine from %4X, pushing address %4X\n",
                        programCounter, programCounter+2);
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
            case MMU:
                switch(readMemory(programCounter+1)) {
                    case 0:
                        redbusDevice = registerA;
                        break;
                    case 128:
                        registerA = redbusDevice;
                        break;
                    case 1:
                        redbusOffset = registerA;
                        break;
                    case 129:
                        registerA = redbusOffset;
                        break;
                    case 2:
                        redbusEnabled = true;
                        break;
                    case 130:
                        redbusEnabled = false;
                        break;
                    case 3:
                        break;
                    case 131:
                        break;
                    case 4:
                        break;
                    case 132:
                        break;
                    case 5:
                        addressBRK = registerA;
                        break;
                    case 133:
                        registerA = addressBRK;
                        break;
                    case 6:
                        addressPOR = registerA;
                        break;
                    case 134:
                        registerA = addressPOR;
                        break;
                    case 255:
                        System.out.println(toString());
                        break;
                }
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
                cyclesElapsed += 3500;
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

    private int pullAbsAddress() {
        int low = pullRStack();
        int high = pullRStack();

        return (high << 8) | low;
    }

    private void setSignZeroDynamic(int testValue, boolean isIndexRegister) {
        if(isEightBit(isIndexRegister)) {
            testValue &= 255;
            flagSign = testValue > 127;
        } else {
            testValue &= 65535;
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

        flagCarry = result > (isEightBit ? 255 : 65535);
        setSignZeroDynamic(result, isIndexRegister);

        System.out.printf("Comparing, EB:%b CT:%2X AD:%4X CV:%2X RS:%2X\n", isEightBit, valueComparedTo, addressOfValue, compareValue, result&255);
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

        data.setBoolean("running", running);
        super.writeToNBT(data);

        return new S35PacketUpdateTileEntity(pos, 0, data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setInteger("registerA", registerA);
        data.setInteger("registerX", registerX);
        data.setInteger("registerY", registerY);
        data.setInteger("registerI", registerI);
        data.setInteger("registerD", registerD);
        data.setInteger("pStackPointer", pStackPointer);
        data.setInteger("rStackPointer", rStackPointer);
        data.setInteger("programCounter", programCounter);

        data.setBoolean("running", running);
        data.setBoolean("flagCarry", flagCarry);
        data.setBoolean("flagZero", flagZero);
        data.setBoolean("flagInterrupt", flagInterrupt);
        data.setBoolean("flagDecimal", flagDecimal);
        data.setBoolean("flagBreak", flagBreak);
        data.setBoolean("flagAccumulator", flagAccumulator);
        data.setBoolean("flagOverflow", flagOverflow);
        data.setBoolean("flagSign", flagSign);
        data.setBoolean("flagEmulator", flagEmulate);

        data.setByteArray("memory", memory);

        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        registerA = data.getInteger("registerA");
        registerX = data.getInteger("registerX");
        registerY = data.getInteger("registerY");
        registerI = data.getInteger("registerI");
        registerD = data.getInteger("registerD");
        pStackPointer = data.getInteger("pStackPointer");
        rStackPointer = data.getInteger("rStackPointer");
        programCounter = data.getInteger("programCounter");

        running = data.getBoolean("running");
        flagCarry = data.getBoolean("flagCarry");
        flagZero = data.getBoolean("flagZero");
        flagInterrupt = data.getBoolean("flagInterrupt");
        flagDecimal = data.getBoolean("flagDecimal");
        flagBreak = data.getBoolean("flagBreak");
        flagAccumulator = data.getBoolean("flagAccumulator");
        flagOverflow = data.getBoolean("flagOverflow");
        flagSign = data.getBoolean("flagSign");
        flagEmulate = data.getBoolean("flagEmulate");

        memory = data.getByteArray("memory");

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
