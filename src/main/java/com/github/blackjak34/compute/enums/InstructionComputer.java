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
	BRK			((byte) 0x00, 1),
	ORA_IND_X	((byte) 0x01, 2),
	ORA_ZP		((byte) 0x05, 2),
	ASL_ZP		((byte) 0x06, 2),
	PHP			((byte) 0x08, 1),
	ORA_IMM		((byte) 0x09, 2),
	ASL_A		((byte) 0x0A, 1),
	ORA_ABS		((byte) 0x0D, 3),
	ASL_ABS		((byte) 0x0E, 3),
	BPL_REL		((byte) 0x10, 2),
	ORA_IND_Y	((byte) 0x11, 2),
	ORA_ZP_X	((byte) 0x15, 2),
	ASL_ZP_X	((byte) 0x16, 2),
	CLC			((byte) 0x18, 1),
	ORA_ABS_Y	((byte) 0x19, 3),
	ORA_ABS_X	((byte) 0x1D, 3),
	ASL_ABS_X	((byte) 0x1E, 3),
	JSR_ABS		((byte) 0x20, 3),
	AND_IND_X	((byte) 0x21, 2),
	BIT_ZP		((byte) 0x24, 2),
	AND_ZP		((byte) 0x25, 2),
	ROL_ZP		((byte) 0x26, 2),
	PLP			((byte) 0x28, 1),
	AND_IMM		((byte) 0x29, 2),
	ROL_A		((byte) 0x2A, 1),
	BIT_ABS		((byte) 0x2C, 3),
	AND_ABS		((byte) 0x2D, 3),
	ROL_ABS		((byte) 0x2E, 3),
	BMI_REL		((byte) 0x30, 2),
	AND_IND_Y	((byte) 0x31, 2),
	AND_ZP_X	((byte) 0x35, 2),
	ROL_ZP_X	((byte) 0x36, 2),
	SEC			((byte) 0x38, 1),
	AND_ABS_Y	((byte) 0x39, 3),
	AND_ABS_X	((byte) 0x3D, 3),
	ROL_ABS_X	((byte) 0x3E, 3),
	RTI			((byte) 0x40, 1),
	EOR_IND_X	((byte) 0x41, 2),
	EOR_ZP		((byte) 0x45, 2),
	LSR_ZP		((byte) 0x46, 2),
	PHA			((byte) 0x48, 1),
	EOR_IMM		((byte) 0x49, 2),
	LSR_A		((byte) 0x4A, 1),
	JMP_ABS		((byte) 0x4C, 3),
	EOR_ABS		((byte) 0x4D, 3),
	LSR_ABS		((byte) 0x4E, 3),
	BVC_REL		((byte) 0x50, 2),
	EOR_IND_Y	((byte) 0x51, 2),
	EOR_ZP_X	((byte) 0x55, 2),
	LSR_ZP_X	((byte) 0x56, 2),
	CLI			((byte) 0x58, 1),
	EOR_ABS_Y	((byte) 0x59, 3),
	EOR_ABS_X	((byte) 0x5D, 3),
	LSR_ABS_X	((byte) 0x5E, 3),
	RTS			((byte) 0x60, 1),
	ADC_IND_X	((byte) 0x61, 2),
	ADC_ZP		((byte) 0x65, 2),
	ROR_ZP		((byte) 0x66, 2),
	PLA			((byte) 0x68, 1),
	ADC_IMM		((byte) 0x69, 2),
	ROR_A		((byte) 0x6A, 1),
	JMP_IND		((byte) 0x6C, 3),
	ADC_ABS		((byte) 0x6D, 3),
	ROR_ABS		((byte) 0x6E, 3),
	BVS_REL		((byte) 0x70, 2),
	ADC_IND_Y	((byte) 0x71, 2),
	ADC_ZP_X	((byte) 0x75, 2),
	ROR_ZP_X	((byte) 0x76, 2),
	SEI			((byte) 0x78, 1),
	ADC_ABS_Y	((byte) 0x79, 3),
	ADC_ABS_X	((byte) 0x7D, 3),
	ROR_ABS_X	((byte) 0x7E, 3),
	STA_IND_X	((byte) 0x81, 2),
	STY_ZP		((byte) 0x84, 2),
	STA_ZP		((byte) 0x85, 2),
	STX_ZP		((byte) 0x86, 2),
	DEY			((byte) 0x88, 1),
	TXA			((byte) 0x8A, 1),
	STY_ABS		((byte) 0x8C, 3),
	STA_ABS		((byte) 0x8D, 3),
	STX_ABS		((byte) 0x8E, 3),
	BCC_REL		((byte) 0x90, 2),
	STA_IND_Y	((byte) 0x91, 2),
	STY_ZP_X	((byte) 0x94, 2),
	STA_ZP_X	((byte) 0x95, 2),
	STX_ZP_Y	((byte) 0x96, 2),
	TYA			((byte) 0x98, 1),
	STA_ABS_Y	((byte) 0x99, 3),
	TXS			((byte) 0x9A, 1),
	STA_ABS_X	((byte) 0x9D, 3),
	LDY_IMM		((byte) 0xA0, 2),
	LDA_IND_X	((byte) 0xA1, 2),
	LDX_IMM		((byte) 0xA2, 2),
	LDY_ZP		((byte) 0xA4, 2),
	LDA_ZP		((byte) 0xA5, 2),
	LDX_ZP		((byte) 0xA6, 2),
	TAY			((byte) 0xA8, 1),
	LDA_IMM		((byte) 0xA9, 2),
	TAX			((byte) 0xAA, 1),
	LDY_ABS		((byte) 0xAC, 3),
	LDA_ABS		((byte) 0xAD, 3),
	LDX_ABS		((byte) 0xAE, 3),
	BCS_REL		((byte) 0xB0, 2),
	LDA_IND_Y	((byte) 0xB1, 2),
	LDY_ZP_X	((byte) 0xB4, 2),
	LDA_ZP_X	((byte) 0xB5, 2),
	LDX_ZP_Y	((byte) 0xB6, 2),
	CLV			((byte) 0xB8, 1),
	LDA_ABS_Y	((byte) 0xB9, 3),
	TSX			((byte) 0xBA, 1),
	LDY_ABS_X	((byte) 0xBC, 3),
	LDA_ABS_X	((byte) 0xBD, 3),
	LDX_ABS_Y	((byte) 0xBE, 3),
	CPY_IMM		((byte) 0xC0, 2),
	CMP_IND_X	((byte) 0xC1, 2),
	CPY_ZP		((byte) 0xC4, 2),
	CMP_ZP		((byte) 0xC5, 2),
	DEC_ZP		((byte) 0xC6, 2),
	INY			((byte) 0xC8, 1),
	CMP_IMM		((byte) 0xC9, 2),
	DEX			((byte) 0xCA, 1),
	CPY_ABS		((byte) 0xCC, 3),
	CMP_ABS		((byte) 0xCD, 3),
	DEC_ABS		((byte) 0xCE, 3),
	BNE_REL		((byte) 0xD0, 2),
	CMP_IND_Y	((byte) 0xD2, 2),
	CMP_ZP_X	((byte) 0xD5, 2),
	DEC_ZP_X	((byte) 0xD6, 2),
	CLD			((byte) 0xD8, 1),
	CMP_ABS_Y	((byte) 0xD9, 3),
	STP			((byte) 0xDB, 1),
	CMP_ABS_X	((byte) 0xDD, 3),
	DEC_ABS_X	((byte) 0xDE, 3),
	CPX_IMM		((byte) 0xE0, 2),
	SBC_IND_X	((byte) 0xE1, 2),
	CPX_ZP		((byte) 0xE4, 2),
	SBC_ZP		((byte) 0xE5, 2),
	INC_ZP		((byte) 0xE6, 2),
	INX			((byte) 0xE8, 1),
	SBC_IMM		((byte) 0xE9, 2),
	NOP			((byte) 0xEA, 1),
	CPX_ABS		((byte) 0xEC, 3),
	SBC_ABS		((byte) 0xED, 3),
	INC_ABS		((byte) 0xEE, 3),
	BEQ_REL		((byte) 0xF0, 2),
	SBC_IND_Y	((byte) 0xF1, 2),
	SBC_ZP_X	((byte) 0xF5, 2),
	INC_ZP_X	((byte) 0xF6, 2),
	SED			((byte) 0xF8, 1),
	SBC_ABS_Y	((byte) 0xF9, 3),
	SBC_ABS_X	((byte) 0xFD, 3),
	INC_ABS_X	((byte) 0xFE, 3),
	UNUSED		((byte) 0xFF, 1);
	
	/**
	 * A HashMap containing the hex value for each
	 * instruction and its corresponding enum. Used for the
	 * getInstruction helper function.
	 */
	private static final HashMap<Byte, InstructionComputer> instructions = new HashMap<Byte, InstructionComputer>();
	
	static {
		for(InstructionComputer instruction : InstructionComputer.values()) {
			instructions.put(instruction.getHexValue(), instruction);
		}
	}
	
	/**
	 * The corresponding hex value for this instruction.
	 */
	private final byte hexValue;
	
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
	private InstructionComputer(byte hexValue, int length) {
		this.hexValue = hexValue;
		this.length = length;
	}
	
	/**
	 * Returns the hex value of this instruction.
	 * 
	 * @return The hex value of this instruction
	 */
	public byte getHexValue() {
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
	public static InstructionComputer getInstruction(byte hexValue) {
		InstructionComputer instruction = instructions.get(hexValue);
		if(instruction == null) {return UNUSED;}
		return instruction;
	}
	
}
