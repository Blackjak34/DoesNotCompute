package com.github.blackjak34.compute.enums;

/**
 * Holds a bunch of constants used for rendering
 * GuiComputer.
 * 
 * @author Blackjak34
 * @since 1.1.0
 */
public enum GuiConstantComputer {
	BUTTON_WIDTH		(7),
	BUTTON_HEIGHT		(7),
	BUTTON_START		(0),
	BUTTON_START_X		(175),
	BUTTON_START_Y		(169),
	BUTTON_STP			(1),
	BUTTON_STP_X		(197),
	BUTTON_STP_Y		(169),
	BUTTON_RST			(2),
	BUTTON_RST_X		(219),
	BUTTON_RST_Y		(169),
	BUTTON_EJECT_WIDTH	(8),
	BUTTON_EJECT_HEIGHT	(4),
	BUTTON_EJECT		(3),
	BUTTON_EJECT_X		(25),
	BUTTON_EJECT_Y		(168),
	LIGHT_GRN_WIDTH		(8),
	LIGHT_GRN_HEIGHT	(4),
	LIGHT_GRN_X			(105),
	LIGHT_GRN_Y			(168),
	LIGHT_GRN_ACTIVE_X	(6),
	LIGHT_GRN_ACTIVE_Y	(198),
	LIGHT_STATE_WIDTH	(6),
	LIGHT_STATE_HEIGHT	(6),
	LIGHT_RUN_X			(142),
	LIGHT_RUN_Y			(168),
	LIGHT_HALT_X		(142),
	LIGHT_HALT_Y		(179),
	DISK_SLOT_WIDTH		(86),
	DISK_SLOT_HEIGHT	(3),
	DISK_SLOT_X			(26),
	DISK_SLOT_Y			(175),
	FLOPPY_DISK_X		(14),
	FLOPPY_DISK_Y		(198);
	
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
	private GuiConstantComputer(int value) {
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
