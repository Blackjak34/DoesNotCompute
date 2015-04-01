package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.container.ContainerBase;
import com.github.blackjak34.compute.entity.tile.client.TileEntityTerminalClient;
import com.github.blackjak34.compute.enums.CharacterComputer;
import com.github.blackjak34.compute.packet.MessageKeyTyped;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiTerminal extends GuiContainer {

	public static final int GUIID = 42;
	private static final double UV_SCALE = 0.00390625;

	private static final Tessellator tessellator = Tessellator.getInstance();
	private static final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

	private static final ResourceLocation guiTextureLoc = new ResourceLocation("doesnotcompute:textures/gui/Gui_Terminal.png");
	private static final ResourceLocation charsetLoc = new ResourceLocation("doesnotcompute:textures/gui/Gui_Terminal_Charset.png");

	private TileEntityTerminalClient tiledata;

	public GuiTerminal(TileEntityTerminalClient tiledata) {
		super(new ContainerBase(tiledata));
		
		this.tiledata = tiledata;
		xSize = 256;
		ySize = 166;
	}

	@Override
	public boolean doesGuiPauseGame() {return false;}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
	}

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
		long time = mc.theWorld.getTotalWorldTime();

		int cursorX = tiledata.getCursorX();
		int cursorY = tiledata.getCursorY();
		int cursorMode = tiledata.getCursorMode();
		// Iterates through each character on the screen
		for(int screenColumn=0;screenColumn<80;screenColumn++) {
			for(int screenRow=0;screenRow<50;screenRow++) {
				// Finds the corner on the screen to start drawing the character from
				int screenPositionX = coordX + 8 + (screenColumn * 3);
				int screenPositionY = coordY + 8 + (screenRow * 3);
				
				// Retrieves the data about this character from the screen buffer
				byte charAtLocation = tiledata.displayBuffer[screenRow][screenColumn];
				
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
				if(cursorMode != 0 && screenColumn == cursorX && screenRow == cursorY &&
						((time&4)!=0 || cursorMode == 1)) {
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
	}

	@Override
	protected void keyTyped(char charTyped, int lwjglCode) {
        // TODO don't send modifier keys to the server
		if(lwjglCode == 1) {mc.thePlayer.closeScreen();}
		
		DoesNotCompute.networkWrapper.sendToServer(new MessageKeyTyped(charTyped));
	}
	
}
