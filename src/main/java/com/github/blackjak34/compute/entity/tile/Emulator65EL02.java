package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.enums.AddressingMode;
import com.github.blackjak34.compute.enums.InstructionComputer;
import com.github.blackjak34.compute.interfaces.IEmulatorIO;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;

public class Emulator65EL02 {

    private int registerA = 0;
    private int registerX = 0;
    private int registerY = 0;
    private int registerI = 0;
    private int registerD = 0;
    private int pStackPointer = 0xFF;
    private int rStackPointer = 0xFF;
    private int programCounter = 0x0400;
    private int cyclesElapsed = 0;
    private int redbusDevice = 0;
    private int redbusOffset = 0;
    private int windowOffset = 0;
    private int addressBRK = 0;
    private int addressPOR = 0;

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
    private boolean windowEnabled = false;

    private byte[] memory = new byte[0x10000];

    private IEmulatorIO io;

    public Emulator65EL02(IEmulatorIO io) {
        this.io = io;
    }

    public void writeWindow(int index, int value) {
        if(windowEnabled) {
            writeMemory(windowOffset+(index&255), value);
        }
    }

    public int readWindow(int index) {
        if(windowEnabled) {
            return readMemory(windowOffset+(index&255));
        }
        return 0xFF;
    }

    public void clearMemory() {
        Arrays.fill(memory, (byte) 0xFF);
    }

    public void setProgramCounter(int value) {
        programCounter = value & 0x0FFFF;
    }

    public int getCyclesElapsed() {
        return cyclesElapsed;
    }

    public void resetCyclesElapsed() {
        cyclesElapsed = 0;
    }

    public int getAddressPOR() {
        return addressPOR;
    }

