package com.github.blackjak34.compute.enums;

import java.util.HashMap;

/**
 * An enum containing all of the instructions available to
 * the 6502 processor and their corresponding hex values.
 * Helper functions are also included to retrieve the hex
 * value from the instruction enum and vice versa.
 * 
 * @author Blackjak34
 * @since 1.0
 */
public enum InstructionComputer {
	// Length is one extra on purpose; part of function
	BRK			(0x00, 2),
	ORA_IND_X	(0x01, 2),
	ORA_ZP		(0x05, 2),
	ASL_ZP		(0x06, 2),
	PHP			(0x08, 1),
	ORA_IMM		(0x09, 2),
	ASL_A		(0x0A, 1),
	ORA_ABS		(0x0D, 3),
	ASL_ABS		(0x0E, 3),
	// This instruction changes the PC
	BPL_REL		(0x10, 2),
	ORA_IND_Y	(0x11, 2),
	ORA_ZP_X	(0x15, 2),
	ASL_ZP_X	(0x16, 2),
	CLC			(0x18, 1),
	ORA_ABS_Y	(0x19, 3),
	ORA_ABS_X	(0x1D, 3),
	ASL_ABS_X	(0x1E, 3),
	// This instruction changes the PC
	JSR_ABS		(0x20, 0),
	AND_IND_X	(0x21, 2),
	BIT_ZP		(0x24, 2),
	AND_ZP		(0x25, 2),
	ROL_ZP		(0x26, 2),
	PLP			(0x28, 1),
	AND_IMM		(0x29, 2),
	ROL_A		(0x2A, 1),
	BIT_ABS		(0x2C, 3),
	AND_ABS		(0x2D, 3),
	ROL_ABS		(0x2E, 3),
	// This instruction changes the PC
	BMI_REL		(0x30, 2),
	AND_IND_Y	(0x31, 2),
	AND_ZP_X	(0x35, 2),
	ROL_ZP_X	(0x36, 2),
	SEC			(0x38, 1),
	AND_ABS_Y	(0x39, 3),
	AND_ABS_X	(0x3D, 3),
	ROL_ABS_X	(0x3E, 3),
	// This instruction changes the PC
	RTI			(0x40, 0),
	EOR_IND_X	(0x41, 2),
	EOR_ZP		(0x45, 2),
	LSR_ZP		(0x46, 2),
	PHA			(0x48, 1),
	EOR_IMM		(0x49, 2),
	LSR_A		(0x4A, 1),
	// This instruction changes the PC
	JMP_ABS		(0x4C, 0),
	EOR_ABS		(0x4D, 3),
	LSR_ABS		(0x4E, 3),
	// This instruction changes the PC
	BVC_REL		(0x50, 2),
	EOR_IND_Y	(0x51, 2),
	EOR_ZP_X	(0x55, 2),
	LSR_ZP_X	(0x56, 2),
	CLI			(0x58, 1),
	EOR_ABS_Y	(0x59, 3),
	EOR_ABS_X	(0x5D, 3),
	LSR_ABS_X	(0x5E, 3),
	// This instruction changes the PC
	RTS			(0x60, 0),
	ADC_IND_X	(0x61, 2),
	ADC_ZP		(0x65, 2),
	ROR_ZP		(0x66, 2),
	PLA			(0x68, 1),
	ADC_IMM		(0x69, 2),
	ROR_A		(0x6A, 1),
	// This instruction changes the PC
	JMP_IND		(0x6C, 0),
	ADC_ABS		(0x6D, 3),
	ROR_ABS		(0x6E, 3),
	// This instruction changes the PC
	BVS_REL		(0x70, 2),
	ADC_IND_Y	(0x71, 2),
	ADC_ZP_X	(0x75, 2),
	ROR_ZP_X	(0x76, 2),
	SEI			(0x78, 1),
	ADC_ABS_Y	(0x79, 3),
	ADC_ABS_X	(0x7D, 3),
	ROR_ABS_X	(0x7E, 3),
	STA_IND_X	(0x81, 2),
	STY_ZP		(0x84, 2),
	STA_ZP		(0x85, 2),
	STX_ZP		(0x86, 2),
	DEY			(0x88, 1),
	TXA			(0x8A, 1),
	STY_ABS		(0x8C, 3),
	STA_ABS		(0x8D, 3),
	STX_ABS		(0x8E, 3),
	// This instruction changes the PC
	BCC_REL		(0x90, 2),
	STA_IND_Y	(0x91, 2),
	STY_ZP_X	(0x94, 2),
	STA_ZP_X	(0x95, 2),
	STX_ZP_Y	(0x96, 2),
	TYA			(0x98, 1),
	STA_ABS_Y	(0x99, 3),
	TXS			(0x9A, 1),
	STA_ABS_X	(0x9D, 3),
	LDY_IMM		(0xA0, 2),
	LDA_IND_X	(0xA1, 2),
	LDX_IMM		(0xA2, 2),
	LDY_ZP		(0xA4, 2),
	LDA_ZP		(0xA5, 2),
	LDX_ZP		(0xA6, 2),
	TAY			(0xA8, 1),
	LDA_IMM		(0xA9, 2),
	TAX			(0xAA, 1),
	LDY_ABS		(0xAC, 3),
	LDA_ABS		(0xAD, 3),
	LDX_ABS		(0xAE, 3),
	// This instruction changes the PC
	BCS_REL		(0xB0, 2),
	LDA_IND_Y	(0xB1, 2),
	LDY_ZP_X	(0xB4, 2),
	LDA_ZP_X	(0xB5, 2),
	LDX_ZP_Y	(0xB6, 2),
	CLV			(0xB8, 1),
	LDA_ABS_Y	(0xB9, 3),
	TSX			(0xBA, 1),
	LDY_ABS_X	(0xBC, 3),
	LDA_ABS_X	(0xBD, 3),
	LDX_ABS_Y	(0xBE, 3),
	CPY_IMM		(0xC0, 2),
	CMP_IND_X	(0xC1, 2),
	CPY_ZP		(0xC4, 2),
	CMP_ZP		(0xC5, 2),
	DEC_ZP		(0xC6, 2),
	INY			(0xC8, 1),
	CMP_IMM		(0xC9, 2),
	DEX			(0xCA, 1),
	CPY_ABS		(0xCC, 3),
	CMP_ABS		(0xCD, 3),
	DEC_ABS		(0xCE, 3),
	// This instruction changes the PC
	BNE_REL		(0xD0, 2),
	CMP_IND_Y	(0xD2, 2),
	CMP_ZP_X	(0xD5, 2),
	DEC_ZP_X	(0xD6, 2),
	CLD			(0xD8, 1),
	CMP_ABS_Y	(0xD9, 3),
	STP			(0xDB, 1),
	CMP_ABS_X	(0xDD, 3),
	DEC_ABS_X	(0xDE, 3),
	CPX_IMM		(0xE0, 2),
	SBC_IND_X	(0xE1, 2),
	CPX_ZP		(0xE4, 2),
	SBC_ZP		(0xE5, 2),
	INC_ZP		(0xE6, 2),
	INX			(0xE8, 1),
	SBC_IMM		(0xE9, 2),
	NOP			(0xEA, 1),
	CPX_ABS		(0xEC, 3),
	SBC_ABS		(0xED, 3),
	INC_ABS		(0xEE, 3),
	// This instruction changes the PC
	BEQ_REL		(0xF0, 2),
	SBC_IND_Y	(0xF1, 2),
	SBC_ZP_X	(0xF5, 2),
	INC_ZP_X	(0xF6, 2),
	SED			(0xF8, 1),
	SBC_ABS_Y	(0xF9, 3),
	SBC_ABS_X	(0xFD, 3),
	INC_ABS_X	(0xFE, 3),
	UNUSED		(0xFF, 1);
	
	/**
	 * A HashMap containing the hex value for each
	 * instruction and its corresponding enum. Used for the
	 * getInstruction helper function.
	 */
	private static final HashMap<Integer, InstructionComputer> instructions = new HashMap<Integer, InstructionComputer>();
	
	static {
		for(InstructionComputer instruction : InstructionComputer.values()) {
			instructions.put(instruction.getHexValue(), instruction);
		}
	}
	
	/**
	 * The corresponding hex value for this instruction.
	 */
	private final int hexValue;
	
	/**
	 * How many bytes long this instruction is, including
	 * the identifier itself.
	 */
	private final int length;
	
	/**
	 * This constructor only serves to assign the hex
	 * values to the enums at initialization.
	 * 
	 * @param hexValue The hex value for this instruction
	 */
	private InstructionComputer(int hexValue, int length) {
		this.hexValue = hexValue;
		this.length = length;
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
