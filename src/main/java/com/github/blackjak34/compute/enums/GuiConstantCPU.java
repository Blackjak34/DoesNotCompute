package com.github.blackjak34.compute.enums;

/**
 * Holds a bunch of constants used for rendering
 * GuiComputer.
 * 
 * @author Blackjak34
 * @since 1.1.0
 */
public enum GuiConstantCPU {
	BUTTON_WIDTH		(19),
	BUTTON_HEIGHT		(12),
	BUTTON_START		(0),
	BUTTON_START_X		(227),
	BUTTON_START_Y		(46),
	BUTTON_STP			(1),
	BUTTON_STP_X		(227),
	BUTTON_STP_Y		(59),
	BUTTON_RST			(2),
	BUTTON_RST_X		(227),
	BUTTON_RST_Y		(72),
	BUTTON_DUMP			(3),
	BUTTON_DUMP_X		(227),
	BUTTON_DUMP_Y		(85),
	LIGHT_STATE_WIDTH	(7),
	LIGHT_STATE_HEIGHT	(7),
	LIGHT_RUN_X			(235),
	LIGHT_RUN_Y			(15);
	
	/**
	 * The numeric value of this constant. The actual units
	 * that this is in varies depending on what constant it
	 * actually is.
	 */
	private final int value;
	
	/**
	 * Loads all of the values into their respective enums.
	 * 
	 * @param value The value of this constant
	 */
	private GuiConstantCPU(int value) {
		this.value = value;
	}
	
	/**
	 * Returns the value of this constant. The actual units
	 * that this value is in varies depending on what
	 * constant it is; for most it is in pixels.
	 * 
	 * @return The value of this constant
	 */
	public int getValue() {
		return value;
	}
}