    public void executeInstruction() {
        boolean eightBitAccumulator = flagEmulate || flagAccumulator;
        boolean eightBitIndexRegs = flagEmulate || flagBreak;

        InstructionComputer instruction = InstructionComputer.getInstruction(readMemory(programCounter));
        int operandAddress = getOperandLocation(instruction.getAddressingMode());
        /*
        System.out.printf("%4X: ", programCounter);
        String instructionName = instruction.toString().substring(0, 3);
        switch(instruction.getAddressingMode()) {
            case IMPLIED:
            case PUSH_P_STACK:
            case PUSH_R_STACK:
            case POP_P_STACK:
            case POP_R_STACK:
                System.out.println(instructionName);
                break;
            case RELATIVE:
                System.out.printf("%s $%4X\n", instructionName, programCounter+2 + (byte) readMemory(programCounter+1));
                break;
            case IMMEDIATE:
                switch(instruction) {
                    case LDX_IMM:
                    case LDY_IMM:
                    case CPX_IMM:
                    case CPY_IMM:
                        if(eightBitIndexRegs) {
                            System.out.printf("%s #$%2X\n", instructionName, readMemory(programCounter+1));
                        } else {
                            System.out.printf("%s #$%4X\n", instructionName, getIndirectAddress(programCounter, 1, 0));
                        }
                        break;
                    case BIT_IMM:case REP_IMM:case SEP_IMM:case MMU:
                        System.out.printf("%s #$%2X\n", instructionName, readMemory(programCounter+1));
                        break;
                    default:
                        if(eightBitAccumulator) {
                            System.out.printf("%s #$%2X\n", instructionName, readMemory(programCounter+1));
                        } else {
                            System.out.printf("%s #$%4X\n", instructionName, getIndirectAddress(programCounter, 1, 0));
                        }
                        break;
                }
                break;
            case ZERO_PAGE:
                System.out.printf("%s $%2X\n", instructionName, readMemory(programCounter+1));
                break;
            case ZERO_PAGE_X:
                System.out.printf("%s $%2X, X\n", instructionName, readMemory(programCounter+1));
                break;
            case ZERO_PAGE_Y:
                System.out.printf("%s $%2X, X\n", instructionName, readMemory(programCounter+1));
                break;
            case ABSOLUTE:
                System.out.printf("%s $%4X\n", instructionName, getIndirectAddress(programCounter, 1, 0));
                break;
            case ABSOLUTE_X:
                System.out.printf("%s $%4X, X\n", instructionName, getIndirectAddress(programCounter, 1, 0));
                break;
            case ABSOLUTE_Y:
                System.out.printf("%s $%4X, Y\n", instructionName, getIndirectAddress(programCounter, 1, 0));
                break;
            case INDIRECT:
                System.out.printf("%s ($%2X)\n", instructionName, readMemory(programCounter+1));
                break;
            case INDIRECT_16:
                System.out.printf("%s ($%4X)\n", instructionName, getIndirectAddress(programCounter, 1, 0));
                break;
            case INDIRECT_INDEXED:
                System.out.printf("%s ($%2X), Y\n", instructionName, readMemory(programCounter+1));
                break;
            case INDEXED_INDIRECT:
                System.out.printf("%s ($%2X, X)\n", instructionName, readMemory(programCounter+1));
                break;
            case P_STACK_INDEXED:
                System.out.printf("%s #$%2X, P\n", instructionName, readMemory(programCounter+1));
                break;
            case R_STACK_INDEXED:
                System.out.printf("%s #$%2X, R\n", instructionName, readMemory(programCounter+1));
                break;
            case P_STACK_INDIRECT:
                System.out.printf("%s (#$%2X, P), Y\n", instructionName, readMemory(programCounter+1));
                break;
            case R_STACK_INDIRECT:
                System.out.printf("%s (#$%2X, R), Y\n", instructionName, readMemory(programCounter+1));
                break;
        }
        System.out.printf("%4X: %s\n", programCounter, toString());
        */
        switch(instruction) {
            case BRK:
                pushPStack(programCounter >>> 8);
                pushPStack(programCounter);

                pushFlags(flagEmulate);
                flagInterrupt = true;
                flagDecimal = false;
                break;
            case RTI:
            {
                popFlags();
                if(flagEmulate) {flagBreak = false;}

                int programCounterLB = pullPStack();
                int programCounterHB = pullPStack();
                setProgramCounter(to16Bit(programCounterHB, programCounterLB));
            }
            break;
            case WAI:
                cyclesElapsed += 3500;
                break;
            case STP:
                io.halt();
                io.lightOnFire();
                break;
            case NOP:
                break;
            case XBA:
                registerA = ((registerA&255) << 8) | (registerA >>> 8);
                break;
            case ORA_IMM:
                if(!eightBitAccumulator) {setProgramCounter(programCounter + 1);}
            case ORA_ZP:
            case ORA_ZP_X:
            case ORA_ABS:
            case ORA_ABS_X:
            case ORA_ABS_Y:
            case ORA_IND:
            case ORA_IND_X:
            case ORA_IND_Y:
            case ORA_STK:
            case ORA_RSTK:
            case ORA_STK_Y:
            case ORA_RSTK_Y:
                if(eightBitAccumulator) {
                    registerA |= readMemory(operandAddress);
                    setSignZero8(registerA);
                } else {
                    registerA |= getIndirectAddress(operandAddress, 0, 0);
                    setSignZero16(registerA);
                }
                break;
            case TSB_ZP:
            case TSB_ABS:
                if(eightBitAccumulator) {
                    int operand = readMemory(operandAddress);
                    flagZero = (operand & registerA&255) == 0;
                    writeMemory(operandAddress, operand | (registerA&255));
                } else {
                    int operand = getIndirectAddress(operandAddress, 0, 0);
                    flagZero = (operand & registerA) == 0;
                    operand |= registerA;
                    writeMemory(operandAddress, operand);
                    writeMemory(operandAddress+1, operand >>> 8);
                }
                break;
            case TRB_ZP:
            case TRB_ABS:
                if(eightBitAccumulator) {
                    int operand = readMemory(operandAddress);
                    flagZero = (operand & registerA&255) == 0;
                    writeMemory(operandAddress, operand & ((~registerA)&255));
                } else {
                    int operand = getIndirectAddress(operandAddress, 0, 0);
                    flagZero = (operand & registerA) == 0;
                    operand &= (~registerA) & 65535;
                    writeMemory(operandAddress, operand);
                    writeMemory(operandAddress+1, operand >>> 8);
                }
                break;
            case ADC_IMM:
                if(!eightBitAccumulator) {setProgramCounter(programCounter + 1);}
            case ADC_ZP:
            case ADC_ZP_X:
            case ADC_ABS:
            case ADC_ABS_X:
            case ADC_ABS_Y:
            case ADC_IND:
            case ADC_IND_X:
            case ADC_IND_Y:
            case ADC_STK:
            case ADC_RSTK:
            case ADC_STK_Y:
            case ADC_RSTK_Y:
                if(eightBitAccumulator) {
                    registerA = (registerA&0xFF00) | performAddition(registerA, operandAddress, false, true, false);
                } else {
                    registerA = performAddition(registerA, operandAddress, false, false, false);
                }
                break;
            case SBC_IMM:
                if(!eightBitAccumulator) {setProgramCounter(programCounter + 1);}
            case SBC_ZP:
            case SBC_ZP_X:
            case SBC_ABS:
            case SBC_ABS_X:
            case SBC_ABS_Y:
            case SBC_IND:
            case SBC_IND_X:
            case SBC_IND_Y:
            case SBC_STK:
            case SBC_RSTK:
            case SBC_STK_Y:
            case SBC_RSTK_Y:
                if(eightBitAccumulator) {
                    registerA = (registerA&0xFF00) | performAddition(registerA, operandAddress, true, true, false);
                } else {
                    registerA = performAddition(registerA, operandAddress, true, false, false);
                }
                break;
            case ASL_ZP:
            case ASL_ZP_X:
            case ASL_ABS:
            case ASL_ABS_X:
            {
                if(eightBitAccumulator) {
                    int operand = readMemory(operandAddress);
                    flagCarry = (operand & 128) != 0;

                    operand <<= 1;
                    setSignZero8(operand);

                    writeMemory(operandAddress, operand);
                } else {
                    int operand = getIndirectAddress(operandAddress, 0, 0);
                    flagCarry = (operand & 32768) != 0;

                    operand <<= 1;
                    setSignZero16(operand);

                    writeMemory(operandAddress, operand);
                    writeMemory(operandAddress+1, operand >>> 8);
                }
            }
            break;
            case ASL_A:
                if(eightBitAccumulator) {
                    int accumulatorLB = registerA & 255;
                    flagCarry = (accumulatorLB & 128) != 0;

                    accumulatorLB <<= 1;
                    setSignZero8(accumulatorLB);

                    registerA = (registerA & 0xFF00) | (accumulatorLB & 255);
                } else {
                    flagCarry = (registerA & 32768) != 0;

                    registerA <<= 1;
                    setSignZero16(registerA);

                    registerA &= 65535;
                }
                break;
            case AND_IMM:
                if(!eightBitAccumulator) {setProgramCounter(programCounter + 1);}
            case AND_ZP:
            case AND_ZP_X:
            case AND_ABS:
            case AND_ABS_X:
            case AND_ABS_Y:
            case AND_IND:
            case AND_IND_X:
            case AND_IND_Y:
            case AND_STK:
            case AND_RSTK:
            case AND_STK_Y:
            case AND_RSTK_Y:
                if(eightBitAccumulator) {
                    registerA &= 0xFF00 | readMemory(operandAddress);
                    setSignZero8(registerA);
                } else {
                    registerA &= getIndirectAddress(operandAddress, 0, 0);
                    setSignZero16(registerA);
                }
                break;
            case ROL_ZP:
            case ROL_ZP_X:
            case ROL_ABS:
            case ROL_ABS_X:
                if(eightBitAccumulator) {
                    int operand = readMemory(operandAddress);

                    operand <<= 1;
                    operand += flagCarry ? 1 : 0;
                    flagCarry = (operand & 256) != 0;

                    setSignZero8(operand);
                    writeMemory(operandAddress, operand);
                } else {
                    int operand = getIndirectAddress(operandAddress, 0, 0);

                    operand <<= 1;
                    operand += flagCarry ? 1 : 0;
                    flagCarry = (operand & 65536) != 0;

                    setSignZero16(operand);
                    writeMemory(operandAddress, operand);
                    writeMemory(operandAddress+1, operand >>> 8);
                }
                break;
            case ROL_A:
                if(eightBitAccumulator) {
                    int accumulatorLB = registerA & 255;

                    accumulatorLB <<= 1;
                    accumulatorLB += flagCarry ? 1 : 0;
                    flagCarry = (accumulatorLB & 256) != 0;

                    setSignZero8(accumulatorLB);
                    registerA = (registerA&0xFF00) | (accumulatorLB&255);
                } else {
                    registerA <<= 1;
                    registerA += flagCarry ? 1 : 0;
                    flagCarry = (registerA & 65536) != 0;

                    setSignZero16(registerA);
                    registerA &= 65535;
                }
                break;
            case ROR_ZP:
            case ROR_ZP_X:
            case ROR_ABS:
            case ROR_ABS_X:
                if(eightBitAccumulator) {
                    int operand = readMemory(operandAddress);

                    boolean tempCarry = (operand & 1) != 0;
                    operand >>>= 1;
                    operand += flagCarry ? 128 : 0;
                    flagCarry = tempCarry;

                    setSignZero8(operand);
                    writeMemory(operandAddress, operand);
                } else {
                    int operand = getIndirectAddress(operandAddress, 0, 0);

                    boolean tempCarry = (operand & 1) != 0;
                    operand >>>= 1;
                    operand += flagCarry ? 32768 : 0;
                    flagCarry = tempCarry;

                    setSignZero16(operand);
                    writeMemory(operandAddress, operand);
                    writeMemory(operandAddress+1, operand >>> 8);
                }
                break;
            case ROR_A:
                if(eightBitAccumulator) {
                    int accumulatorLB = registerA & 255;

                    boolean tempCarry = (accumulatorLB & 1) != 0;
                    accumulatorLB >>>= 1;
                    accumulatorLB += flagCarry ? 128 : 0;
                    flagCarry = tempCarry;

                    setSignZero8(accumulatorLB);
                    registerA = (registerA&0xFF00) | accumulatorLB;
                } else {
                    boolean tempCarry = (registerA & 1) != 0;
                    registerA >>>= 1;
                    registerA += flagCarry ? 32768 : 0;
                    flagCarry = tempCarry;

                    setSignZero16(registerA);
                }
                break;
            case LSR_ZP:
            case LSR_ZP_X:
            case LSR_ABS:
            case LSR_ABS_X:
                if(eightBitAccumulator) {
                    int operand = readMemory(operandAddress);

                    flagCarry = (operand & 1) != 0;
                    operand >>>= 1;
                    setSignZero8(operand);

                    writeMemory(operandAddress, operand);
                } else {
                    int operand = getIndirectAddress(operandAddress, 0, 0);

                    flagCarry = (operand & 1) != 0;
                    operand >>>= 1;
                    setSignZero16(operand);

                    writeMemory(operandAddress, operand);
                    writeMemory(operandAddress+1, operand >>> 8);
                }
                break;
            case LSR_A:
                if(eightBitAccumulator) {
                    flagCarry = (registerA & 1) != 0;
                    registerA = (registerA&0xFF00) | ((registerA&255) >>> 1);
                } else {
                    flagCarry = (registerA & 1) != 0;
                    registerA >>>= 1;
                }
                break;
            case EOR_IMM:
                if(!eightBitAccumulator) {setProgramCounter(programCounter + 1);}
            case EOR_ZP:
            case EOR_ZP_X:
            case EOR_ABS:
            case EOR_ABS_X:
            case EOR_ABS_Y:
            case EOR_IND:
            case EOR_IND_X:
            case EOR_IND_Y:
            case EOR_STK:
            case EOR_RSTK:
            case EOR_STK_Y:
            case EOR_RSTK_Y:
                if(eightBitAccumulator) {
                    registerA = (registerX&0xFF00) | ((registerA ^ readMemory(operandAddress)) & 255);
                    setSignZero8(registerA);
                } else {
                    registerA ^= getIndirectAddress(operandAddress, 0, 0);
                    setSignZero16(registerA);
                    registerA &= 65535;
                }
                break;
            case BIT_ZP:
            case BIT_ABS:
            case BIT_ZP_X:
            case BIT_ABS_X:
                if(eightBitAccumulator) {
                    int operand = readMemory(operandAddress);

                    flagZero = ((registerA&255) & operand) == 0;
                    flagSign = (operand & 128) != 0;
                    flagOverflow = (operand & 64) != 0;
                } else {
                    int operand = getIndirectAddress(operandAddress, 0, 0);

                    flagZero = (registerA & operand) == 0;
                    flagSign = (operand & 32768) != 0;
                    flagOverflow = (operand & 16384) != 0;
                }
                break;
            case BIT_IMM:
                if(eightBitAccumulator) {
                    flagZero = ((registerA&255) & readMemory(operandAddress)) == 0;
                } else {
                    flagZero = (registerA & getIndirectAddress(operandAddress, 0, 0)) == 0;

                    setProgramCounter(programCounter + 1);
                }
                break;
            case INC_ZP:
            case INC_ABS:
            case INC_ZP_X:
            case INC_ABS_X:
                if(eightBitAccumulator) {
                    int operand = readMemory(operandAddress);

                    ++operand;
                    operand &= 255;

                    flagSign = (operand & 128) != 0;
                    flagZero = operand == 0;

                    writeMemory(operandAddress, operand);
                } else {
                    int operand = getIndirectAddress(operandAddress, 0, 0);

                    ++operand;
                    operand &= 65535;

                    flagSign = (operand & 32768) != 0;
                    flagZero = operand == 0;

                    writeMemory(operandAddress, operand);
                    writeMemory(operandAddress+1, operand >>> 8);
                }
                break;
            case INC_A:
                if(eightBitAccumulator) {
                    registerA = (registerA & 0xFF00) | (((registerA&255)+1)&255);
                    setSignZero8(registerA);
                } else {
                    ++registerA;
                    registerA &= 65535;
                    setSignZero16(registerA);
                }
                break;
            case INX:
                if(eightBitIndexRegs) {
                    ++registerX;
                    registerX &= 255;
                    setSignZero8(registerX);
                } else {
                    ++registerX;
                    registerX &= 65535;
                    setSignZero16(registerX);
                }
                break;
            case INY:
                if(eightBitIndexRegs) {
                    ++registerY;
                    registerY &= 255;
                    setSignZero8(registerY);
                } else {
                    ++registerY;
                    registerY &= 65535;
                    setSignZero16(registerY);
                }
                break;
            case DEC_ZP:
            case DEC_ZP_X:
            case DEC_ABS:
            case DEC_ABS_X:
                if(eightBitAccumulator) {
                    int operand = readMemory(operandAddress);

                    --operand;
                    operand &= 255;

                    flagSign = (operand & 128) != 0;
                    flagZero = operand == 0;

                    writeMemory(operandAddress, operand);
                } else {
                    int operand = getIndirectAddress(operandAddress, 0, 0);

                    --operand;
                    operand &= 65535;

                    flagSign = (operand & 32768) != 0;
                    flagZero = operand == 0;

                    writeMemory(operandAddress, operand);
                    writeMemory(operandAddress+1, operand >>> 8);
                }
                break;
            case DEC_A:
                if(eightBitAccumulator) {
                    registerA = (registerA & 0xFF00) | (((registerA&255)-1)&255);
                    setSignZero8(registerA);
                } else {
                    --registerA;
                    registerA &= 65535;
                    setSignZero16(registerA);
                }
                break;
            case DEX:
                if(eightBitIndexRegs) {
                    --registerX;
                    registerX &= 255;
                    setSignZero8(registerX);
                } else {
                    --registerX;
                    registerX &= 65535;
                    setSignZero16(registerX);
                }
                break;
            case DEY:
                if(eightBitIndexRegs) {
                    --registerY;
                    registerY &= 255;
                    setSignZero8(registerY);
                } else {
                    --registerY;
                    registerY &= 65535;
                    setSignZero16(registerY);
                }
                break;
            case PHP:
                pushFlags(false);
                break;
            case PLP:
                popFlags();
                break;
            case PHA:
                if(eightBitAccumulator) {
                    pushPStack(registerA);
                } else {
                    pushPStack(registerA >>> 8);
                    pushPStack(registerA);
                }
                break;
            case PLA:
                if(eightBitAccumulator) {
                    registerA = (registerX&0xFF00) | pullPStack();
                    setSignZero8(registerA);
                } else {
                    int regALB = pullPStack();
                    int regAHB = pullPStack();

                    registerA = to16Bit(regAHB, regALB);
                    setSignZero16(registerA);
                }
                break;
            case PHY:
                if(eightBitIndexRegs) {
                    pushPStack(registerY);
                } else {
                    pushPStack(registerY >>> 8);
                    pushPStack(registerY);
                }
                break;
            case PLY:
                if(eightBitIndexRegs) {
                    registerY = pullPStack();
                    setSignZero8(registerY);
                } else {
                    int regYLB = pullPStack();
                    int regYHB = pullPStack();

                    registerY = to16Bit(regYHB, regYLB);
                    setSignZero16(registerY);
                }
                break;
            case PHX:
                if(eightBitIndexRegs) {
                    pushPStack(registerX);
                } else {
                    pushPStack(registerX >>> 8);
                    pushPStack(registerX);
                }
                break;
            case PLX:
                if(eightBitIndexRegs) {
                    registerX = pullPStack();
                    setSignZero8(registerX);
                } else {
                    int regXLB = pullPStack();
                    int regXHB = pullPStack();

                    registerX = to16Bit(regXHB, regXLB);
                    setSignZero16(registerX);
                }
                break;
            case PHD:
                pushPStack(registerD >>> 8);
                pushPStack(registerD);
                break;
            case PLD:
            {
                int regDLB = pullPStack();
                int regDHB = pullPStack();

                registerD = to16Bit(regDHB, regDLB);
                setSignZero16(registerD);
            }
            break;
            case RHI:
                pushRStack(registerI >>> 8);
                pushRStack(registerI);
                break;
            case RLI:
            {
                int regILB = pullRStack();
                int regIHB = pullRStack();

                registerI = to16Bit(regIHB, regILB);
                setSignZero16(registerI);
            }
            break;
            case RHX:
                if(eightBitIndexRegs) {
                    pushRStack(registerX);
                } else {
                    pushRStack(registerX >>> 8);
                    pushRStack(registerX);
                }
                break;
            case RLX:
                if(eightBitIndexRegs) {
                    registerX = pullRStack();
                    setSignZero8(registerX);
                } else {
                    int regXLB = pullRStack();
                    int regXHB = pullRStack();

                    registerX = to16Bit(regXHB, regXLB);
                    setSignZero16(registerX);
                }
                break;
            case RHA:
                if(eightBitAccumulator) {
                    pushRStack(registerA);
                } else {
                    pushRStack(registerA >>> 8);
                    pushRStack(registerA);
                }
                break;
            case RLA:
                if(eightBitAccumulator) {
                    registerA = (registerA&0xFF00) | pullRStack();
                    setSignZero8(registerA);
                } else {
                    int regALB = pullRStack();
                    int regAHB = pullRStack();

                    registerA = to16Bit(regAHB, regALB);
                    setSignZero16(registerA);
                }
                break;
            case RHY:
                if(eightBitIndexRegs) {
                    pushRStack(registerY);
                } else {
                    pushRStack(registerY >>> 8);
                    pushRStack(registerY);
                }
                break;
            case RLY:
                if(eightBitIndexRegs) {
                    registerY = pullRStack();
                    setSignZero8(registerY);
                } else {
                    int regYLB = pullRStack();
                    int regYHB = pullRStack();

                    registerY = to16Bit(regYHB, regYLB);
                    setSignZero16(registerY);
                }
                break;
            case LDA_IMM:
                if(!eightBitAccumulator) {setProgramCounter(programCounter + 1);}
            case LDA_ABS:
            case LDA_ABS_X:
            case LDA_ABS_Y:
            case LDA_ZP:
            case LDA_ZP_X:
            case LDA_IND:
            case LDA_IND_X:
            case LDA_IND_Y:
            case LDA_STK:
            case LDA_RSTK:
            case LDA_STK_Y:
            case LDA_RSTK_Y:
                if(eightBitAccumulator) {
                    registerA = (registerX&0xFF00) | readMemory(operandAddress);
                    setSignZero8(registerA);
                } else {
                    registerA = getIndirectAddress(operandAddress, 0, 0);
                    setSignZero16(registerA);
                }
                break;
            case LDX_IMM:
                if(!eightBitIndexRegs) {setProgramCounter(programCounter + 1);}
            case LDX_ZP:
            case LDX_ZP_Y:
            case LDX_ABS:
            case LDX_ABS_Y:
                if(eightBitIndexRegs) {
                    registerX = readMemory(operandAddress);
                    setSignZero8(registerX);
                } else {
                    registerX = getIndirectAddress(operandAddress, 0, 0);
                    setSignZero16(registerX);
                }
                break;
            case LDY_IMM:
                if(!eightBitIndexRegs) {setProgramCounter(programCounter + 1);}
            case LDY_ZP:
            case LDY_ZP_X:
            case LDY_ABS:
            case LDY_ABS_X:
                if(eightBitIndexRegs) {
                    registerY = readMemory(operandAddress);
                    setSignZero8(registerY);
                } else {
                    registerY = getIndirectAddress(operandAddress, 0, 0);
                    setSignZero16(registerY);
                }
                break;
            case STA_ZP:
            case STA_ZP_X:
            case STA_ABS:
            case STA_ABS_X:
            case STA_ABS_Y:
            case STA_IND:
            case STA_IND_X:
            case STA_IND_Y:
            case STA_STK:
            case STA_RSTK:
            case STA_STK_Y:
            case STA_RSTK_Y:
                if(eightBitAccumulator) {
                    writeMemory(operandAddress, registerA);
                } else {
                    writeMemory(operandAddress, registerA);
                    writeMemory(operandAddress+1, registerA >>> 8);
                }
                break;
            case STX_ZP:
            case STX_ZP_Y:
            case STX_ABS:
                if(eightBitIndexRegs) {
                    writeMemory(operandAddress, registerX);
                } else {
                    writeMemory(operandAddress, registerX);
                    writeMemory(operandAddress+1, registerX >>> 8);
                }
                break;
            case STY_ZP:
            case STY_ZP_X:
            case STY_ABS:
                if(eightBitIndexRegs) {
                    writeMemory(operandAddress, registerY);
                } else {
                    writeMemory(operandAddress, registerY);
                    writeMemory(operandAddress+1, registerY >>> 8);
                }
                break;
            case STZ_ZP:
            case STZ_ZP_X:
            case STZ_ABS:
            case STZ_ABS_X:
                if(eightBitAccumulator) {
                    writeMemory(operandAddress, 0);
                } else {
                    writeMemory(operandAddress, 0);
                    writeMemory(operandAddress+1, 0);
                }
                break;
            case TXI:
                registerI = registerX;
                setSignZero16(registerI);
                break;
            case TIX:
                if(eightBitIndexRegs) {
                    registerX = registerI & 255;
                    setSignZero8(registerX);
                } else {
                    registerX = registerI;
                    setSignZero16(registerX);
                }
                break;
            case TDA:
                if(eightBitAccumulator) {
                    registerA = (registerX&0xFF00) | (registerD&255);
                    setSignZero8(registerA);
                } else {
                    registerA = registerD;
                    setSignZero16(registerA);
                }
                break;
            case TAD:
                registerD = registerA;
                setSignZero16(registerD);
                break;
            case TXA:
                if(eightBitAccumulator) {
                    registerA = (registerA&0xFF00) | (registerX&255);
                    setSignZero8(registerA);
                } else {
                    registerA = registerX;
                    setSignZero16(registerA);
                }
                break;
            case TYA:
                if(eightBitAccumulator) {
                    registerA = (registerA&0xFF00) | (registerY&255);
                    setSignZero8(registerA);
                } else {
                    registerA = registerY;
                    setSignZero16(registerA);
                }
                break;
            case TAY:
                if(eightBitAccumulator) {
                    registerY = registerA&255;
                    if(eightBitIndexRegs) {
                        setSignZero8(registerY);
                    } else {
                        setSignZero16(registerY);
                    }
                } else {
                    if(eightBitIndexRegs) {
                        registerY = registerA&255;
                        setSignZero8(registerY);
                    } else {
                        registerY = registerA;
                        setSignZero16(registerY);
                    }
                }
                break;
            case TAX:
                if(eightBitAccumulator) {
                    registerX = registerA&255;
                    if(eightBitIndexRegs) {
                        setSignZero8(registerX);
                    } else {
                        setSignZero16(registerX);
                    }
                } else {
                    if(eightBitIndexRegs) {
                        registerX = registerA&255;
                        setSignZero8(registerX);
                    } else {
                        registerX = registerA;
                        setSignZero16(registerX);
                    }
                }
                break;
            case TXY:
                registerY = registerX;
                if(eightBitIndexRegs) {
                    setSignZero8(registerY);
                } else {
                    setSignZero16(registerY);
                }
                break;
            case TYX:
                registerX = registerY;
                if(eightBitIndexRegs) {
                    setSignZero8(registerX);
                } else {
                    setSignZero16(registerX);
                }
                break;
            case TXS:
                pStackPointer = registerX&255;
                setSignZero8(pStackPointer);
                break;
            case TSX:
                if(eightBitIndexRegs) {
                    registerX = pStackPointer;
                    setSignZero8(registerX);
                } else {
                    registerX = pStackPointer + 256;
                    setSignZero16(registerX);
                }
                break;
            case TXR:
                rStackPointer = registerX&255;
                setSignZero8(rStackPointer);
                break;
            case TRX:
                if(eightBitIndexRegs) {
                    registerX = rStackPointer;
                    setSignZero8(registerX);
                } else {
                    registerX = rStackPointer + 512;
                    setSignZero16(registerX);
                }
                break;
            case BPL_REL:
                if(!flagSign) {
                    int newProgramCounter = programCounter + ((byte) readMemory(operandAddress));

                    if((programCounter&0xFF00) != (newProgramCounter&0xFF00)) {++cyclesElapsed;}
                    setProgramCounter(newProgramCounter);
                    ++cyclesElapsed;
                }
                break;
            case BMI_REL:
                if(flagSign) {
                    int newProgramCounter = programCounter + ((byte) readMemory(operandAddress));

                    if((programCounter&0xFF00) != (newProgramCounter&0xFF00)) {++cyclesElapsed;}
                    setProgramCounter(newProgramCounter);
                    ++cyclesElapsed;
                }
                break;
            case BVC_REL:
                if(!flagOverflow) {
                    int newProgramCounter = programCounter + ((byte) readMemory(operandAddress));

                    if((programCounter&0xFF00) != (newProgramCounter&0xFF00)) {++cyclesElapsed;}
                    setProgramCounter(newProgramCounter);
                    ++cyclesElapsed;
                }
                break;
            case BVS_REL:
                if(flagOverflow) {
                    int newProgramCounter = programCounter + ((byte) readMemory(operandAddress));

                    if((programCounter&0xFF00) != (newProgramCounter&0xFF00)) {++cyclesElapsed;}
                    setProgramCounter(newProgramCounter);
                    ++cyclesElapsed;
                }
                break;
            case BCC_REL:
                if(!flagCarry) {
                    int newProgramCounter = programCounter + ((byte) readMemory(operandAddress));

                    if((programCounter&0xFF00) != (newProgramCounter&0xFF00)) {++cyclesElapsed;}
                    setProgramCounter(newProgramCounter);
                    ++cyclesElapsed;
                }
                break;
            case BCS_REL:
                if(flagCarry) {
                    int newProgramCounter = programCounter + ((byte) readMemory(operandAddress));

                    if((programCounter&0xFF00) != (newProgramCounter&0xFF00)) {++cyclesElapsed;}
                    setProgramCounter(newProgramCounter);
                    ++cyclesElapsed;
                }
                break;
            case BNE_REL:
                if(!flagZero) {
                    int newProgramCounter = programCounter + ((byte) readMemory(operandAddress));

                    if((programCounter&0xFF00) != (newProgramCounter&0xFF00)) {++cyclesElapsed;}
                    setProgramCounter(newProgramCounter);
                    ++cyclesElapsed;
                }
                break;
            case BEQ_REL:
                if(flagZero) {
                    int newProgramCounter = programCounter + ((byte) readMemory(operandAddress));

                    if((programCounter&0xFF00) != (newProgramCounter&0xFF00)) {++cyclesElapsed;}
                    setProgramCounter(newProgramCounter);
                    ++cyclesElapsed;
                }
                break;
            case BRA_REL:
            {
                int newProgramCounter = programCounter + ((byte) readMemory(operandAddress));

                if((programCounter&0xFF00) != (newProgramCounter&0xFF00)) {++cyclesElapsed;}
                setProgramCounter(newProgramCounter);
                ++cyclesElapsed;
            }
            break;
            case CMP_IMM:
                if(!eightBitAccumulator) {setProgramCounter(programCounter + 1);}
            case CMP_ZP:
            case CMP_ZP_X:
            case CMP_ABS:
            case CMP_ABS_X:
            case CMP_ABS_Y:
            case CMP_IND:
            case CMP_IND_X:
            case CMP_IND_Y:
            case CMP_STK:
            case CMP_RSTK:
            case CMP_STK_Y:
            case CMP_RSTK_Y:
                if(eightBitAccumulator) {
                    performAddition(registerA, operandAddress, false, true, true);
                } else {
                    performAddition(registerA, operandAddress, false, false, true);
                }
                break;
            case CPX_IMM:
                if(!eightBitIndexRegs) {setProgramCounter(programCounter + 1);}
            case CPX_ZP:
            case CPX_ABS:
                if(eightBitIndexRegs) {
                    performAddition(registerX, operandAddress, false, true, true);
                } else {
                    performAddition(registerX, operandAddress, false, false, true);
                }
                break;
            case CPY_IMM:
                if(!eightBitIndexRegs) {setProgramCounter(programCounter + 1);}
            case CPY_ZP:
            case CPY_ABS:
                if(eightBitIndexRegs) {
                    performAddition(registerY, operandAddress, false, true, true);
                } else {
                    performAddition(registerY, operandAddress, false, false, true);
                }
                break;
            case JSR_ABS:
            case JSR_ABS_X:
                pushPStack(programCounter+2 >>> 8);
                pushPStack(programCounter+2);

                setProgramCounter(operandAddress);
                break;
            case RTS:
            {
                int programCounterLB = pullPStack();
                int programCounterHB = pullPStack();

                setProgramCounter(to16Bit(programCounterHB, programCounterLB)+1);
            }
            break;
            case JMP_ABS:
            case JMP_ABS_X:
            case JMP_IND:
                setProgramCounter(operandAddress);
                break;
            case CLC:
                flagCarry = false;
                break;
            case SEC:
                flagCarry = true;
                break;
            case CLI:
                flagInterrupt = false;
                break;
            case SEI:
                flagInterrupt = true;
                break;
            case CLV:
                flagOverflow = false;
                break;
            case SED:
                flagDecimal = true;
                break;
            case CLD:
                flagDecimal = false;
                break;
            case PEA_ABS:
            {
                int operand = getIndirectAddress(operandAddress, 0, 0);

                pushPStack(operand >>> 8);
                pushPStack(operand);
            }
            break;
            case REA_ABS:
            {
                int operand = getIndirectAddress(operandAddress, 0, 0);

                pushRStack(operand >>> 8);
                pushRStack(operand);
            }
            break;
            case PEI_IND:
            {
                int operand = getIndirectAddress(operandAddress, 0, 0);

                pushPStack(operand >>> 8);
                pushPStack(operand);
            }
            break;
            case REI_IND:
            {
                int operand = getIndirectAddress(operandAddress, 0, 0);

                pushRStack(operand >>> 8);
                pushRStack(operand);
            }
            break;
            case PER_REL:
            {
                int operand = getIndirectAddress(programCounter+2, (byte) readMemory(operandAddress), 0);

                pushPStack(operand >>> 8);
                pushPStack(operand);
            }
            break;
            case RER_REL:
            {
                int operand = getIndirectAddress(programCounter+2, (byte) readMemory(operandAddress), 0);

                pushRStack(operand >>> 8);
                pushRStack(operand);
            }
            break;
            case REP_IMM:
            {
                int operand = readMemory(operandAddress);

                flagCarry &= (operand & 1) == 0;
                flagZero &= (operand & 2) == 0;
                flagInterrupt &= (operand & 4) == 0;
                flagDecimal &= (operand & 8) == 0;
                flagBreak &= (operand & 16) == 0;
                flagAccumulator &= (operand & 32) == 0;
                flagOverflow &= (operand & 64) == 0;
                flagSign &= (operand & 128) == 0;
            }
            break;
            case SEP_IMM:
            {
                int operand = readMemory(operandAddress);

                flagCarry |= (operand & 1) != 0;
                flagZero |= (operand & 2) != 0;
                flagInterrupt |= (operand & 4) != 0;
                flagDecimal |= (operand & 8) != 0;
                flagBreak |= (operand & 16) != 0;
                flagAccumulator |= (operand & 32) != 0;
                flagOverflow |= (operand & 64) != 0;
                flagSign |= (operand & 128) != 0;
            }
            break;
            case ZEA:
                if(eightBitAccumulator) {registerA &= 255;}
                registerD = 0;
                break;
            case SEA:
                if(eightBitAccumulator) {registerA |= 0xFF00;}
                registerD = (registerA&32768) == 0 ? 0 : 65535;
                break;
            case XCE:
            {
                boolean tempCarry = flagEmulate;
                flagEmulate = flagCarry;
                flagCarry = tempCarry;
            }
            break;
            case MMU:
                switch(readMemory(operandAddress)) {
                    case 0:
                        redbusDevice = registerA&255;
                        break;
                    case 128:
                        if(eightBitAccumulator) {
                            registerA = (registerA&0xFF00) | redbusDevice;
                        } else {
                            registerA = redbusDevice;
                        }
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
                        windowOffset = registerA;
                        break;
                    case 131:
                        registerA = windowOffset;
                        break;
                    case 4:
                        windowEnabled = true;
                        break;
                    case 132:
                        windowEnabled = false;
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
                    case 135:
                        break;
                    case 255:
                        //System.out.println(toString());
                        break;
                }
                break;
            case MUL_ZP:
            case MUL_ZP_X:
            case MUL_ABS:
            case MUL_ABS_X:
            {
                int operand = getIndirectAddress(operandAddress, 0, 0);
                int result = ((short) registerA) * ((short) operand);

                flagSign = result < 0;
                flagZero = result == 0;
                flagOverflow = (registerA&0x8000) == (operand&0x8000) && (operand&0x8000) != (result&0x80000000);

                registerD = result >>> 16;
                registerA = result & 65535;
            }
            break;
            case DIV_ZP:
            case DIV_ZP_X:
            case DIV_ABS:
            case DIV_ABS_X:
            {
                int dividend = (registerD << 16) | registerA;
                int divisor = getIndirectAddress(operandAddress, 0, 0);
                if(divisor == 0) {divisor = 1;}

                registerA = (dividend / ((short) divisor))&65535;
                registerD = (dividend % ((short) divisor))&65535;

                flagSign = (registerA&0x8000) != 0;
                flagZero = registerA == 0;
                flagOverflow = ((dividend>>>16)&0x8000) == (divisor&0x8000) && (divisor&0x8000) != (registerA&0x8000);
            }
            break;
            case NXA:
                if(eightBitAccumulator) {
                    registerA = (registerA&0xFF00) | readMemory(registerI);
                    ++registerI;
                    registerI &= 65535;
                } else {
                    registerA = getIndirectAddress(registerI, 0, 0);
                    registerI += 2;
                    registerI &= 65535;
                }
                break;
            case NXT:
                setProgramCounter(getIndirectAddress(registerI, 0, 0));
                registerI += 2;
                break;
            case ENT:
                pushRStack(registerI >>> 8);
                pushRStack(registerI);

                registerI = (programCounter+3) & 65535;
                setProgramCounter(operandAddress);
                break;
            case UNUSED:
                io.halt();
                break;
        }

        setProgramCounter(programCounter + instruction.getLength());
        cyclesElapsed += instruction.getNumCycles();
    }

