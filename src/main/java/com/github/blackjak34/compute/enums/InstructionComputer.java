package com.github.blackjak34.compute.enums;

import java.util.HashMap;

/*
 * All branches have a listed cycle number of two; branches require three cycles if they are taken and four cycles if
 * they are taken and cross a page boundary.
 * ADC and SBC take an extra cycle when in decimal mode.
 */
public enum InstructionComputer {
	// Length is one extra on purpose; part of function
	BRK			(0x00, 2, 7, false, false),
	ORA_IND_X	(0x01, 2, 6, false, false),
	TSB_ZP		(0x04, 2, 5, false, false),
	ORA_ZP		(0x05, 2, 3, false, false),
	ASL_ZP		(0x06, 2, 5, false, false),
	PHP			(0x08, 1, 3, false, false),
	ORA_IMM		(0x09, 2, 2, false, true),
	ASL_A		(0x0A, 1, 2, false, false),
	RHI			(0x0B, 1, 3, false, false),
	TSB_ABS		(0x0C, 3, 6, false, false),
	ORA_ABS		(0x0D, 3, 4, false, false),
	ASL_ABS		(0x0E, 3, 6, false, false),
	MUL_ZP		(0x0F, 2, 7, false, false),
	// This instruction changes the PC
	BPL_REL		(0x10, 2, 2, true, false),
	ORA_IND_Y	(0x11, 2, 5, true, false),
	ORA_IND		(0x12, 2, 5, false, false),
	TRB_ZP		(0x14, 2, 5, false, false),
	ORA_ZP_X	(0x15, 2, 4, false, false),
	ASL_ZP_X	(0x16, 2, 6, false, false),
	CLC			(0x18, 1, 2, false, false),
	ORA_ABS_Y	(0x19, 3, 4, true, false),
	INC_A		(0x1A, 1, 2, false, false),
	RHX			(0x1B, 1, 3, false, false),
	TRB_ABS		(0x1C, 3, 6, false, false),
	ORA_ABS_X	(0x1D, 3, 4, true, false),
	ASL_ABS_X	(0x1E, 3, 6, true, false),
	MUL_ZP_X	(0x1F, 2, 8, false, false),
	// This instruction changes the PC
	JSR_ABS		(0x20, 0, 6, false, false),
	AND_IND_X	(0x21, 2, 6, false, false),
	BIT_ZP		(0x24, 2, 3, false, false),
	AND_ZP		(0x25, 2, 3, false, false),
	ROL_ZP		(0x26, 2, 5, false, false),
	PLP			(0x28, 1, 4, false, false),
	AND_IMM		(0x29, 2, 2, false, true),
	ROL_A		(0x2A, 1, 2, false, false),
	RLI			(0x2B, 1, 4, false, false),
	BIT_ABS		(0x2C, 3, 4, false, false),
	AND_ABS		(0x2D, 3, 4, false, false),
	ROL_ABS		(0x2E, 3, 6, false, false),
	MUL_ABS		(0x2F, 3, 8, false, false),
	// This instruction changes the PC
	BMI_REL		(0x30, 2, 2, true, false),
	AND_IND_Y	(0x31, 2, 5, true, false),
	AND_IND		(0x32, 2, 5, false, false),
	BIT_ZP_X	(0x34, 2, 4, false, false),
	AND_ZP_X	(0x35, 2, 4, false, false),
	ROL_ZP_X	(0x36, 2, 6, false, false),
	SEC			(0x38, 1, 2, false, false),
	AND_ABS_Y	(0x39, 3, 4, true, false),
	DEC_A		(0x3A, 1, 2, false, false),
	RLX			(0x3B, 1, 4, false, false),
	BIT_ABS_X	(0x3C, 3, 4, true, false),
	AND_ABS_X	(0x3D, 3, 4, true, false),
	ROL_ABS_X	(0x3E, 3, 6, true, false),
	MUL_ABS_X	(0x3F, 3, 9, true, false),
	// This instruction changes the PC
	RTI			(0x40, 0, 6, false, false),
	EOR_IND_X	(0x41, 2, 6, false, false),
	EOR_ZP		(0x45, 2, 3, false, false),
	LSR_ZP		(0x46, 2, 5, false, false),
	PHA			(0x48, 1, 3, false, false),
	EOR_IMM		(0x49, 2, 2, false, true),
	LSR_A		(0x4A, 1, 2, false, false),
	RHA			(0x4B, 1, 3, false, false),
	// This instruction changes the PC
	JMP_ABS		(0x4C, 0, 3, false, false),
	EOR_ABS		(0x4D, 3, 4, false, false),
	LSR_ABS		(0x4E, 3, 6, false, false),
	DIV_ZP		(0x4F, 2, 7, false, false),
	// This instruction changes the PC
	BVC_REL		(0x50, 2, 2, true, false),
	EOR_IND_Y	(0x51, 2, 5, true, false),
	EOR_IND		(0x52, 2, 5, false, false),
	EOR_ZP_X	(0x55, 2, 4, false, false),
	LSR_ZP_X	(0x56, 2, 6, false, false),
	CLI			(0x58, 1, 2, false, false),
	EOR_ABS_Y	(0x59, 3, 4, true, false),
	PHY			(0x5A, 1, 3, false, false),
	RHY			(0x5B, 1, 3, false, false),
	TXI			(0x5C, 1, 2, false, false),
	EOR_ABS_X	(0x5D, 3, 4, true, false),
	LSR_ABS_X	(0x5E, 3, 6, true, false),
	DIV_ZP_X	(0x5F, 2, 8, false, false),
	// This instruction changes the PC
	RTS			(0x60, 1, 6, false, false),
	ADC_IND_X	(0x61, 2, 6, false, false),
	STZ_ZP		(0x64, 2, 3, false, false),
	ADC_ZP		(0x65, 2, 3, false, false),
	ROR_ZP		(0x66, 2, 5, false, false),
	PLA			(0x68, 1, 4, false, false),
	ADC_IMM		(0x69, 2, 2, false, true),
	ROR_A		(0x6A, 1, 2, false, false),
	RLA			(0x6B, 1, 4, false, false),
	// This instruction changes the PC
	JMP_IND		(0x6C, 0, 5, false, false),
	ADC_ABS		(0x6D, 3, 4, false, false),
	ROR_ABS		(0x6E, 3, 6, false, false),
	DIV_ABS		(0x6F, 3, 8, false, false),
	// This instruction changes the PC
	BVS_REL		(0x70, 2, 2, true, false),
	ADC_IND_Y	(0x71, 2, 5, true, false),
	ADC_IND		(0x72, 2, 5, false, false),
	STZ_ZP_X	(0x74, 2, 4, false, false),
	ADC_ZP_X	(0x75, 2, 4, false, false),
	ROR_ZP_X	(0x76, 2, 6, false, false),
	SEI			(0x78, 1, 2, false, false),
	ADC_ABS_Y	(0x79, 3, 4, true, false),
	PLY			(0x7A, 1, 4, false, false),
	RLY			(0x7B, 1, 4, false, false),
	// This instruction is indirect
	JMP_ABS_X	(0x7C, 3, 6, false, false),
	ADC_ABS_X	(0x7D, 3, 4, true, false),
	ROR_ABS_X	(0x7E, 3, 6, true, false),
	DIV_ABS_X	(0x7F, 3, 9, true, false),
	BRA_REL		(0x80, 2, 3, true, false),
	STA_IND_X	(0x81, 2, 6, false, false),
	STY_ZP		(0x84, 2, 3, false, false),
	STA_ZP		(0x85, 2, 3, false, false),
	STX_ZP		(0x86, 2, 3, false, false),
	DEY			(0x88, 1, 2, false, false),
	BIT_IMM		(0x89, 2, 2, false, true),
	TXA			(0x8A, 1, 2, false, false),
	TXR			(0x8B, 1, 2, false, false),
	STY_ABS		(0x8C, 3, 4, false, false),
	STA_ABS		(0x8D, 3, 4, false, false),
	STX_ABS		(0x8E, 3, 4, false, false),
	// This instruction changes the PC
	BCC_REL		(0x90, 2, 2, true, false),
	STA_IND_Y	(0x91, 2, 6, false, false),
	STA_IND		(0x92, 2, 5, false, false),
	STY_ZP_X	(0x94, 2, 4, false, false),
	STA_ZP_X	(0x95, 2, 4, false, false),
	STX_ZP_Y	(0x96, 2, 4, false, false),
	TYA			(0x98, 1, 2, false, false),
	STA_ABS_Y	(0x99, 3, 5, false, false),
	TXS			(0x9A, 1, 2, false, false),
	TXY			(0x9B, 1, 2, false, false),
	STZ_ABS		(0x9C, 3, 4, false, false),
	STA_ABS_X	(0x9D, 3, 5, false, false),
	STZ_ABS_X	(0x9E, 3, 5, false, false),
	LDY_IMM		(0xA0, 2, 2, false, true),
	LDA_IND_X	(0xA1, 2, 6, false, false),
	LDX_IMM		(0xA2, 2, 2, false, true),
	LDY_ZP		(0xA4, 2, 3, false, false),
	LDA_ZP		(0xA5, 2, 3, false, false),
	LDX_ZP		(0xA6, 2, 3, false, false),
	TAY			(0xA8, 1, 2, false, false),
	LDA_IMM		(0xA9, 2, 2, false, true),
	TAX			(0xAA, 1, 2, false, false),
	TRX			(0xAB, 1, 2, false, false),
	LDY_ABS		(0xAC, 3, 4, false, false),
	LDA_ABS		(0xAD, 3, 4, false, false),
	LDX_ABS		(0xAE, 3, 4, false, false),
	TDA			(0xAF, 1, 2, false, false),
	// This instruction changes the PC
	BCS_REL		(0xB0, 2, 2, true, false),
	LDA_IND_Y	(0xB1, 2, 5, true, false),
	LDA_IND		(0xB2, 2, 5, false, false),
	LDY_ZP_X	(0xB4, 2, 4, false, false),
	LDA_ZP_X	(0xB5, 2, 4, false, false),
	LDX_ZP_Y	(0xB6, 2, 4, false, false),
	CLV			(0xB8, 1, 2, false, false),
	LDA_ABS_Y	(0xB9, 3, 4, true, false),
	TSX			(0xBA, 1, 2, false, false),
	TYX			(0xBB, 1, 2, false, false),
	LDY_ABS_X	(0xBC, 3, 4, true, false),
	LDA_ABS_X	(0xBD, 3, 4, true, false),
	LDX_ABS_Y	(0xBE, 3, 4, true, false),
	TAD			(0xBF, 1, 2, false, false),
	CPY_IMM		(0xC0, 2, 2, false, true),
	CMP_IND_X	(0xC1, 2, 6, false, false),
	CPY_ZP		(0xC4, 2, 3, false, false),
	CMP_ZP		(0xC5, 2, 3, false, false),
	DEC_ZP		(0xC6, 2, 5, false, false),
	INY			(0xC8, 1, 2, false, false),
	CMP_IMM		(0xC9, 2, 2, false, true),
	DEX			(0xCA, 1, 2, false, false),
	WAI			(0xCB, 1, 3, false, false),
	CPY_ABS		(0xCC, 3, 4, false, false),
	CMP_ABS		(0xCD, 3, 4, false, false),
	DEC_ABS		(0xCE, 3, 6, false, false),
	PLD			(0xCF, 1, 4, false, false),
	// This instruction changes the PC
	BNE_REL		(0xD0, 2, 2, true, false),
	CMP_IND_Y	(0xD1, 2, 5, true, false),
	CMP_IND		(0xD2, 2, 5, false, false),
	CMP_ZP_X	(0xD5, 2, 4, false, false),
	DEC_ZP_X	(0xD6, 2, 6, false, false),
	CLD			(0xD8, 1, 2, false, false),
	CMP_ABS_Y	(0xD9, 3, 4, true, false),
	PHX			(0xDA, 1, 3, false, false),
	STP			(0xDB, 1, 3, false, false),
	TIX			(0xDC, 1, 2, false, false),
	CMP_ABS_X	(0xDD, 3, 4, true, false),
	DEC_ABS_X	(0xDE, 3, 7, false, false),
	PHD			(0xDF, 1, 3, false, false),
	CPX_IMM		(0xE0, 2, 2, false, true),
	SBC_IND_X	(0xE1, 2, 6, false, false),
	CPX_ZP		(0xE4, 2, 3, false, false),
	SBC_ZP		(0xE5, 2, 3, false, false),
	INC_ZP		(0xE6, 2, 5, false, false),
	INX			(0xE8, 1, 2, false, false),
	SBC_IMM		(0xE9, 2, 2, false, false),
	NOP			(0xEA, 1, 2, false, false),
	CPX_ABS		(0xEC, 3, 4, false, false),
	SBC_ABS		(0xED, 3, 4, false, false),
	INC_ABS		(0xEE, 3, 6, false, false),
	MMU			(0xEF, 1, 2, false, false),
	// This instruction changes the PC
	BEQ_REL		(0xF0, 2, 2, true, false),
	SBC_IND_Y	(0xF1, 2, 5, true, false),
	SBC_IND		(0xF2, 2, 5, false, false),
	SBC_ZP_X	(0xF5, 2, 4, false, false),
	INC_ZP_X	(0xF6, 2, 6, false, false),
	SED			(0xF8, 1, 2, false, false),
	SBC_ABS_Y	(0xF9, 3, 4, true, false),
	PLX			(0xFA, 1, 4, false, false),
	// This instruction is indirect
	SBC_ABS_X	(0xFD, 3, 4, true, false),
	INC_ABS_X	(0xFE, 3, 7, false, false),
	UNUSED		(0xFF, 0, 0, false, false);

