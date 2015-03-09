package com.github.blackjak34.compute.enums;

import java.util.HashMap;

public enum CharacterComputer {
	SPACE				(32, 2, 0),
	EXCLAMATION			(33, 2, 1),
	DOUBLE_QUOTE		(34, 2, 2),
	HASH				(35, 2, 3),
	DOLLAR				(36, 2, 4),
	PERCENT				(37, 2, 5),
	AMPERSAND			(38, 2, 6),
	SINGLE_QUOTE		(39, 2, 7),
	OPEN_PARENTHESIS	(40, 2, 8),
	CLOSE_PARENTHESIS	(41, 2, 9),
	ASTERISK			(42, 2, 10),
	SUM					(43, 2, 11),
	COMMA				(44, 2, 12),
	DIFFERENCE			(45, 2, 13),
	DECIMAL				(46, 2, 14),
	FORWARD_SLASH		(47, 2, 15),
	ZERO				(48, 3, 0),
	ONE					(49, 3, 1),
	TWO					(50, 3, 2),
	THREE				(51, 3, 3),
	FOUR				(52, 3, 4),
	FIVE				(53, 3, 5),
	SIX					(54, 3, 6),
	SEVEN				(55, 3, 7),
	EIGHT				(56, 3, 8),
	NINE				(57, 3, 9),
	COLON				(58, 3, 10),
	SEMICOLON			(59, 3, 11),
	LESS_THAN			(60, 3, 12),
	EQUALS				(61, 3, 13),
	GREATER_THAN		(62, 3, 14),
	QUESTION			(63, 3, 15),
	AT					(64, 0, 0),
	CAPITAL_A			(65, 4, 1),
	CAPITAL_B			(66, 4, 2),
	CAPITAL_C			(67, 4, 3),
	CAPITAL_D			(68, 4, 4),
	CAPITAL_E			(69, 4, 5),
	CAPITAL_F			(70, 4, 6),
	CAPITAL_G			(71, 4, 7),
	CAPITAL_H			(72, 4, 8),
	CAPITAL_I			(73, 4, 9),
	CAPITAL_J			(74, 4, 10),
	CAPITAL_K			(75, 4, 11),
	CAPITAL_L			(76, 4, 12),
	CAPITAL_M			(77, 4, 13),
	CAPITAL_N			(78, 4, 14),
	CAPITAL_O			(79, 4, 15),
	CAPITAL_P			(80, 5, 0),
	CAPITAL_Q			(81, 5, 1),
	CAPITAL_R			(82, 5, 2),
	CAPITAL_S			(83, 5, 3),
	CAPITAL_T			(84, 5, 4),
	CAPITAL_U			(85, 5, 5),
	CAPITAL_V			(86, 5, 6),
	CAPITAL_W			(87, 5, 7),
	CAPITAL_X			(88, 5, 8),
	CAPITAL_Y			(89, 5, 9),
	CAPITAL_Z			(90, 5, 10),
	OPEN_BRACKET		(91, 1, 11),
	BACKWARD_SLASH		(92, 2, 15),
	CLOSE_BRACKET		(93, 1, 13),
	CAROT				(94, 2, 0),
	UNDERSCORE			(95, 6, 4),
	BACKTICK			(96, 2, 7),
	LOWER_A				(97, 0, 1),
	LOWER_B				(98, 0, 2),
	LOWER_C				(99, 0, 3),
	LOWER_D				(100, 0, 4),
	LOWER_E				(101, 0, 5),
	LOWER_F				(102, 0, 6),
	LOWER_G				(103, 0, 7),
	LOWER_H				(104, 0, 8),
	LOWER_I				(105, 0, 9),
	LOWER_J				(106, 0, 10),
	LOWER_K				(107, 0, 11),
	LOWER_L				(108, 0, 12),
	LOWER_M				(109, 0, 13),
	LOWER_N				(110, 0, 14),
	LOWER_O				(111, 0, 15),
	LOWER_P				(112, 1, 0),
	LOWER_Q				(113, 1, 1),
	LOWER_R				(114, 1, 2),
	LOWER_S				(115, 1, 3),
	LOWER_T				(116, 1, 4),
	LOWER_U				(117, 1, 5),
	LOWER_V				(118, 1, 6),
	LOWER_W				(119, 1, 7),
	LOWER_X				(120, 1, 8),
	LOWER_Y				(121, 1, 9),
	LOWER_Z				(122, 1, 10),
	OPEN_BRACE			(123, 1, 11),
	VERTICAL_BAR		(124, 5, 13),
	CLOSE_BRACE			(125, 1, 13),
	TILDE				(126, 2, 0),
	DELETE				(127, 1, 15),
	INVALID				(-1, 2, 0);

	private final static HashMap<Integer, CharacterComputer> characters = new HashMap<Integer, CharacterComputer>();
	
	static {
		for(CharacterComputer character : CharacterComputer.values()) {
			characters.put(character.getCharCode(), character);
		}
	}

	private final int charCode;
	private final int uValue;
	private final int vValue;

	private CharacterComputer(int charCode, int uValue, int vValue) {
		this.charCode = charCode;
		this.uValue = uValue;
		this.vValue = vValue;
	}

	public int getCharCode() {
		return charCode;
	}

	public int getUValue() {
		return uValue;
	}

	public int getVValue() {
		return vValue;
	}

	public static CharacterComputer getCharacter(int charCode) {
		CharacterComputer character = characters.get(charCode);
		if(character == null) {return INVALID;}
		return character;
	}
	
}