    public void copyArrayIntoMemory(byte[] data, int index, int length) {
        if(data.length < length) {length = data.length;}
        if(index+length > memory.length) {length = memory.length-index;}
        System.arraycopy(data, 0, memory, index, length);
    }

    public int readMemory(int index) {
        if(redbusEnabled && (index >= redbusOffset) && (index < redbusOffset+256)) {
            return io.readNetwork(redbusDevice, index-redbusOffset);
        }
        return memory[index&0x0FFFF] & 255;
    }

    private void writeMemory(int index, int value) {
        if(redbusEnabled && (index >= redbusOffset) && (index < redbusOffset+256)) {
            io.writeNetwork(redbusDevice, index-redbusOffset, value & 255);
        } else {
            memory[index&0x0FFFF] = (byte) value;
        }
    }

    private int getOperandLocation(AddressingMode mode) {
        switch(mode) {
            case IMPLIED:
                return programCounter;
            case RELATIVE:case IMMEDIATE:
                return programCounter+1;
            case ZERO_PAGE:
                return readMemory(programCounter+1);
            case ZERO_PAGE_X:
                return readMemory(programCounter+1)+registerX; //& 255;
            case ZERO_PAGE_Y:
                return readMemory(programCounter+1)+registerY; //& 255;
            case ABSOLUTE:
                return getIndirectAddress(programCounter, 1, 0);
            case ABSOLUTE_X:
                return getIndirectAddress(programCounter, 1, registerX);
            case ABSOLUTE_Y:
                return getIndirectAddress(programCounter, 1, registerY);
            case INDIRECT:
                return getIndirectAddress(readMemory(programCounter+1), 0, 0);
            case INDIRECT_16:
                return getIndirectAddress(getIndirectAddress(programCounter, 1, 0), 0, 0);
            case INDIRECT_INDEXED:
                return getIndirectAddress(readMemory(programCounter+1), 0, registerY);
            case INDEXED_INDIRECT:
                return getIndirectAddress(readMemory(programCounter+1)+registerX & 255, 0, 0);
            case PUSH_P_STACK:case POP_P_STACK:
                return 256 + pStackPointer;
            case PUSH_R_STACK:case POP_R_STACK:
                return 512 + rStackPointer;
            case P_STACK_INDEXED:
                return 256 + ((pStackPointer+readMemory(programCounter+1)) & 255);
            case R_STACK_INDEXED:
                return 512 + ((rStackPointer+readMemory(programCounter+1)) & 255);
            case P_STACK_INDIRECT:
                return getIndirectAddress(256, (pStackPointer-readMemory(programCounter+1)) & 255, registerY);
            case R_STACK_INDIRECT:
                return getIndirectAddress(512, (rStackPointer-readMemory(programCounter+1)) & 255, registerY);
            default:
                return 0;
        }
    }

