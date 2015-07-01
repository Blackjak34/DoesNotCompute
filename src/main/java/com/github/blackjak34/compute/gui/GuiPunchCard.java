package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.DoesNotCompute;
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
        double guiLeft = (width - 256) / 2;
        double guiTop = (height - 112.5) / 2;

        DoesNotCompute.punchCard.drawPunchCard(punchCard, guiLeft, guiTop, zLevel, 0.5);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
