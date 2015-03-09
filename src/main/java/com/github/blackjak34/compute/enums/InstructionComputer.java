package com.github.blackjak34.compute.enums;

import static com.github.blackjak34.compute.enums.AddressingMode.*;

import java.util.HashMap;

/*
 * All branches have a listed cycle number of two; branches require three cycles if they are taken and four cycles if
 * they are taken and cross a page boundary.
 * ADC and SBC take an extra cycle when in decimal mode.
 */
public enum InstructionComputer {
    // Length is one extra on purpose; part of function
    BRK			(0x00, 2, 7, IMPLIED),
    ORA_IND_X	(0x01, 2, 6, INDEXED_INDIRECT),
    // This instruction changes the PC
    NXT			(0x02, 0, 3, IMPLIED),
    ORA_STK		(0x03, 2, 4, P_STACK_INDEXED),
    TSB_ZP		(0x04, 2, 5, ZERO_PAGE),
    ORA_ZP		(0x05, 2, 3, ZERO_PAGE),
    ASL_ZP		(0x06, 2, 5, ZERO_PAGE),
    ORA_RSTK	(0x07, 2, 4, R_STACK_INDEXED),
    PHP			(0x08, 1, 3, PUSH_P_STACK),
    ORA_IMM		(0x09, 2, 2, IMMEDIATE),
    ASL_A		(0x0A, 1, 2, IMPLIED),
    RHI			(0x0B, 1, 3, PUSH_R_STACK),
    TSB_ABS		(0x0C, 3, 6, ABSOLUTE),
    ORA_ABS		(0x0D, 3, 4, ABSOLUTE),
    ASL_ABS		(0x0E, 3, 6, ABSOLUTE),
    MUL_ZP		(0x0F, 2, 7, ZERO_PAGE),
    // This instruction changes the PC
    BPL_REL		(0x10, 2, 2, RELATIVE),
    ORA_IND_Y	(0x11, 2, 5, INDIRECT_INDEXED),
    ORA_IND		(0x12, 2, 5, INDIRECT),
    // This instruction is indirect
    ORA_STK_Y	(0x13, 2, 7, P_STACK_INDIRECT),
    TRB_ZP		(0x14, 2, 5, ZERO_PAGE),
    ORA_ZP_X	(0x15, 2, 4, ZERO_PAGE_X),
    ASL_ZP_X	(0x16, 2, 6, ZERO_PAGE_X),
    // This instruction is indirect
    ORA_RSTK_Y	(0x17, 2, 7, R_STACK_INDIRECT),
    CLC			(0x18, 1, 2, IMPLIED),
    ORA_ABS_Y	(0x19, 3, 4, ABSOLUTE_Y),
    INC_A		(0x1A, 1, 2, IMPLIED),
    RHX			(0x1B, 1, 3, PUSH_R_STACK),
    TRB_ABS		(0x1C, 3, 6, ABSOLUTE),
    ORA_ABS_X	(0x1D, 3, 4, ABSOLUTE_X),
    ASL_ABS_X	(0x1E, 3, 6, ABSOLUTE_X),
    MUL_ZP_X	(0x1F, 2, 8, ZERO_PAGE_X),
    // This instruction changes the PC
    JSR_ABS		(0x20, 0, 6, ABSOLUTE),
    AND_IND_X	(0x21, 2, 6, INDEXED_INDIRECT),
    // This instruction changes the PC
    ENT			(0x22, 0, 5, ABSOLUTE),
    AND_STK		(0x23, 2, 4, P_STACK_INDEXED),
    BIT_ZP		(0x24, 2, 3, ZERO_PAGE),
    AND_ZP		(0x25, 2, 3, ZERO_PAGE),
    ROL_ZP		(0x26, 2, 5, ZERO_PAGE),
    AND_RSTK	(0x27, 2, 4, R_STACK_INDEXED),
    PLP			(0x28, 1, 4, POP_P_STACK),
    AND_IMM		(0x29, 2, 2, IMMEDIATE),
    ROL_A		(0x2A, 1, 2, IMPLIED),
    RLI			(0x2B, 1, 4, POP_R_STACK),
    BIT_ABS		(0x2C, 3, 4, ABSOLUTE),
    AND_ABS		(0x2D, 3, 4, ABSOLUTE),
    ROL_ABS		(0x2E, 3, 6, ABSOLUTE),
    MUL_ABS		(0x2F, 3, 8, ABSOLUTE),
    // This instruction changes the PC
    BMI_REL		(0x30, 2, 2, RELATIVE),
    AND_IND_Y	(0x31, 2, 5, INDIRECT_INDEXED),
    AND_IND		(0x32, 2, 5, INDIRECT),
    // This instruction is indirect
    AND_STK_Y	(0x33, 2, 7, P_STACK_INDIRECT),
    BIT_ZP_X	(0x34, 2, 4, ZERO_PAGE_X),
    AND_ZP_X	(0x35, 2, 4, ZERO_PAGE_X),
    ROL_ZP_X	(0x36, 2, 6, ZERO_PAGE_X),
    // This instruction is indirect
    AND_RSTK_Y	(0x37, 2, 7, R_STACK_INDIRECT),
    SEC			(0x38, 1, 2, IMPLIED),
    AND_ABS_Y	(0x39, 3, 4, ABSOLUTE_Y),
    DEC_A		(0x3A, 1, 2, IMPLIED),
    RLX			(0x3B, 1, 4, POP_R_STACK),
    BIT_ABS_X	(0x3C, 3, 4, ABSOLUTE_X),
    AND_ABS_X	(0x3D, 3, 4, ABSOLUTE_X),
    ROL_ABS_X	(0x3E, 3, 6, ABSOLUTE_X),
    MUL_ABS_X	(0x3F, 3, 9, ABSOLUTE_X),
    // This instruction changes the PC
    RTI			(0x40, 0, 6, IMPLIED),
    EOR_IND_X	(0x41, 2, 6, INDEXED_INDIRECT),
    NXA			(0x42, 1, 3, IMPLIED),
    EOR_STK		(0x43, 2, 4, P_STACK_INDEXED),
    REA_ABS		(0x44, 3, 5, ABSOLUTE),
    EOR_ZP		(0x45, 2, 3, ZERO_PAGE),
    LSR_ZP		(0x46, 2, 5, ZERO_PAGE),
    EOR_RSTK	(0x47, 2, 4, R_STACK_INDEXED),
    PHA			(0x48, 1, 3, PUSH_P_STACK),
    EOR_IMM		(0x49, 2, 2, IMMEDIATE),
    LSR_A		(0x4A, 1, 2, IMPLIED),
    RHA			(0x4B, 1, 3, PUSH_R_STACK),
    // This instruction changes the PC
    JMP_ABS		(0x4C, 0, 3, ABSOLUTE),
    EOR_ABS		(0x4D, 3, 4, ABSOLUTE),
    LSR_ABS		(0x4E, 3, 6, ABSOLUTE),
    DIV_ZP		(0x4F, 2, 7, ZERO_PAGE),
    // This instruction changes the PC
    BVC_REL		(0x50, 2, 2, RELATIVE),
    EOR_IND_Y	(0x51, 2, 5, INDIRECT_INDEXED),
    EOR_IND		(0x52, 2, 5, INDIRECT),
    // This instruction is indirect
    EOR_STK_Y	(0x53, 2, 7, P_STACK_INDIRECT),
    REI_IND		(0x54, 3, 6, INDIRECT),
    EOR_ZP_X	(0x55, 2, 4, ZERO_PAGE_X),
    LSR_ZP_X	(0x56, 2, 6, ZERO_PAGE_X),
    // This instruction is indirect
    EOR_RSTK_Y	(0x57, 2, 7, R_STACK_INDIRECT),
    CLI			(0x58, 1, 2, IMPLIED),
    EOR_ABS_Y	(0x59, 3, 4, ABSOLUTE_Y),
    PHY			(0x5A, 1, 3, PUSH_P_STACK),
    RHY			(0x5B, 1, 3, PUSH_R_STACK),
    TXI			(0x5C, 1, 2, IMPLIED),
    EOR_ABS_X	(0x5D, 3, 4, ABSOLUTE_X),
    LSR_ABS_X	(0x5E, 3, 6, ABSOLUTE_X),
    DIV_ZP_X	(0x5F, 2, 8, ZERO_PAGE_X),
    // This instruction changes the PC
    RTS			(0x60, 0, 6, IMPLIED),
    ADC_IND_X	(0x61, 2, 6, INDEXED_INDIRECT),
    PER_REL		(0x62, 3, 6, RELATIVE),
    ADC_STK		(0x63, 2, 4, P_STACK_INDEXED),
    STZ_ZP		(0x64, 2, 3, ZERO_PAGE),
    ADC_ZP		(0x65, 2, 3, ZERO_PAGE),
    ROR_ZP		(0x66, 2, 5, ZERO_PAGE),
    ADC_RSTK	(0x67, 2, 4, R_STACK_INDEXED),
    PLA			(0x68, 1, 4, POP_P_STACK),
    ADC_IMM		(0x69, 2, 2, IMMEDIATE),
    ROR_A		(0x6A, 1, 2, IMPLIED),
    RLA			(0x6B, 1, 4, POP_R_STACK),
    // This instruction changes the PC
    JMP_IND		(0x6C, 0, 5, INDIRECT_16),
    ADC_ABS		(0x6D, 3, 4, ABSOLUTE),
    ROR_ABS		(0x6E, 3, 6, ABSOLUTE),
    DIV_ABS		(0x6F, 3, 8, ABSOLUTE),
    // This instruction changes the PC
    BVS_REL		(0x70, 2, 2, RELATIVE),
    ADC_IND_Y	(0x71, 2, 5, INDIRECT_INDEXED),
    ADC_IND		(0x72, 2, 5, INDIRECT),
    // This instruction is indirect
    ADC_STK_Y	(0x73, 2, 7, P_STACK_INDIRECT),
    STZ_ZP_X	(0x74, 2, 4, ZERO_PAGE_X),
    ADC_ZP_X	(0x75, 2, 4, ZERO_PAGE_X),
    ROR_ZP_X	(0x76, 2, 6, ZERO_PAGE_X),
    // This instruction is indirect
    ADC_RSTK_Y	(0x77, 2, 7, R_STACK_INDIRECT),
    SEI			(0x78, 1, 2, IMPLIED),
    ADC_ABS_Y	(0x79, 3, 4, ABSOLUTE_Y),
    PLY			(0x7A, 1, 4, POP_P_STACK),
    RLY			(0x7B, 1, 4, POP_R_STACK),
    // This instruction is indirect
    JMP_ABS_X	(0x7C, 3, 6, ABSOLUTE_X),
    ADC_ABS_X	(0x7D, 3, 4, ABSOLUTE_X),
    ROR_ABS_X	(0x7E, 3, 6, ABSOLUTE_X),
    DIV_ABS_X	(0x7F, 3, 9, ABSOLUTE_X),
    BRA_REL		(0x80, 2, 3, RELATIVE),
    STA_IND_X	(0x81, 2, 6, INDEXED_INDIRECT),
    RER_REL		(0x82, 3, 6, RELATIVE),
    STA_STK		(0x83, 2, 4, P_STACK_INDEXED),
    STY_ZP		(0x84, 2, 3, ZERO_PAGE),
    STA_ZP		(0x85, 2, 3, ZERO_PAGE),
    STX_ZP		(0x86, 2, 3, ZERO_PAGE),
    STA_RSTK	(0x87, 2, 4, R_STACK_INDEXED),
    DEY			(0x88, 1, 2, IMPLIED),
    BIT_IMM		(0x89, 2, 2, IMMEDIATE),
    TXA			(0x8A, 1, 2, IMPLIED),
    TXR			(0x8B, 1, 2, IMPLIED),
    STY_ABS		(0x8C, 3, 4, ABSOLUTE),
    STA_ABS		(0x8D, 3, 4, ABSOLUTE),
    STX_ABS		(0x8E, 3, 4, ABSOLUTE),
    ZEA			(0x8F, 1, 2, IMPLIED),
    // This instruction changes the PC
    BCC_REL		(0x90, 2, 2, RELATIVE),
    STA_IND_Y	(0x91, 2, 6, INDIRECT_INDEXED),
    STA_IND		(0x92, 2, 5, INDIRECT),
    // This instruction is indirect
    STA_STK_Y	(0x93, 2, 7, P_STACK_INDIRECT),
    STY_ZP_X	(0x94, 2, 4, ZERO_PAGE_X),
    STA_ZP_X	(0x95, 2, 4, ZERO_PAGE_X),
    STX_ZP_Y	(0x96, 2, 4, ZERO_PAGE_Y),
    // This instruction is indirect
    STA_RSTK_Y	(0x97, 2, 7, R_STACK_INDIRECT),
    TYA			(0x98, 1, 2, IMPLIED),
    STA_ABS_Y	(0x99, 3, 5, ABSOLUTE_Y),
    TXS			(0x9A, 1, 2, IMPLIED),
    TXY			(0x9B, 1, 2, IMPLIED),
    STZ_ABS		(0x9C, 3, 4, ABSOLUTE),
    STA_ABS_X	(0x9D, 3, 5, ABSOLUTE_X),
    STZ_ABS_X	(0x9E, 3, 5, ABSOLUTE_X),
    SEA			(0x9F, 1, 2, IMPLIED),
    LDY_IMM		(0xA0, 2, 2, IMMEDIATE),
    LDA_IND_X	(0xA1, 2, 6, INDEXED_INDIRECT),
    LDX_IMM		(0xA2, 2, 2, IMMEDIATE),
    LDA_STK		(0xA3, 2, 4, P_STACK_INDEXED),
    LDY_ZP		(0xA4, 2, 3, ZERO_PAGE),
    LDA_ZP		(0xA5, 2, 3, ZERO_PAGE),
    LDX_ZP		(0xA6, 2, 3, ZERO_PAGE),
    LDA_RSTK	(0xA7, 2, 4, R_STACK_INDEXED),
    TAY			(0xA8, 1, 2, IMPLIED),
    LDA_IMM		(0xA9, 2, 2, IMMEDIATE),
    TAX			(0xAA, 1, 2, IMPLIED),
    TRX			(0xAB, 1, 2, IMPLIED),
    LDY_ABS		(0xAC, 3, 4, ABSOLUTE),
    LDA_ABS		(0xAD, 3, 4, ABSOLUTE),
    LDX_ABS		(0xAE, 3, 4, ABSOLUTE),
    TDA			(0xAF, 1, 2, IMPLIED),
    // This instruction changes the PC
    BCS_REL		(0xB0, 2, 2, RELATIVE),
    LDA_IND_Y	(0xB1, 2, 5, INDIRECT_INDEXED),
    LDA_IND		(0xB2, 2, 5, INDIRECT),
    // This instruction is indirect
    LDA_STK_Y	(0xB3, 2, 7, P_STACK_INDIRECT),
    LDY_ZP_X	(0xB4, 2, 4, ZERO_PAGE_X),
    LDA_ZP_X	(0xB5, 2, 4, ZERO_PAGE_X),
    LDX_ZP_Y	(0xB6, 2, 4, ZERO_PAGE_Y),
    // This instruction is indirect
    LDA_RSTK_Y	(0xB7, 2, 7, R_STACK_INDIRECT),
    CLV			(0xB8, 1, 2, IMPLIED),
    LDA_ABS_Y	(0xB9, 3, 4, ABSOLUTE_Y),
    TSX			(0xBA, 1, 2, IMPLIED),
    TYX			(0xBB, 1, 2, IMPLIED),
    LDY_ABS_X	(0xBC, 3, 4, ABSOLUTE_X),
    LDA_ABS_X	(0xBD, 3, 4, ABSOLUTE_X),
    LDX_ABS_Y	(0xBE, 3, 4, ABSOLUTE_Y),
    TAD			(0xBF, 1, 2, IMPLIED),
    CPY_IMM		(0xC0, 2, 2, IMMEDIATE),
    CMP_IND_X	(0xC1, 2, 6, INDEXED_INDIRECT),
    REP_IMM		(0xC2, 2, 3, IMMEDIATE),
    CMP_STK		(0xC3, 2, 4, P_STACK_INDEXED),
    CPY_ZP		(0xC4, 2, 3, ZERO_PAGE),
    CMP_ZP		(0xC5, 2, 3, ZERO_PAGE),
    DEC_ZP		(0xC6, 2, 5, ZERO_PAGE),
    CMP_RSTK	(0xC7, 2, 4, R_STACK_INDEXED),
    INY			(0xC8, 1, 2, IMPLIED),
    CMP_IMM		(0xC9, 2, 2, IMMEDIATE),
    DEX			(0xCA, 1, 2, IMPLIED),
    WAI			(0xCB, 1, 3, IMPLIED),
    CPY_ABS		(0xCC, 3, 4, ABSOLUTE),
    CMP_ABS		(0xCD, 3, 4, ABSOLUTE),
    DEC_ABS		(0xCE, 3, 6, ABSOLUTE),
    PLD			(0xCF, 1, 4, POP_P_STACK),
    // This instruction changes the PC
    BNE_REL		(0xD0, 2, 2, RELATIVE),
    CMP_IND_Y	(0xD1, 2, 5, INDIRECT_INDEXED),
    CMP_IND		(0xD2, 2, 5, INDIRECT),
    // This instruction is indirect
    CMP_STK_Y	(0xD3, 2, 7, P_STACK_INDIRECT),
    PEI_IND		(0xD4, 2, 6, INDIRECT),
    CMP_ZP_X	(0xD5, 2, 4, ZERO_PAGE_X),
    DEC_ZP_X	(0xD6, 2, 6, ZERO_PAGE_X),
    // This instruction is indirect
    CMP_RSTK_Y	(0xD7, 2, 7, R_STACK_INDIRECT),
    CLD			(0xD8, 1, 2, IMPLIED),
    CMP_ABS_Y	(0xD9, 3, 4, ABSOLUTE_Y),
    PHX			(0xDA, 1, 3, PUSH_P_STACK),
    STP			(0xDB, 1, 3, IMPLIED),
    TIX			(0xDC, 1, 2, IMPLIED),
    CMP_ABS_X	(0xDD, 3, 4, ABSOLUTE_X),
    DEC_ABS_X	(0xDE, 3, 7, ABSOLUTE_X),
    PHD			(0xDF, 1, 3, PUSH_P_STACK),
    CPX_IMM		(0xE0, 2, 2, IMMEDIATE),
    SBC_IND_X	(0xE1, 2, 6, INDEXED_INDIRECT),
    SEP_IMM		(0xE2, 2, 3, IMMEDIATE),
    SBC_STK		(0xE3, 2, 4, P_STACK_INDEXED),
    CPX_ZP		(0xE4, 2, 3, ZERO_PAGE),
    SBC_ZP		(0xE5, 2, 3, ZERO_PAGE),
    INC_ZP		(0xE6, 2, 5, ZERO_PAGE),
    SBC_RSTK	(0xE7, 2, 4, R_STACK_INDEXED),
    INX			(0xE8, 1, 2, IMPLIED),
    SBC_IMM		(0xE9, 2, 2, IMMEDIATE),
    NOP			(0xEA, 1, 2, IMPLIED),
    XBA			(0xEB, 1, 3, IMPLIED),
    CPX_ABS		(0xEC, 3, 4, ABSOLUTE),
    SBC_ABS		(0xED, 3, 4, ABSOLUTE),
    INC_ABS		(0xEE, 3, 6, ABSOLUTE),
    MMU			(0xEF, 2, 2, IMMEDIATE),
    // This instruction changes the PC
    BEQ_REL		(0xF0, 2, 2, RELATIVE),
    SBC_IND_Y	(0xF1, 2, 5, INDIRECT_INDEXED),
    SBC_IND		(0xF2, 2, 5, INDIRECT),
    // This instruction is indirect
    SBC_STK_Y	(0xF3, 2, 7, P_STACK_INDIRECT),
    PEA_ABS		(0xF4, 3, 5, IMMEDIATE),
    SBC_ZP_X	(0xF5, 2, 4, ZERO_PAGE_X),
    INC_ZP_X	(0xF6, 2, 6, ZERO_PAGE_X),
    // This instruction is indirect
    SBC_RSTK_Y	(0xF7, 2, 7, R_STACK_INDIRECT),
    SED			(0xF8, 1, 2, IMPLIED),
    SBC_ABS_Y	(0xF9, 3, 4, ABSOLUTE_Y),
    PLX			(0xFA, 1, 4, POP_P_STACK),
    XCE			(0xFB, 1, 2, IMPLIED),
    JSR_ABS_X	(0xFC, 0, 8, ABSOLUTE_X),
    // This instruction is indirect
    SBC_ABS_X	(0xFD, 3, 4, ABSOLUTE_X),
    INC_ABS_X	(0xFE, 3, 7, ABSOLUTE_X),
    UNUSED		(0xFF, 0, 3500, IMPLIED);

    private static final HashMap<Integer, InstructionComputer> instructions = new HashMap<Integer, InstructionComputer>();

    static {
        for(InstructionComputer instruction : InstructionComputer.values()) {
            instructions.put(instruction.getHexValue(), instruction);
        }
    }

    private final int hexValue;
    private final int length;
    private final int numCycles;
    private final AddressingMode addressingMode;

    private InstructionComputer(int hexValue, int length, int numCycles, AddressingMode addressingMode) {
        this.hexValue = hexValue;
        this.length = length;
        this.numCycles = numCycles;
        this.addressingMode = addressingMode;
    }

    public int getHexValue() {
        return hexValue;
    }

    public int getLength() {
        return length;
    }

    public int getNumCycles() {
        return numCycles;
    }

    public AddressingMode getAddressingMode() {
        return addressingMode;
    }

    public static InstructionComputer getInstruction(int hexValue) {
        InstructionComputer instruction = instructions.get(hexValue);
        if(instruction == null) {return UNUSED;}
        return instruction;
    }

}