    public void writeToNBT(NBTTagCompound data) {
        NBTTagCompound emulatorData = new NBTTagCompound();

        emulatorData.setInteger("registerA", registerA);
        emulatorData.setInteger("registerX", registerX);
        emulatorData.setInteger("registerY", registerY);
        emulatorData.setInteger("registerI", registerI);
        emulatorData.setInteger("registerD", registerD);
        emulatorData.setInteger("pStackPointer", pStackPointer);
        emulatorData.setInteger("rStackPointer", rStackPointer);
        emulatorData.setInteger("programCounter", programCounter);
        emulatorData.setInteger("cyclesElapsed", cyclesElapsed);
        emulatorData.setInteger("redbusDevice", redbusDevice);
        emulatorData.setInteger("redbusOffset", redbusOffset);
        emulatorData.setInteger("windowOffset", windowOffset);
        emulatorData.setInteger("addressBRK", addressBRK);
        emulatorData.setInteger("addressPOR", addressPOR);

        emulatorData.setBoolean("flagCarry", flagCarry);
        emulatorData.setBoolean("flagZero", flagZero);
        emulatorData.setBoolean("flagInterrupt", flagInterrupt);
        emulatorData.setBoolean("flagDecimal", flagDecimal);
        emulatorData.setBoolean("flagBreak", flagBreak);
        emulatorData.setBoolean("flagAccumulator", flagAccumulator);
        emulatorData.setBoolean("flagOverflow", flagOverflow);
        emulatorData.setBoolean("flagSign", flagSign);
        emulatorData.setBoolean("flagEmulate", flagEmulate);
        emulatorData.setBoolean("redbusEnabled", redbusEnabled);
        emulatorData.setBoolean("windowEnabled", windowEnabled);

        emulatorData.setByteArray("memory", memory);

        data.setTag("emulatorData", emulatorData);
    }

