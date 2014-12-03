package com.github.blackjak34.compute.enums;

/**
 * Holds a bunch of constants used for rendering
 * {@link GuiComputer}.
 * 
 * @author Blackjak34
 * @since 1.1.0
 */
public enum GuiConstantComputer {
	IMG_BUTTON_WIDTH	(14),
	IMG_BUTTON_HEIGHT	(28),
	IMG_BUTTON_STP_X	(164),
	IMG_BUTTON_STP_Y	(160),
	IMG_BUTTON_RUN_X	(186),
	IMG_BUTTON_RUN_Y	(160),
	IMG_BUTTON_RST_X	(208),
	IMG_BUTTON_RST_Y	(160),
	BUTTON_WIDTH		(12),
	BUTTON_HEIGHT		(12),
	BUTTON_STP			(0),
	BUTTON_STP_X		(165),
	BUTTON_STP_Y		(174),
	BUTTON_RUN			(1),
	BUTTON_RUN_X		(187),
	BUTTON_RUN_Y		(174),
	BUTTON_RST			(2),
	BUTTON_RST_X		(209),
	BUTTON_RST_Y		(174),
	LIGHT_WIDTH			(6),
	LIGHT_HEIGHT		(2),
	LIGHT_RED_X			(34),
	LIGHT_RED_Y			(169),
	LIGHT_GRN_X			(114),
	LIGHT_GRN_Y			(169),
	LIGHT_RED_ACTIVE_X	(14),
	LIGHT_RED_ACTIVE_Y	(198),
	LIGHT_GRN_ACTIVE_X	(14),
	LIGHT_GRN_ACTIVE_Y	(200),
	DISK_SLOT_WIDTH		(86),
	DISK_SLOT_HEIGHT	(3),
	DISK_SLOT_X			(34),
	DISK_SLOT_Y			(175),
	FLOPPY_DISK_X		(20),
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
