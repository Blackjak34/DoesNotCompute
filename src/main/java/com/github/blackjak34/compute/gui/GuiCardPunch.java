package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.container.ContainerBase;
import com.github.blackjak34.compute.entity.tile.TileEntityCardPunch;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

public class GuiCardPunch extends GuiContainer {

    public static final int GUIID = 321;

    private static final ResourceLocation guiTextureLoc =
            new ResourceLocation("doesnotcompute:textures/gui/Gui_Card_Punch.png");

    private final TileEntityCardPunch tileEntity;

    public GuiCardPunch(TileEntityCardPunch tileEntity) {
        super(new ContainerBase(tileEntity));

        this.tileEntity = tileEntity;
        xSize = 512;
        ySize = 183;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {

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
    protected void drawGuiContainerBackgroundLayer(
            float partialTicks, int mouseX, int mouseY) {

    }

    @Override
    public void actionPerformed(GuiButton button) {

    }

}