    public void readFromNBT(NBTTagCompound data) {
        NBTTagCompound emulatorData = (NBTTagCompound) data.getTag("emulatorData");

        registerA = emulatorData.getInteger("registerA");
        registerX = emulatorData.getInteger("registerX");
        registerY = emulatorData.getInteger("registerY");
        registerI = emulatorData.getInteger("registerI");
        registerD = emulatorData.getInteger("registerD");
        pStackPointer = emulatorData.getInteger("pStackPointer");
        rStackPointer = emulatorData.getInteger("rStackPointer");
        programCounter = emulatorData.getInteger("programCounter");
        cyclesElapsed = emulatorData.getInteger("cyclesElapsed");
        redbusDevice = emulatorData.getInteger("redbusDevice");
        redbusOffset = emulatorData.getInteger("redbusOffset");
        windowOffset = emulatorData.getInteger("windowOffset");
        addressBRK = emulatorData.getInteger("addressBRK");
        addressPOR = emulatorData.getInteger("addressPOR");

        flagCarry = emulatorData.getBoolean("flagCarry");
        flagZero = emulatorData.getBoolean("flagZero");
        flagInterrupt = emulatorData.getBoolean("flagInterrupt");
        flagDecimal = emulatorData.getBoolean("flagDecimal");
        flagBreak = emulatorData.getBoolean("flagBreak");
        flagAccumulator = emulatorData.getBoolean("flagAccumulator");
        flagOverflow = emulatorData.getBoolean("flagOverflow");
        flagSign = emulatorData.getBoolean("flagSign");
        flagEmulate = emulatorData.getBoolean("flagEmulate");
        redbusEnabled = emulatorData.getBoolean("redbusEnabled");
        windowEnabled = emulatorData.getBoolean("windowEnabled");

        memory = emulatorData.getByteArray("memory");
    }

