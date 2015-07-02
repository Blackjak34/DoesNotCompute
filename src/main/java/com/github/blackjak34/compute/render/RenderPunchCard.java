package com.github.blackjak34.compute.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTPackedDepthStencil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.EXTFramebufferBlit.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

public class RenderPunchCard {

    public static final RenderPunchCard INSTANCE = new RenderPunchCard();

    private static final ResourceLocation punchCardTexture =
            new ResourceLocation("doesnotcompute:textures/gui/Fortran_Card.png");

    private final int framebuffer;

    private RenderPunchCard() {
        if(!OpenGlHelper.isFramebufferEnabled()) {
            framebuffer = 0;
            return;
        }

        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 512, 225, 0, GL_RGBA,
                GL_UNSIGNED_BYTE, (ByteBuffer) null);

        framebuffer = OpenGlHelper.func_153165_e(); // glGenFramebuffers
        OpenGlHelper.func_153171_g(GL_FRAMEBUFFER_EXT, framebuffer); // glBindFramebuffer

        OpenGlHelper.func_153188_a(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT,
                GL_TEXTURE_2D, texture, 0); // glFramebufferTexture2D

        int renderbuffer = OpenGlHelper.func_153185_f(); // glGenRenderbuffers
        OpenGlHelper.func_153176_h(GL_RENDERBUFFER_EXT, renderbuffer); // glBindRenderbuffer

        OpenGlHelper.func_153186_a(GL_RENDERBUFFER_EXT,
                EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT,
                512, 225); // glRenderbufferStorage

        OpenGlHelper.func_153190_b(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT,
                GL_RENDERBUFFER_EXT, renderbuffer); // glFramebufferRenderbuffer
        OpenGlHelper.func_153190_b(GL_FRAMEBUFFER_EXT, GL_STENCIL_ATTACHMENT_EXT,
                GL_RENDERBUFFER_EXT, renderbuffer); // glFramebufferRenderbuffer

        switch(OpenGlHelper.func_153167_i(GL_FRAMEBUFFER_EXT)) { // glCheckFramebufferStatus
            case GL_FRAMEBUFFER_COMPLETE_EXT:
                System.out.println("Framebuffers are working properly.");
                return;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
                System.err.println("Framebuffer error, incomplete attachment.");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
                System.err.println("Framebuffer error, incomplete/missing attachment.");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
                System.err.println("Framebuffer error, incomplete dimensions.");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
                System.err.println("Framebuffer error, incomplete formats.");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
                System.err.println("Framebuffer error, incomplete draw buffer.");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
                System.err.println("Framebuffer error, incomplete read buffer.");
                break;
            case GL_FRAMEBUFFER_UNSUPPORTED_EXT:
                System.err.println("Framebuffer error, framebuffer unsupported.");
                break;
        }

