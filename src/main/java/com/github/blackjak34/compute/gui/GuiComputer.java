package com.github.blackjak34.compute.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

import com.github.blackjak34.compute.entity.tile.TileEntityComputer;
import com.github.blackjak34.compute.enums.CharacterComputer;
import com.github.blackjak34.compute.enums.InstructionComputer;
import com.github.blackjak34.compute.enums.StateComputer;

/**
 * The GUI interface to the {@link BlockComputer} and
 * {@link TileEntityComputer} that together comprise the
 * 6502 emulator. Accepts keyboard input from the player
 * and feeds it into the computer accordingly.
 * 
 * @author Blackjak34
 * @since 1.0.1
 */
public class GuiComputer extends GuiScreen {
	
	/**
	 * The mod-specific identifier for this GUI. Only used
	 * to differentiate between different GUIs when selecting
	 * one to be opened.
	 */
	public static final int GUI_ID = 42;
	
	/**
	 * The width of the texture that this gui uses, in pixels.
	 */
	private static final int GUI_WIDTH = 256;
	
	/**
	 * The height of the texture that this gui uses, in pixels.
	 */
	private static final int GUI_HEIGHT = 166;
	
	/**
	 * A special constant equal to 1/256. When the gui texture
	 * is stored within a 256x256 image, a pixel count can be
	 * multiplied by this constant to convert to uv coordinates
	 * (a range from 0 to 1).
	 */
	private static final double UV_SCALE = 0.00390625;
	
	/**
	 * An instance of Minecraft's tessellator, used for custom
	 * rendering.
	 */
	private static final Tessellator tessellator = Tessellator.instance;
	
	/**
	 * The file path to where the main GUI texture is located.
	 */
	private static final ResourceLocation guiTextureLoc = new ResourceLocation("doesnotcompute:textures/gui/Computer_Gui2.png");
	
	/**
	 * The file path to where the Computer's charset is located.
	 */
	private static final ResourceLocation charsetLoc = new ResourceLocation("doesnotcompute:textures/gui/Computer_Charset3.png");
	
	/**
	 * The {@link TileEntityComputer} that this GUI is displaying
	 * the contents of.
	 */
	private TileEntityComputer tiledata;
	
	/**
	 * This constructor only serves to assign the TileEntity
	 * to its associated variable for later use.
	 * @param tiledata The TileEntityComputer that this GUI represents
	 */
	public GuiComputer(TileEntityComputer tiledata) {
		this.tiledata = tiledata;
	}
	
	/**
	 * Simply specifies that this GUI does not pause the game
	 * when opened.
	 */
	@Override
	public boolean doesGuiPauseGame() {return false;}
	