    private static int to16Bit(int a, int b) {
        return (a << 8) | b;
    }

    private int getIndirectAddress(int a, int b, int c) {
        return to16Bit(readMemory(a+b+1), readMemory(a+b)) + c;
    }

    private static int binaryToBCD(int value) {
        String asString = String.valueOf(value);

        int returnValue = 0;
        for(int i=Math.min(7,asString.length()-1);i>=0;--i) {
            returnValue |= Character.getNumericValue(asString.charAt(i)) << (i * 4);
        }
        return returnValue;
    }

    private static int BCDToBinary(int value) {
        int length = (int) (((32 - Integer.numberOfLeadingZeros(value)) / 4.0) + 0.76);

        String asString = "";
        for(int i=length-1;i>=0;--i) {
            char digit = Character.forDigit((value >> (i * 4)) & 0xF, 10);
            if(digit == '\0') {digit = '9';}
            asString = digit + asString;
        }
        return Integer.parseInt(asString);
    }

    private int performAddition(int a, int addressOfB, boolean subtract, boolean eightBit, boolean compare) {
        boolean compareOrSubtract = compare || subtract;
        if(flagDecimal) {++cyclesElapsed;}

        if(eightBit) {
            a &= 255;
            int b = readMemory(addressOfB);
            if(flagDecimal) {
                a = BCDToBinary(a);
                b = BCDToBinary(b);
            }

            b = (compare || flagCarry ? 1 : 0) + (compareOrSubtract ? (~b)&255 : b);
            int result = a + b;

            if(!compare) {flagOverflow = (a&128) == (b&128) && (b&128) != (result&128);}
            flagCarry = (result & 256) != 0;
            setSignZero8(result);

            result &= 255;
            if(flagDecimal) {result = binaryToBCD(result) & 255;}
            return result;
        } else {
            int b = getIndirectAddress(addressOfB, 0, 0);
            if(flagDecimal) {
                a = BCDToBinary(a);
                b = BCDToBinary(b);
            }

            b = (compare || flagCarry ? 1 : 0) + (compareOrSubtract ? (~b)&65535 : b);
            int result = a + b;

            if(!compare) {flagOverflow = (a&32768) == (b&32768) && (b&32768) != (result&32768);}
            flagCarry = (result & 65536) != 0;
            setSignZero16(result);

            result &= 65535;
            if(flagDecimal) {result = binaryToBCD(result) & 65535;}
            return result;
        }
    }