        //TODO: delete texture, framebuffer, and renderbuffer upon failure
    }

    public void drawPunchCard(ItemStack punchCard, int x, int y, double scale) {
        if(!OpenGlHelper.isFramebufferEnabled()) {return;}
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        int oldFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING_EXT);

        // buffer must have at least 16 elements, regardless of actual data size
        IntBuffer oldViewport = BufferUtils.createIntBuffer(16);
        glGetInteger(GL_VIEWPORT, oldViewport);

        // bind framebuffer
        OpenGlHelper.func_153171_g(GL_FRAMEBUFFER_EXT, framebuffer);

        glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        glClearStencil(0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT |  GL_STENCIL_BUFFER_BIT);

        glViewport(0, 0, 512, 225);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, 512.0, 225.0, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_STENCIL_TEST);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glColorMask(false, false, false, false);

        // begin drawing to the stencil buffer
        glStencilFunc(GL_ALWAYS, 1, 1);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);

        NBTTagCompound tagCompound = punchCard.hasTagCompound()
                                     ? punchCard.getTagCompound()
                                     : new NBTTagCompound();

        // draw punch card holes into stencil buffer
        glBindTexture(GL_TEXTURE_2D, 0);
        glBegin(GL_QUADS);
        for(int i=0;i<80;++i) {
            String key = "col" + i;
            if(!tagCompound.hasKey(key)) {continue;}
            int columnData = tagCompound.getInteger(key);

            int holeNearX = 16 + (i * 6);
            int holeFarX = holeNearX + 5;
            if((columnData & 1) != 0) {
                glVertex2i(holeNearX, 205);
                glVertex2i(holeNearX, 214);
                glVertex2i(holeFarX, 214);
                glVertex2i(holeFarX, 205);
            }
            if((columnData & 2) != 0) {
                glVertex2i(holeNearX, 188);
                glVertex2i(holeNearX, 197);
                glVertex2i(holeFarX, 197);
                glVertex2i(holeFarX, 188);
            }
            if((columnData & 4) != 0) {
                glVertex2i(holeNearX, 170);
                glVertex2i(holeNearX, 179);
                glVertex2i(holeFarX, 179);
                glVertex2i(holeFarX, 170);
            }
            if((columnData & 8) != 0) {
                glVertex2i(holeNearX, 152);
                glVertex2i(holeNearX, 161);
                glVertex2i(holeFarX, 161);
                glVertex2i(holeFarX, 152);
            }
            if((columnData & 16) != 0) {
                glVertex2i(holeNearX, 135);
                glVertex2i(holeNearX, 144);
                glVertex2i(holeFarX, 144);
                glVertex2i(holeFarX, 135);
            }
            if((columnData & 32) != 0) {
                glVertex2i(holeNearX, 117);
                glVertex2i(holeNearX, 126);
                glVertex2i(holeFarX, 126);
                glVertex2i(holeFarX, 117);
            }
            if((columnData & 64) != 0) {
                glVertex2i(holeNearX, 100);
                glVertex2i(holeNearX, 109);
                glVertex2i(holeFarX, 109);
                glVertex2i(holeFarX, 100);
            }
            if((columnData & 128) != 0) {
                glVertex2i(holeNearX, 82);
                glVertex2i(holeNearX, 91);
                glVertex2i(holeFarX, 91);
                glVertex2i(holeFarX, 82);
            }
            if((columnData & 256) != 0) {
                glVertex2i(holeNearX, 64);
                glVertex2i(holeNearX, 73);
                glVertex2i(holeFarX, 73);
                glVertex2i(holeFarX, 64);
            }
            if((columnData & 512) != 0) {
                glVertex2i(holeNearX, 47);
                glVertex2i(holeNearX, 56);
                glVertex2i(holeFarX, 56);
                glVertex2i(holeFarX, 47);
            }
            if((columnData & 1024) != 0) {
                glVertex2i(holeNearX, 29);
                glVertex2i(holeNearX, 38);
                glVertex2i(holeFarX, 38);
                glVertex2i(holeFarX, 29);
            }
            if((columnData & 2048) != 0) {
                glVertex2i(holeNearX, 12);
                glVertex2i(holeNearX, 21);
                glVertex2i(holeFarX, 21);
                glVertex2i(holeFarX, 12);
            }
        }
        glEnd();

        // stop drawing to the stencil buffer and set up stencil test
        glStencilFunc(GL_EQUAL, 0, 1);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

        glColorMask(true, true, true, true);

        // draw punch card texture using stencil test
        Minecraft.getMinecraft().renderEngine.bindTexture(punchCardTexture);
        glColor4d(1.0, 1.0, 1.0, 1.0);
        glBegin(GL_QUADS);
        glTexCoord2d(0.0, 0.0);
        glVertex2i(0, 0);
        glTexCoord2d(0.0, 0.439453125);
        glVertex2i(0, 225);
        glTexCoord2d(1.0, 0.439453125);
        glVertex2i(512, 225);
        glTexCoord2d(1.0, 0.0);
        glVertex2i(512, 0);
        glEnd();

        glDisable(GL_STENCIL_TEST);

        // draw printed letters
        glBegin(GL_QUADS);
        for(int i=0;i<80;++i) {
            byte[] printedChars = tagCompound.getByteArray("chr" + i);

            int charNearX = 16 + (i * 6);
            int charFarX = charNearX + 5;
            for(byte printedChar : printedChars) {
                double charNearS = (printedChar & 255) * 0.009765625;
                double charFarS = charNearS + 0.009765625;

                glTexCoord2d(charNearS, 0.439453125);
                glVertex2i(charNearX, 3);
                glTexCoord2d(charNearS, 0.453125);
                glVertex2i(charNearX, 10);
                glTexCoord2d(charFarS, 0.453125);
                glVertex2i(charFarX, 10);
                glTexCoord2d(charFarS, 0.439453125);
                glVertex2i(charFarX, 3);
            }
        }
        glEnd();

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();

        // restore viewport
        glViewport(oldViewport.get(0), oldViewport.get(1),
                oldViewport.get(2), oldViewport.get(3));

        // bind framebuffers and blit image
        OpenGlHelper.func_153171_g(GL_READ_FRAMEBUFFER_EXT, framebuffer);
        OpenGlHelper.func_153171_g(GL_DRAW_FRAMEBUFFER_EXT, oldFramebuffer);

        glBlitFramebufferEXT(0, 0, 512, 225, x, y,
                x + ((int) (512 * scale)),
                y + ((int) (225 * scale)),
                GL_COLOR_BUFFER_BIT, GL_NEAREST);
    }

}
