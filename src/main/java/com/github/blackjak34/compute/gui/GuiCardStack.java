package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.container.ContainerCardStack;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiCardStack extends GuiContainer {

    public static final int GUIID = 999;

    private static final ResourceLocation guiTextureLoc =
            new ResourceLocation("doesnotcompute:textures/gui/Gui_Card_Stack.png");

    public GuiCardStack(EntityPlayer player, ItemStack punchCardStack) {
        super(new ContainerCardStack(player.inventory, punchCardStack));

        xSize = 176;
        ySize = 256;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(guiTextureLoc);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