    private void setSignZero8(int value) {
        value &= 255;

        flagSign = (value & 128) != 0;
        flagZero = value == 0;
    }

    private void setSignZero16(int value) {
        value &= 65535;

        flagSign = (value & 32768) != 0;
        flagZero = value == 0;
    }

    private void pushFlags(boolean setBreak) {
        int serializedFlags = flagCarry ? 1 : 0;
        serializedFlags += flagZero ? 2 : 0;
        serializedFlags += flagInterrupt ? 4 : 0;
        serializedFlags += flagDecimal ? 8 : 0;
        serializedFlags += flagBreak || setBreak ? 16 : 0;
        serializedFlags += flagAccumulator ? 32 : 0;
        serializedFlags += flagOverflow ? 64 : 0;
        serializedFlags += flagSign ? 128 : 0;
        pushPStack(serializedFlags);
    }

    private void popFlags() {
        int serializedFlags = pullPStack();
        flagCarry = (serializedFlags & 1) != 0;
        flagZero = (serializedFlags & 2) != 0;
        flagInterrupt = (serializedFlags & 4) != 0;
        flagDecimal = (serializedFlags & 8) != 0;
        flagBreak = (serializedFlags & 16) != 0;
        flagAccumulator = (serializedFlags & 32) != 0;
        flagOverflow = (serializedFlags & 64) != 0;
        flagSign = (serializedFlags & 128) != 0;
    }

