package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.enums.CharacterComputer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiPunchCard extends GuiScreen {

    public static final int GUIID = 123;
    private static final double UV_SCALE = 0.001953125;

    private static final int X_SIZE = 512;
    private static final int Y_SIZE = 227;

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

    private static final ResourceLocation guiTextureLoc = new
            ResourceLocation("doesnotcompute:textures/gui/Gui_Punch_Card.png");

    private ItemStack punchCard;

    public GuiPunchCard(ItemStack punchCard) {
        this.punchCard = punchCard;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
        mc.renderEngine.bindTexture(guiTextureLoc);

        int guiLeft = (width - (X_SIZE/2)) / 2;
        int guiTop = (height - (Y_SIZE/2)) / 2;

        // draw left margin of punch card
        worldRenderer.startDrawingQuads();
        worldRenderer.addVertexWithUV(guiLeft, guiTop, zLevel, 0.0, 0.0);
        worldRenderer.addVertexWithUV(guiLeft, guiTop+(Y_SIZE/2), zLevel,
                0.0, Y_SIZE*UV_SCALE);
        worldRenderer.addVertexWithUV(guiLeft+8.5, guiTop+(Y_SIZE/2),
                zLevel, 17*UV_SCALE, Y_SIZE*UV_SCALE);
        worldRenderer.addVertexWithUV(guiLeft+8.5, guiTop, zLevel,
                17*UV_SCALE, 0.0);
        tessellator.draw();

        NBTTagCompound tagCompound = punchCard.hasTagCompound()
                                     ? punchCard.getTagCompound()
                                     : new NBTTagCompound();

        for(int i=0;i<80;++i) {
            String key = "hole_" + i;
            CharacterComputer character;
            if(!tagCompound.hasKey(key)) {
                character = CharacterComputer.SPACE;
            } else {
                character = CharacterComputer.getCharacter(
                        tagCompound.getInteger(key));
            }

            int symbolIndex = character.getPrintedSymbol();
            worldRenderer.startDrawingQuads();
            worldRenderer.addVertexWithUV(guiLeft+8.5+(i*3), guiTop,
                    zLevel, ((symbolIndex*6)+17)*UV_SCALE, 0.0);
            worldRenderer.addVertexWithUV(guiLeft+8.5+(i*3), guiTop+(Y_SIZE/2),
                    zLevel, ((symbolIndex*6)+17)*UV_SCALE, Y_SIZE*UV_SCALE);
            worldRenderer.addVertexWithUV(guiLeft+11.5+(i*3), guiTop+(Y_SIZE/2),
                    zLevel, ((symbolIndex*6)+23)*UV_SCALE, Y_SIZE*UV_SCALE);
            worldRenderer.addVertexWithUV(guiLeft+11.5+(i*3), guiTop,
                    zLevel, ((symbolIndex*6)+23)*UV_SCALE, 0.0);
            tessellator.draw();
        }

        // draw right margin of punch card
        worldRenderer.startDrawingQuads();
        worldRenderer.addVertexWithUV(width-15-guiLeft, guiTop, zLevel,
                359*UV_SCALE, 0.0);
        worldRenderer.addVertexWithUV(width-15-guiLeft, guiTop+(Y_SIZE/2),
                zLevel, 359*UV_SCALE, Y_SIZE*UV_SCALE);
        worldRenderer.addVertexWithUV(width-guiLeft, guiTop+(Y_SIZE/2),
                zLevel, 365*UV_SCALE, Y_SIZE*UV_SCALE);
        worldRenderer.addVertexWithUV(width-guiLeft, guiTop, zLevel,
                365*UV_SCALE, 0.0);
        tessellator.draw();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
