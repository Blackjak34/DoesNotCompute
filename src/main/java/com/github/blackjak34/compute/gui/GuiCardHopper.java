package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.container.ContainerCardHopper;
import com.github.blackjak34.compute.container.ContainerCardStacker;
import com.github.blackjak34.compute.entity.tile.TileEntityCardPunch;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiCardHopper extends GuiContainer {

    public static final int GUIID = 29;

    private static final ResourceLocation guiTextureLoc = new
            ResourceLocation("doesnotcompute:textures/gui/Gui_Card_Hopper.png");

    public GuiCardHopper(TileEntityCardPunch tileEntity, EntityPlayer player) {
        this(tileEntity, player, true);
    }

    protected GuiCardHopper(TileEntityCardPunch tileEntity, EntityPlayer player, boolean openCardHopper) {
        super(openCardHopper ? new ContainerCardHopper(player, tileEntity) : new ContainerCardStacker(player, tileEntity));

        xSize = 176;
        ySize = 133;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(guiTextureLoc);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

}
