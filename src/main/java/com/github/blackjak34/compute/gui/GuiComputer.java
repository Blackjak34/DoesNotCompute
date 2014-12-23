package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.Compute;
import com.github.blackjak34.compute.container.ContainerComputer;
import com.github.blackjak34.compute.entity.tile.TileEntityComputer;
import com.github.blackjak34.compute.enums.CharacterComputer;
import com.github.blackjak34.compute.packet.MessageButtonClicked;
import com.github.blackjak34.compute.packet.MessageKeyPressed;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static com.github.blackjak34.compute.enums.GuiConstantComputer.*;

/**
 * The GUI interface to the BlockComputer and
 * {@link TileEntityComputer} that together comprise the
 * 6502 emulator. Accepts keyboard input from the player
 * and feeds it into the computer accordingly.
 * 
 * @author Blackjak34
 * @since 1.0.1
 */
public class GuiComputer extends GuiContainer {
	
	/**
	 * The mod-specific identifier for this GUI. Only used
	 * to differentiate between different GUIs when selecting
	 * one to be opened. See GuiConstantComputer for
	 * all other constants.
	 */
	public static final int GUIID = 42;
	
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
	private static final Tessellator tessellator = Tessellator.getInstance();
	
	/**
	 * The file path to where the main GUI texture is located.
	 */
	private static final ResourceLocation guiTextureLoc = new ResourceLocation("doesnotcompute:textures/gui/Computer_Gui4.png");
	
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
		super(new ContainerComputer(tiledata));
		
