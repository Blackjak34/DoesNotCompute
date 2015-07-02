package com.github.blackjak34.compute.enums;

import java.util.HashMap;

public enum CharacterComputer {
	SPACE				(32, 2, 0, 51),
	EXCLAMATION			(33, 2, 1, 44),
	DOUBLE_QUOTE		(34, 2, 2, 62),
	HASH				(35, 2, 3, 58),
	DOLLAR				(36, 2, 4, 45),
	PERCENT				(37, 2, 5, 53),
	AMPERSAND			(38, 2, 6, 36),
	SINGLE_QUOTE		(39, 2, 7, 60),
	OPEN_PARENTHESIS	(40, 2, 8, 40),
	CLOSE_PARENTHESIS	(41, 2, 9, 47),
	ASTERISK			(42, 2, 10, 46),
	SUM					(43, 2, 11, 41),
	COMMA				(44, 2, 12, 52),
	DIFFERENCE			(45, 2, 13, 43),
	DECIMAL				(46, 2, 14, 38),
	FORWARD_SLASH		(47, 2, 15, 50),
	ZERO				(48, 3, 0, 0),
	ONE					(49, 3, 1, 1),
	TWO					(50, 3, 2, 2),
	THREE				(51, 3, 3, 3),
	FOUR				(52, 3, 4, 4),
	FIVE				(53, 3, 5, 5),
	SIX					(54, 3, 6, 6),
	SEVEN				(55, 3, 7, 7),
	EIGHT				(56, 3, 8, 8),
	NINE				(57, 3, 9, 9),
	COLON				(58, 3, 10, 57),
	SEMICOLON			(59, 3, 11, 48),
	LESS_THAN			(60, 3, 12, 39),
	EQUALS				(61, 3, 13, 61),
	GREATER_THAN		(62, 3, 14, 55),
	QUESTION			(63, 3, 15, 56),
	AT					(64, 0, 0, 59),
	CAPITAL_A			(65, 4, 1, 10),
	CAPITAL_B			(66, 4, 2, 11),
	CAPITAL_C			(67, 4, 3, 12),
	CAPITAL_D			(68, 4, 4, 13),
	CAPITAL_E			(69, 4, 5, 14),
	CAPITAL_F			(70, 4, 6, 15),
	CAPITAL_G			(71, 4, 7, 16),
	CAPITAL_H			(72, 4, 8, 17),
	CAPITAL_I			(73, 4, 9, 18),
	CAPITAL_J			(74, 4, 10, 19),
	CAPITAL_K			(75, 4, 11, 20),
	CAPITAL_L			(76, 4, 12, 21),
	CAPITAL_M			(77, 4, 13, 22),
	CAPITAL_N			(78, 4, 14, 23),
	CAPITAL_O			(79, 4, 15, 24),
	CAPITAL_P			(80, 5, 0, 25),
	CAPITAL_Q			(81, 5, 1, 26),
	CAPITAL_R			(82, 5, 2, 27),
	CAPITAL_S			(83, 5, 3, 28),
	CAPITAL_T			(84, 5, 4, 29),
	CAPITAL_U			(85, 5, 5, 30),
	CAPITAL_V			(86, 5, 6, 31),
	CAPITAL_W			(87, 5, 7, 32),
	CAPITAL_X			(88, 5, 8, 33),
	CAPITAL_Y			(89, 5, 9, 34),
	CAPITAL_Z			(90, 5, 10, 35),
	OPEN_BRACKET		(91, 1, 11, 63),
	BACKWARD_SLASH		(92, 2, 15, 63),
	CLOSE_BRACKET		(93, 1, 13, 63),
	CAROT				(94, 2, 0, 63),
	UNDERSCORE			(95, 6, 4, 54),
	BACKTICK			(96, 2, 7, 63),
	LOWER_A				(97, 0, 1, 10),
	LOWER_B				(98, 0, 2, 11),
	LOWER_C				(99, 0, 3, 12),
	LOWER_D				(100, 0, 4, 13),
	LOWER_E				(101, 0, 5, 14),
	LOWER_F				(102, 0, 6, 15),
	LOWER_G				(103, 0, 7, 16),
	LOWER_H				(104, 0, 8, 17),
	LOWER_I				(105, 0, 9, 18),
	LOWER_J				(106, 0, 10, 19),
	LOWER_K				(107, 0, 11, 20),
	LOWER_L				(108, 0, 12, 21),
	LOWER_M				(109, 0, 13, 22),
	LOWER_N				(110, 0, 14, 23),
	LOWER_O				(111, 0, 15, 24),
	LOWER_P				(112, 1, 0, 25),
	LOWER_Q				(113, 1, 1, 26),
	LOWER_R				(114, 1, 2, 27),
	LOWER_S				(115, 1, 3, 28),
	LOWER_T				(116, 1, 4, 29),
	LOWER_U				(117, 1, 5, 30),
	LOWER_V				(118, 1, 6, 31),
	LOWER_W				(119, 1, 7, 32),
	LOWER_X				(120, 1, 8, 33),
	LOWER_Y				(121, 1, 9, 34),
	LOWER_Z				(122, 1, 10, 35),
	OPEN_BRACE			(123, 1, 11, 63),
	VERTICAL_BAR		(124, 5, 13, 42),
	CLOSE_BRACE			(125, 1, 13, 63),
	TILDE				(126, 2, 0, 63),
	DELETE				(127, 1, 15, 63),
    SPACE_I				(160, 10, 0, 51),
    EXCLAMATION_I		(161, 10, 1, 44),
    DOUBLE_QUOTE_I		(162, 10, 2, 62),
    HASH_I				(163, 10, 3, 58),
    DOLLAR_I			(164, 10, 4, 45),
    PERCENT_I			(165, 10, 5, 53),
    AMPERSAND_I			(166, 10, 6, 36),
    SINGLE_QUOTE_I		(167, 10, 7, 60),
    OPEN_PARENTHESIS_I	(168, 10, 8, 40),
    CLOSE_PARENTHESIS_I	(169, 10, 9, 47),
    ASTERISK_I			(170, 10, 10, 46),
    SUM_I				(171, 10, 11, 41),
    COMMA_I				(172, 10, 12, 52),
    DIFFERENCE_I		(173, 10, 13, 43),
    DECIMAL_I			(174, 10, 14, 38),
    FORWARD_SLASH_I		(175, 10, 15, 50),
    ZERO_I				(176, 11, 0, 0),
    ONE_I				(177, 11, 1, 1),
    TWO_I				(178, 11, 2, 2),
    THREE_I				(179, 11, 3, 3),
    FOUR_I				(180, 11, 4, 4),
    FIVE_I				(181, 11, 5, 5),
    SIX_I				(182, 11, 6, 6),
    SEVEN_I				(183, 11, 7, 7),
    EIGHT_I				(184, 11, 8, 8),
    NINE_I				(185, 11, 9, 9),
    COLON_I				(186, 11, 10, 57),
    SEMICOLON_I			(187, 11, 11, 48),
    LESS_THAN_I			(188, 11, 12, 39),
    EQUALS_I			(189, 11, 13, 61),
    GREATER_THAN_I		(190, 11, 14, 55),
    QUESTION_I			(191, 11, 15, 56),
    AT_I				(192, 8, 0, 59),
    CAPITAL_A_I			(193, 12, 1, 10),
    CAPITAL_B_I			(194, 12, 2, 11),
    CAPITAL_C_I			(195, 12, 3, 12),
    CAPITAL_D_I			(196, 12, 4, 13),
    CAPITAL_E_I			(197, 12, 5, 14),
    CAPITAL_F_I			(198, 12, 6, 15),
    CAPITAL_G_I			(199, 12, 7, 16),
    CAPITAL_H_I			(200, 12, 8, 17),
    CAPITAL_I_I			(201, 12, 9, 18),
    CAPITAL_J_I			(202, 12, 10, 19),
    CAPITAL_K_I			(203, 12, 11, 20),
    CAPITAL_L_I			(204, 12, 12, 21),
    CAPITAL_M_I			(205, 12, 13, 22),
    CAPITAL_N_I			(206, 12, 14, 23),
    CAPITAL_O_I			(207, 12, 15, 24),
    CAPITAL_P_I			(208, 13, 0, 25),
    CAPITAL_Q_I			(209, 13, 1, 26),
    CAPITAL_R_I			(210, 13, 2, 27),
    CAPITAL_S_I			(211, 13, 3, 28),
    CAPITAL_T_I			(212, 13, 4, 29),
    CAPITAL_U_I			(213, 13, 5, 30),
    CAPITAL_V_I			(214, 13, 6, 31),
    CAPITAL_W_I			(215, 13, 7, 32),
    CAPITAL_X_I			(216, 13, 8, 33),
    CAPITAL_Y_I			(217, 13, 9, 34),
    CAPITAL_Z_I			(218, 13, 10, 35),
    OPEN_BRACKET_I		(219, 9, 11, 63),
    BACKWARD_SLASH_I	(220, 10, 15, 63),
    CLOSE_BRACKET_I		(221, 9, 13, 63),
    CAROT_I				(222, 10, 0, 63),
    UNDERSCORE_I		(223, 14, 4, 54),
    BACKTICK_I			(224, 10, 7, 63),
    LOWER_A_I			(225, 8, 1, 10),
    LOWER_B_I			(226, 8, 2, 11),
    LOWER_C_I			(227, 8, 3, 12),
    LOWER_D_I			(228, 8, 4, 13),
    LOWER_E_I			(229, 8, 5, 14),
    LOWER_F_I			(230, 8, 6, 15),
    LOWER_G_I			(231, 8, 7, 16),
    LOWER_H_I			(232, 8, 8, 17),
    LOWER_I_I			(233, 8, 9, 18),
    LOWER_J_I			(234, 8, 10, 19),
    LOWER_K_I			(235, 8, 11, 20),
    LOWER_L_I			(236, 8, 12, 21),
    LOWER_M_I			(237, 8, 13, 22),
    LOWER_N_I			(238, 8, 14, 23),
    LOWER_O_I			(239, 8, 15, 24),
    LOWER_P_I			(240, 9, 0, 25),
    LOWER_Q_I			(241, 9, 1, 26),
    LOWER_R_I			(242, 9, 2, 27),
    LOWER_S_I			(243, 9, 3, 28),
    LOWER_T_I			(244, 9, 4, 29),
    LOWER_U_I			(245, 9, 5, 30),
    LOWER_V_I			(246, 9, 6, 31),
    LOWER_W_I			(247, 9, 7, 32),
    LOWER_X_I			(248, 9, 8, 33),
    LOWER_Y_I			(249, 9, 9, 34),
    LOWER_Z_I			(250, 9, 10, 35),
    OPEN_BRACE_I		(251, 9, 11, 63),
    VERTICAL_BAR_I		(252, 13, 13, 42),
    CLOSE_BRACE_I		(253, 9, 13, 63),
    TILDE_I				(254, 10, 0, 63),
    DELETE_I			(255, 9, 15, 63),
	INVALID				(-1, 2, 0, 63);

	private final static HashMap<Integer, CharacterComputer> characters = new HashMap<Integer, CharacterComputer>();

	static {
		for(CharacterComputer character : CharacterComputer.values()) {
			characters.put(character.getCharCode(), character);
		}
	}

	private final int charCode;
	private final int uValue;
	private final int vValue;
    private final int printedSymbol;

	CharacterComputer(int charCode, int uValue, int vValue, int printedSymbol) {
		this.charCode = charCode;
		this.uValue = uValue;
		this.vValue = vValue;
        this.printedSymbol = printedSymbol;
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

    public int getPrintedSymbol() {
        return printedSymbol;
    }

	public static CharacterComputer getCharacter(int charCode) {
		CharacterComputer character = characters.get(charCode);
		if(character == null) {return INVALID;}
		return character;
	}

}