	private static final HashMap<Integer, InstructionComputer> instructions = new HashMap<Integer, InstructionComputer>();
	
	static {
		for(InstructionComputer instruction : InstructionComputer.values()) {
			instructions.put(instruction.getHexValue(), instruction);
		}
	}

	private final int hexValue;
	private final int length;
	private final int numCycles;
	private final boolean pageSensitive;
	private final boolean lengthSensitive;
	
	/**
	 * This constructor only serves to assign the hex
	 * values to the enums at initialization.
	 * 
	 * @param hexValue The hex value for this instruction
	 * @param length How many bytes long this instruction is
	 */
	private InstructionComputer(int hexValue, int length, int numCycles, boolean pageSensitive, boolean lengthSensitive) {
		this.hexValue = hexValue;
		this.length = length;
		this.numCycles = numCycles;
		this.pageSensitive = pageSensitive;
		this.lengthSensitive = lengthSensitive;
	}
	
	/**
	 * Returns the hex value of this instruction.
	 * 
	 * @return The hex value of this instruction
	 */
	public int getHexValue() {
		return hexValue;
	}
	
	/**
	 * Returns how many bytes long this instruction is,
	 * including the identifier itself.
	 * @return How many bytes long this instruction is
	 */
	public int getLength() {
		return length;
	}

	public int getNumCycles() {
		return numCycles;
	}

	public boolean isPageSensitive() {
		return pageSensitive;
	}

	public boolean isLengthSensitive() {
		return lengthSensitive;
	}
	
	/**
	 * Returns an instruction that corresponds to the given
	 * hex value. If no instruction exists, the enum UNUSED
	 * is returned instead.
	 * 
	 * @param hexValue A hex value to get the enum for
	 * @return The enum corresponding to the given hex value
	 */
	public static InstructionComputer getInstruction(int hexValue) {
		InstructionComputer instruction = instructions.get(hexValue);
		if(instruction == null) {return UNUSED;}
		return instruction;
	}
	
}