    private void pushPStack(int value) {
        // This is the correct implementation, however to match RP Control we need to make it incorrect
        //memory[256 + pStackPointer] = (byte) value;
        //pStackPointer--;
        //pStackPointer &= 255;

        pStackPointer--;
        pStackPointer &= 255;
        memory[256 + pStackPointer] = (byte) value;
    }

    private void pushRStack(int value) {
        // This is the correct implementation, however to match RP Control we need to make it incorrect
        //memory[512 + rStackPointer] = (byte) value;
        //rStackPointer--;
        //rStackPointer &= 255;

        rStackPointer--;
        rStackPointer &= 255;
        memory[512 + rStackPointer] = (byte) value;
    }

    private int pullPStack() {
        // This is the correct implementation, however to match RP Control we need to make it incorrect
        //pStackPointer++;
        //pStackPointer &= 255;
        //return memory[256 + pStackPointer] & 255;

        int value = memory[256 + pStackPointer] & 255;
        pStackPointer++;
        pStackPointer &= 255;
        return value;
    }

    private int pullRStack() {
        // This is the correct implementation, however to match RP Control we need to make it incorrect
        //rStackPointer++;
        //rStackPointer &= 255;
        //return memory[512 + rStackPointer] & 255;

        int value = memory[512 + rStackPointer] & 255;
        rStackPointer++;
        rStackPointer &= 255;
        return value;
    }

}
