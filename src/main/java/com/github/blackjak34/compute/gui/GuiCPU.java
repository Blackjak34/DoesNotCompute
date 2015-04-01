package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.container.ContainerBase;
import com.github.blackjak34.compute.entity.tile.client.TileEntityCPUClient;
import com.github.blackjak34.compute.packet.MessageActionPerformed;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static com.github.blackjak34.compute.enums.GuiConstantCPU.*;

public class GuiCPU extends GuiContainer {

    public static final int GUIID = 24;
    private static final double UV_SCALE = 0.00390625;

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

    private static final ResourceLocation guiTextureLoc = new ResourceLocation("doesnotcompute:textures/gui/Gui_Commodore9001.png");

    private TileEntityCPUClient tiledata;

    public GuiCPU(TileEntityCPUClient tiledata) {
        super(new ContainerBase(tiledata));

        this.tiledata = tiledata;
        xSize = 256;
        ySize = 128;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();

        GuiButton buttonStop = new GuiButton(BUTTON_STP.getValue(),
                guiLeft+BUTTON_STP_X.getValue(), guiTop+BUTTON_STP_Y.getValue(),
                BUTTON_WIDTH.getValue(), BUTTON_HEIGHT.getValue(), "");
        GuiButton buttonRun = new GuiButton(BUTTON_START.getValue(),
                guiLeft+BUTTON_START_X.getValue(), guiTop+BUTTON_START_Y.getValue(),
                BUTTON_WIDTH.getValue(), BUTTON_HEIGHT.getValue(), "");
        GuiButton buttonReset = new GuiButton(BUTTON_RST.getValue(),
                guiLeft+BUTTON_RST_X.getValue(), guiTop+BUTTON_RST_Y.getValue(),
                BUTTON_WIDTH.getValue(), BUTTON_HEIGHT.getValue(), "");
        GuiButton buttonDump = new GuiButton(BUTTON_DUMP.getValue(),
                guiLeft+BUTTON_DUMP_X.getValue(), guiTop+BUTTON_DUMP_Y.getValue(),
                BUTTON_WIDTH.getValue(), BUTTON_HEIGHT.getValue(), "");

        buttonList.add(buttonStop);
        buttonList.add(buttonRun);
        buttonList.add(buttonReset);
        buttonList.add(buttonDump);
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

        if(tiledata.isRunning()) {
            drawTexturedModalRect(coordX + LIGHT_RUN_X.getValue(), coordY + LIGHT_RUN_Y.getValue(),
                    0, ySize,
                    LIGHT_STATE_WIDTH.getValue(), LIGHT_STATE_HEIGHT.getValue());
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        DoesNotCompute.networkWrapper.sendToServer(new MessageActionPerformed(button.id));
    }

}
