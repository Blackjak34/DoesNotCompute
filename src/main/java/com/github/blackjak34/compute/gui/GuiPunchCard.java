package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.render.RenderPunchCard;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

public class GuiPunchCard extends GuiScreen {

    public static final int GUIID = 123;

    private ItemStack punchCard;

    public GuiPunchCard(ItemStack punchCard) {
        this.punchCard = punchCard;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderPunchCard.INSTANCE.drawPunchCard(punchCard,
                (mc.displayWidth-1024)/2,
                (mc.displayHeight-450)/2,
                2.0);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