		this.tiledata = tiledata;
		xSize = 256;
		ySize = 198;
	}

	/**
	 * Sets up the buttons in the GUI.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();

		GuiButton buttonStop = new GuiButton(BUTTON_STP.getValue(),
				guiLeft+BUTTON_STP_X.getValue(), guiTop+BUTTON_STP_Y.getValue(),
				BUTTON_WIDTH.getValue(), BUTTON_HEIGHT.getValue(), "");
		GuiButton buttonRun = new GuiButton(BUTTON_RUN.getValue(),
				guiLeft+BUTTON_RUN_X.getValue(), guiTop+BUTTON_RUN_Y.getValue(),
				BUTTON_WIDTH.getValue(), BUTTON_HEIGHT.getValue(), "");
		GuiButton buttonReset = new GuiButton(BUTTON_RST.getValue(),
				guiLeft+BUTTON_RST_X.getValue(), guiTop+BUTTON_RST_Y.getValue(),
				BUTTON_WIDTH.getValue(), BUTTON_HEIGHT.getValue(), "");
		GuiButton buttonEject = new GuiButton(BUTTON_EJECT.getValue(),
				guiLeft+BUTTON_EJECT_X.getValue(), guiTop+BUTTON_EJECT_Y.getValue(),
				BUTTON_EJECT_WIDTH.getValue(), BUTTON_EJECT_HEIGHT.getValue(), "");

		buttonList.add(BUTTON_STP.getValue(), buttonStop);
		buttonList.add(BUTTON_RUN.getValue(), buttonRun);
		buttonList.add(BUTTON_RST.getValue(), buttonReset);
		buttonList.add(BUTTON_EJECT.getValue(), buttonEject);
	}

	/**
	 * Called by Minecraft whenever a button on this GUI is pressed. The
	 * only thing that this function does is send a packet to the server
	 * with the button id to be processed.
	 *
	 * @param button The button that was clicked
	 */
	@Override
	public void actionPerformed(GuiButton button) {
		Compute.networkWrapper.sendToServer(new MessageButtonClicked(button.id));
	}
	
	/**
	 * Simply specifies that this GUI does not pause the game
	 * when opened.
	 */
	@Override
	public boolean doesGuiPauseGame() {return false;}
	
	/**
	 * Draws the screen of this GUI. This is overrided to
	 * avoid all of the fancy logic in the default method
	 * because we aren't actually rendering any ItemStacks,
	 * just normal GUI stuff.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
	}
	
	/**
	 * The meat of the GUI code. Draws in the GUI background
	 * using the tessellator and then individually draws in
	 * each character on the screen from the charset depending
	 * on the data stored in the {@link TileEntityComputer}'s
	 * screen buffer.
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		// Switches the color to default and binds the texture to prepare for rendering
		GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
		mc.renderEngine.bindTexture(guiTextureLoc);
		
		// Finds the corner on the screen to start drawing the GUI from as to center it
		int coordX = (width - xSize) / 2;
		int coordY = (height - ySize) / 2;
		
		// Draws in the GUI background
		drawTexturedModalRect(coordX, coordY, 0, 0, xSize, ySize);
		
		// Switches to a nice green color for the text and binds the charset texture
		GL11.glColor4d(0.10196078, 0.31372549, 0.0, 1.0);
		mc.renderEngine.bindTexture(charsetLoc);
		
		// Fetches the current world time, will be relevant later when drawing the cursor
		long time = mc.theWorld.getWorldTime();

		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		// Iterates through each character on the screen
		for(int screenColumn=0;screenColumn<80;screenColumn++) {
			for(int screenRow=0;screenRow<50;screenRow++) {
				// Finds the corner on the screen to start drawing the character from
				int screenPositionX = coordX + 8 + (screenColumn * 3);
				int screenPositionY = coordY + 8 + (screenRow * 3);
				
				// Retrieves the data about this character from the screen buffer
				byte charAtLocation = tiledata.getCharAt(screenColumn, screenRow);
				
				// Translates the data into charset data and converts position in the charset to pixels
				CharacterComputer charSprite = CharacterComputer.getCharacter(charAtLocation);
				int charU = charSprite.getUValue() * 8;
				int charV = charSprite.getVValue() * 8;

				// Renders in the character with the tessellator, multiplying by the scale factor to convert pixels to uv coords
				worldRenderer.startDrawingQuads();
				worldRenderer.addVertexWithUV(screenPositionX, screenPositionY, zLevel, charU*UV_SCALE, charV*UV_SCALE);
				worldRenderer.addVertexWithUV(screenPositionX, screenPositionY + 3, zLevel, charU * UV_SCALE, (charV + 8) * UV_SCALE);
				worldRenderer.addVertexWithUV(screenPositionX + 3, screenPositionY + 3, zLevel, (charU + 8) * UV_SCALE, (charV + 8) * UV_SCALE);
				worldRenderer.addVertexWithUV(screenPositionX + 3, screenPositionY, zLevel, (charU + 8) * UV_SCALE, charV * UV_SCALE);
				tessellator.draw();
				
				// Draws in the cursor on the screen as a solid blinking block of green
				if(screenColumn == tiledata.cursorX && screenRow == tiledata.cursorY && ((time >> 2) & 1L) > 0L) {
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					worldRenderer.startDrawingQuads();
					worldRenderer.addVertex(screenPositionX, screenPositionY, zLevel);
					worldRenderer.addVertex(screenPositionX, screenPositionY+3, zLevel);
					worldRenderer.addVertex(screenPositionX+3, screenPositionY+3, zLevel);
					worldRenderer.addVertex(screenPositionX+3, screenPositionY, zLevel);
					tessellator.draw();
					GL11.glEnable(GL11.GL_TEXTURE_2D);
				}
			}
		}
		
		GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
		mc.renderEngine.bindTexture(guiTextureLoc);

		if(tiledata.isRunning()) {
			drawTexturedModalRect(coordX + IMG_BUTTON_RUN_X.getValue(), coordY + IMG_BUTTON_RUN_Y.getValue(),
					0, ySize,
					IMG_BUTTON_WIDTH.getValue(), IMG_BUTTON_HEIGHT.getValue());
		} else {
			drawTexturedModalRect(coordX + IMG_BUTTON_STP_X.getValue(), coordY + IMG_BUTTON_STP_Y.getValue(),
					0, ySize,
					IMG_BUTTON_WIDTH.getValue(), IMG_BUTTON_HEIGHT.getValue());
		}

		// Draws the floppy disk in the drive slot if one is inserted into the computer.
		if(tiledata.isFloppyInDrive()) {
			drawTexturedModalRect(coordX + DISK_SLOT_X.getValue(), coordY + DISK_SLOT_Y.getValue(),
					FLOPPY_DISK_X.getValue(), FLOPPY_DISK_Y.getValue(),
					DISK_SLOT_WIDTH.getValue(), DISK_SLOT_HEIGHT.getValue());
		}
	}
	
	/**
	 * Checks to see if a char typed by the player is either:
	 * in the charset, the escape key, the enter key, or
	 * the backspace key, and sends a packet to the server
	 * for handling accordingly.
	 */
	@Override
	protected void keyTyped(char charTyped, int lwjglCode) {
		if(lwjglCode == 1) {mc.thePlayer.closeScreen();}
		
		if(CharacterComputer.getCharacter(charTyped) != CharacterComputer.INVALID ||
				charTyped == 13 || (charTyped == 8 && tiledata.cursorX > 0)) {
			Compute.networkWrapper.sendToServer(new MessageKeyPressed(charTyped));

			// Plays a key pressing sound
			mc.thePlayer.playSound("doesnotcompute:computer.keypress", 1.0f, 1.0f);
		}
	}
	
}
