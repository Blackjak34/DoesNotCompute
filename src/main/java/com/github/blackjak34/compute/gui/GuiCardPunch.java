package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.container.ContainerBase;
import com.github.blackjak34.compute.entity.tile.TileEntityCardPunch;
import com.github.blackjak34.compute.enums.CharacterComputer;
import com.github.blackjak34.compute.proxy.client.KeyBindings;
import com.github.blackjak34.compute.render.RenderPunchCard;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

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
        RenderPunchCard.INSTANCE.drawPunchCard(tileEntity.cardPreRegister, mc.displayWidth - 544, 32, 1.0);
        RenderPunchCard.INSTANCE.drawPunchCard(tileEntity.cardPunchStation, mc.displayWidth - 576, 32, 1.0);
        RenderPunchCard.INSTANCE.drawPunchCard(tileEntity.cardPreRead, mc.displayWidth - 1024, 32, 1.0);
        RenderPunchCard.INSTANCE.drawPunchCard(tileEntity.cardReadStation, 32, 32, 1.0);
        RenderPunchCard.INSTANCE.drawPunchCard(tileEntity.cardPostRegister, 0, 32, 1.0);
    }

    @Override
    public void actionPerformed(GuiButton button) {

    }

    @Override
    protected void keyTyped(char charTyped, int lwjglCode) {
        if(lwjglCode == Keyboard.KEY_ESCAPE) {
            mc.thePlayer.closeScreen();
        } else if(lwjglCode == KeyBindings.punchFeed.getKeyCode()) {
            if(tileEntity.cardPreRegister == null) {
                tileEntity.cardPreRegister = new ItemStack(DoesNotCompute.punchCard);
            } else if(tileEntity.cardPunchStation == null) {
                tileEntity.cardPunchStation = tileEntity.cardPreRegister;
                tileEntity.cardPreRegister = new ItemStack(DoesNotCompute.punchCard);
                tileEntity.punchPos = 0;
            }
        } else if(lwjglCode == KeyBindings.punchRegister.getKeyCode()) {
            // TODO: if a card is already at one station but not the other, punch pos gets reset
            if(tileEntity.cardPunchStation == null) {
                tileEntity.cardPunchStation = tileEntity.cardPreRegister;
                tileEntity.cardPreRegister = null;
                tileEntity.punchPos = 0;
            }
            if(tileEntity.cardReadStation == null) {
                tileEntity.cardReadStation = tileEntity.cardPreRead;
                tileEntity.cardPreRead = null;
                tileEntity.punchPos = 0;
            }
            tileEntity.cardPostRegister = null;
        } else if(lwjglCode == KeyBindings.punchRelease.getKeyCode()) {
            if(tileEntity.cardPostRegister == null) {
                tileEntity.cardPostRegister = tileEntity.cardReadStation;
                tileEntity.cardReadStation = null;
            }
            if(tileEntity.cardPreRead == null) {
                tileEntity.cardPreRead = tileEntity.cardPunchStation;
                tileEntity.cardPunchStation = null;
            }
        } else if(lwjglCode == KeyBindings.punchLZero.getKeyCode()) {

        } else if(lwjglCode == KeyBindings.punchDup.getKeyCode()) {

        } else if(lwjglCode == KeyBindings.punchAuxDup.getKeyCode()) {

        } else if(lwjglCode == KeyBindings.punchSkip.getKeyCode()) {
            advancePunch();
        } else if(lwjglCode == KeyBindings.punchProgOne.getKeyCode()) {
            tileEntity.programSelect = false;
        } else if(lwjglCode == KeyBindings.punchProgTwo.getKeyCode()) {
            tileEntity.programSelect = true;
        } else if(lwjglCode == KeyBindings.punchMaster.getKeyCode()) {

        } else if(lwjglCode == KeyBindings.punchCent.getKeyCode()) {

        } else if(lwjglCode == KeyBindings.punchPrime.getKeyCode()) {

        } else {
            punchCharacter(charTyped);
        }
    }

    private void advancePunch() {
        if(tileEntity.cardPunchStation == null &&
           tileEntity.cardReadStation == null) {return;}

        ++tileEntity.punchPos;
        if(tileEntity.punchPos == 80) {
            // TODO: ignores possible card at pre-read position
            tileEntity.cardReadStation = tileEntity.cardPunchStation;
            tileEntity.cardPunchStation = tileEntity.cardPreRegister;
            tileEntity.cardPreRegister = null;
            tileEntity.punchPos = 0;
        }
    }

    private void punchCharacter(char character) {
        if(tileEntity.cardPunchStation == null) {return;}

        CharacterComputer data = CharacterComputer.getCharacter(character);
        NBTTagCompound tagCompound;
        if(tileEntity.cardPunchStation.hasTagCompound()) {
            tagCompound = tileEntity.cardPunchStation.getTagCompound();
        } else {
            tagCompound = new NBTTagCompound();
            tileEntity.cardPunchStation.setTagCompound(tagCompound);
        }

        byte[] printedChars = tagCompound.getByteArray("chr" + tileEntity.punchPos);
        byte[] newPrintedChars = new byte[printedChars.length+1];
        System.arraycopy(printedChars, 0, newPrintedChars, 0, printedChars.length);
        newPrintedChars[printedChars.length] = (byte) data.getPrintedSymbol();
        tagCompound.setByteArray("chr" + tileEntity.punchPos, newPrintedChars);

        int holePattern = tagCompound.getInteger("col" + tileEntity.punchPos);
        holePattern |= data.getHolePattern();
        tagCompound.setInteger("col" + tileEntity.punchPos, holePattern);

        advancePunch();
    }

}
