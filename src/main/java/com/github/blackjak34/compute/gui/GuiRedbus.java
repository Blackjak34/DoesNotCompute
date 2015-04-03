package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.container.ContainerBase;
import com.github.blackjak34.compute.entity.tile.client.TileEntityRedbus;
import com.github.blackjak34.compute.packet.MessageChangeAddress;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class GuiRedbus extends GuiContainer {

    public static final int GUIID = 99;

    private static final ResourceLocation guiTextureLoc = new ResourceLocation("doesnotcompute:textures/gui/Gui_RedBus.png");

    private TileEntityRedbus tiledata;

    public GuiRedbus(TileEntityRedbus tiledata) {
        super(new ContainerBase(tiledata));

        this.tiledata = tiledata;
        xSize = 256;
        ySize = 75;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();

        GuiButton button128Up = new GuiButton(7, guiLeft+14, guiTop+4, 18, 33, "");
        GuiButton button128Down = new GuiButton(15, guiLeft+14, guiTop+38, 18, 33, "");
        GuiButton button64Up = new GuiButton(6, guiLeft+44, guiTop+4, 18, 33, "");
        GuiButton button64Down = new GuiButton(14, guiLeft+44, guiTop+38, 18, 33, "");
        GuiButton button32Up = new GuiButton(5, guiLeft+74, guiTop+4, 18, 33, "");
        GuiButton button32Down = new GuiButton(13, guiLeft+74, guiTop+38, 18, 33, "");
        GuiButton button16Up = new GuiButton(4, guiLeft+104, guiTop+4, 18, 33, "");
        GuiButton button16Down = new GuiButton(12, guiLeft+104, guiTop+38, 18, 33, "");
        GuiButton button8Up = new GuiButton(3, guiLeft+134, guiTop+4, 18, 33, "");
        GuiButton button8Down = new GuiButton(11, guiLeft+134, guiTop+38, 18, 33, "");
        GuiButton button4Up = new GuiButton(2, guiLeft+164, guiTop+4, 18, 33, "");
        GuiButton button4Down = new GuiButton(10, guiLeft+164, guiTop+38, 18, 33, "");
        GuiButton button2Up = new GuiButton(1, guiLeft+194, guiTop+4, 18, 33, "");
        GuiButton button2Down = new GuiButton(9, guiLeft+194, guiTop+38, 18, 33, "");
        GuiButton button1Up = new GuiButton(0, guiLeft+224, guiTop+4, 18, 33, "");
        GuiButton button1Down = new GuiButton(8, guiLeft+224, guiTop+38, 18, 33, "");

        int busAddress = tiledata.getBusAddress();
        if((busAddress & 128) != 0) {button128Up.enabled = false;} else {button128Down.enabled = false;}
        if((busAddress & 64) != 0) {button64Up.enabled = false;} else {button64Down.enabled = false;}
        if((busAddress & 32) != 0) {button32Up.enabled = false;} else {button32Down.enabled = false;}
        if((busAddress & 16) != 0) {button16Up.enabled = false;} else {button16Down.enabled = false;}
        if((busAddress & 8) != 0) {button8Up.enabled = false;} else {button8Down.enabled = false;}
        if((busAddress & 4) != 0) {button4Up.enabled = false;} else {button4Down.enabled = false;}
        if((busAddress & 2) != 0) {button2Up.enabled = false;} else {button2Down.enabled = false;}
        if((busAddress & 1) != 0) {button1Up.enabled = false;} else {button1Down.enabled = false;}

        // Registered in order of ascending ID to allow for easy finding of buttons
        buttonList.add(button1Up);
        buttonList.add(button2Up);
        buttonList.add(button4Up);
        buttonList.add(button8Up);
        buttonList.add(button16Up);
        buttonList.add(button32Up);
        buttonList.add(button64Up);
        buttonList.add(button128Up);
        buttonList.add(button1Down);
        buttonList.add(button2Down);
        buttonList.add(button4Down);
        buttonList.add(button8Down);
        buttonList.add(button16Down);
        buttonList.add(button32Down);
        buttonList.add(button64Down);
        buttonList.add(button128Down);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

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

        int busAddress = tiledata.getBusAddress();

        if((busAddress & 128) != 0) {drawTexturedModalRect(guiLeft+14, guiTop+4, 0, ySize, 18, 68);}
        if((busAddress & 64) != 0) {drawTexturedModalRect(guiLeft+44, guiTop+4, 0, ySize, 18, 68);}
        if((busAddress & 32) != 0) {drawTexturedModalRect(guiLeft+74, guiTop+4, 0, ySize, 18, 68);}
        if((busAddress & 16) != 0) {drawTexturedModalRect(guiLeft+104, guiTop+4, 0, ySize, 18, 68);}
        if((busAddress & 8) != 0) {drawTexturedModalRect(guiLeft+134, guiTop+4, 0, ySize, 18, 68);}
        if((busAddress & 4) != 0) {drawTexturedModalRect(guiLeft+164, guiTop+4, 0, ySize, 18, 68);}
        if((busAddress & 2) != 0) {drawTexturedModalRect(guiLeft+194, guiTop+4, 0, ySize, 18, 68);}
        if((busAddress & 1) != 0) {drawTexturedModalRect(guiLeft+224, guiTop+4, 0, ySize, 18, 68);}
    }

    @Override
    public void actionPerformed(GuiButton button) {
        button.enabled = false;
        boolean wasFlippedOn = button.id < 8;
        int otherButtonID = button.id + (wasFlippedOn ? 8 : -8);
        ((GuiButton) buttonList.get(otherButtonID)).enabled = true;

        int newBusAddress = tiledata.getBusAddress();
        if(wasFlippedOn) {
            newBusAddress += Math.pow(2, button.id);
        } else {
            newBusAddress -= Math.pow(2, button.id-8);
        }

        DoesNotCompute.networkWrapper.sendToServer(new MessageChangeAddress(newBusAddress));
    }

}