	/**
	 * The meat of the GUI code. Draws in the GUI background
	 * using the tessellator and then individually draws in
	 * each character on the screen from the charset depending
	 * on the data stored in the {@link TileEntityComputer}'s
	 * screen buffer.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Switches the color to default and binds the texture to prepare for rendering
		GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
		mc.renderEngine.bindTexture(guiTextureLoc);
		
		// Finds the corner on the screen to start drawing the GUI from as to center it
		int coordX = (width - GUI_WIDTH) / 2;
		int coordY = (height - GUI_HEIGHT) / 2;
		
		// Draws in the GUI background
		drawTexturedModalRect(coordX, coordY, 0, 0, GUI_WIDTH, GUI_HEIGHT);
		
		// Switches to a nice green color for the text and binds the charset texture
		GL11.glColor4d(0.10196078, 0.31372549, 0.0, 1.0);
		mc.renderEngine.bindTexture(charsetLoc);
		
		// Fetches the current world time, will be relevant later when drawing the cursor
		long time = mc.theWorld.getWorldTime();
		
		// Iterates through each character on the screen
		for(int screenColumn=0;screenColumn<80;screenColumn++) {
			for(int screenRow=0;screenRow<50;screenRow++) {
				// Finds the corner on the screen to start drawing the character from
				int screenPositionX = coordX + 8 + (screenColumn * 3);
				int screenPositionY = coordY + 8 + (screenRow * 3);
				
				// Retrieves the data about this character from the screen buffer
				byte charAtLocation = tiledata.screenBuffer[screenColumn][screenRow];
				
				// Translates the data into charset data and converts position in the charset to pixels
				CharacterComputer charSprite = CharacterComputer.getCharacter(charAtLocation);
				int charU = charSprite.getUValue() * 8;
				int charV = charSprite.getVValue() * 8;
				
				// Renders in the character with the tessellator, multiplying by the scale factor to convert pixels to uv coords
				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(screenPositionX, screenPositionY, zLevel, charU*UV_SCALE, charV*UV_SCALE);
				tessellator.addVertexWithUV(screenPositionX, screenPositionY+3, zLevel, charU*UV_SCALE, (charV+8)*UV_SCALE);
				tessellator.addVertexWithUV(screenPositionX+3, screenPositionY+3, zLevel, (charU+8)*UV_SCALE, (charV+8)*UV_SCALE);
				tessellator.addVertexWithUV(screenPositionX+3, screenPositionY, zLevel, (charU+8)*UV_SCALE, charV*UV_SCALE);
				tessellator.draw();
				
				// Draws in the cursor on the screen as a solid blinking block of green
				if(screenColumn == tiledata.cursorX && screenRow == tiledata.cursorY && ((time >> 2) & 1L) > 0L) {
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					tessellator.startDrawingQuads();
					tessellator.addVertex(screenPositionX, screenPositionY, zLevel);
					tessellator.addVertex(screenPositionX, screenPositionY+3, zLevel);
					tessellator.addVertex(screenPositionX+3, screenPositionY+3, zLevel);
					tessellator.addVertex(screenPositionX+3, screenPositionY, zLevel);
					tessellator.draw();
					GL11.glEnable(GL11.GL_TEXTURE_2D);
				}
			}
		}
	}
	
	/**
	 * A huge function that processes keyboard input to the
	 * GUI. Special keys are escape to leave the GUI, backspace
	 * to delete a character, and enter to plug in a command.
	 * Because this function is messy and leaves the computer
	 * as a hardcoded half-chat based assembler, this will
	 * certainly be recoded in the near future.
	 */
	@Override
	public void keyTyped(char charTyped, int lwjglCode) {
		// Closes the GUI if escape is pressed
		if(lwjglCode == 1) {mc.thePlayer.closeScreen();} else
		
		// If the key typed is in the charset
		if(CharacterComputer.getCharacter(charTyped) != CharacterComputer.INVALID) {
			// Put the ascii code for the char that was typed into the screen buffer at the cursor
			tiledata.screenBuffer[tiledata.cursorX][tiledata.cursorY] = (byte) charTyped;
			
			// Move the cursor to the right, wrap if needed
			tiledata.cursorX++;
			tiledata.cursorX %= 80;
		// If the enter key was pressed
		} else if(charTyped == 13) {
			// Initialize the StringBuilder (efficient way to concentate lots of Strings)
			StringBuilder lineText = new StringBuilder();
			
			// Read all of the data from the left side of the current row up to the cursor
			for(int screenColumn = 0;screenColumn<tiledata.cursorX;screenColumn++) {
				// Get the data at the position, convert from charset data into ascii, and add onto the StringBuilder
				lineText.append((char) CharacterComputer.getCharacter(tiledata.screenBuffer[screenColumn][tiledata.cursorY]).getCharCode());
			}
			// Move the cursor down and all the way to the left, wrap if needed
			tiledata.cursorY++;
			tiledata.cursorY %= 50;
			tiledata.cursorX = 0;
			
			// Break up the built string to analyze it for a typed instruction
			String[] split = lineText.toString().split(" ", 4);
			
			// Figure out what instruction was typed, print error message if it was invalid
			InstructionComputer instruction = InstructionComputer.UNUSED;
			try {
				instruction = InstructionComputer.valueOf(split[0]);
			} catch(IllegalArgumentException e) {
				mc.thePlayer.addChatMessage(new ChatComponentText("That isn't a valid instruction."));
				return;
			}
			
			// Make sure enough args were added after the instruction for it to work
			if(split.length < instruction.getLength()) {
				mc.thePlayer.addChatMessage(new ChatComponentText("Not enough arguments."));
				return;
			}
			
			// Stop the computer
			tiledata.setState(StateComputer.RESET);
			
			// Write the instruction and its args into computer memory accordingly
			switch(instruction.getLength()) {
				case 3:
					int arg3;
					try {
						arg3 = Integer.parseInt(split[2]);
					} catch(NumberFormatException e) {
						mc.thePlayer.addChatMessage(new ChatComponentText("The second argument isn't a valid number."));
						return;
					}
					tiledata.writeMemory((short) 2, (byte) arg3);
				//$FALL-THROUGH$
				case 2:
					int arg2;
					try {
						arg2 = Integer.parseInt(split[1]);
					} catch(NumberFormatException e) {
						mc.thePlayer.addChatMessage(new ChatComponentText("The first argument isn't a valid number."));
						return;
					}
					tiledata.writeMemory((short) 1, (byte) arg2);
				//$FALL-THROUGH$
				case 1:
					tiledata.writeMemory((short) 0, instruction.getHexValue());
			}
			
			// Set the program counter to the instruction just written
			tiledata.setProgramCounter((short) 0);
			
			// Start up the computer
			tiledata.setState(StateComputer.RUN);
			
			// Tick the computer once to make it run the instruction
			tiledata.updateEntity();
			
			// Stop the computer again
			tiledata.setState(StateComputer.HALT);
			
			// Send back a String representation of the computer in the chat
			mc.thePlayer.addChatMessage(new ChatComponentText(tiledata.toString()));
		// If backspace was pressed and the cursor isn't all the way back already
		} else if(charTyped == 8 && tiledata.cursorX > 0) {
			// Move the cursor back one space if it isn't already all the way back
			tiledata.cursorX--;
			
			// Erase the data in the screen buffer at the new cursor location
			tiledata.screenBuffer[tiledata.cursorX][tiledata.cursorY] = ' ';
		}
	}
	
}
