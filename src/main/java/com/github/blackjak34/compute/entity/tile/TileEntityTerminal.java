package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import com.github.blackjak34.compute.packet.MessageUpdateDisplay;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.world.World;

public class TileEntityTerminal extends RedbusCable implements IRedbusCompatible {

    public static final int BUS_ADDR = 1;

    private int accessRow = 0;
    private int cursorX = 0;
    private int cursorY = 0;
    private int cursorMode = 0;
    private int keyBufferStart = 0;
    private int keyBufferPos = 0;
    private int blitResult = 0;
    private int blitXStart = 0;
    private int blitYStart = 0;
    private int blitXOffset = 0;
    private int blitYOffset = 0;
    private int blitWidth = 0;
    private int blitHeight = 0;

    private byte[] keyBuffer = new byte[16];
    private byte[][] displayBuffer = new byte[50][80];

    public TileEntityTerminal() {}

    public TileEntityTerminal(World worldIn) {}

    public void onKeyTyped(char keyTyped) {
        markDirty();
        keyBuffer[keyBufferPos] = (byte) keyTyped;
        ++keyBufferPos;
        keyBufferPos &= 15;
    }

    public boolean isDevice() {
        return true;
    }

    public int getBusAddress() {
        return BUS_ADDR;
    }

    public void write(int index, int value) {
        markDirty();
        switch(index) {
            case 0:
                accessRow = Math.max(0, value % 50);
                break;
            case 1:
                cursorX = Math.max(0, value % 80);
                worldObj.markBlockForUpdate(pos);
                break;
            case 2:
                cursorY = Math.max(0, value % 50);
                worldObj.markBlockForUpdate(pos);
                break;
            case 3:
                cursorMode = Math.max(0, value % 3);
                worldObj.markBlockForUpdate(pos);
                break;
            case 4:
                keyBufferStart = value & 15;
                break;
            case 5:
                keyBufferPos = value & 15;
                break;
            case 6:
                keyBuffer[keyBufferStart] = (byte) value;
                break;
            case 7:
                runBlitter(value);
                break;
            case 8:
                // This is not limited because it is also used as the fill value
                blitXStart = value;
                break;
            case 9:
                blitYStart = Math.max(0, value % 50);
                break;
            case 10:
                blitXOffset = Math.max(0, value % 80);
                break;
            case 11:
                blitYOffset = Math.max(0, value % 50);
                break;
            case 12:
                blitWidth = Math.max(0, value % 81);
                break;
            case 13:
                blitHeight = Math.max(0, value % 51);
                break;
            default:
                if(index > 0x09 && index < 0x60) {
                    writeToDisplayBuffer(index-16, accessRow, value);
                }
        }
    }

    public int read(int index) {
        switch(index) {
            case 0:
                return accessRow;
            case 1:
                return cursorX;
            case 2:
                return cursorY;
            case 3:
                return cursorMode;
            case 4:
                return keyBufferStart;
            case 5:
                return keyBufferPos;
            case 6:
                return keyBuffer[keyBufferStart];
            case 7:
                return blitResult;
            case 8:
                return blitXStart;
            case 9:
                return blitYStart;
            case 10:
                return blitXOffset;
            case 11:
                return blitYOffset;
            case 12:
                return blitWidth;
            case 13:
                return blitHeight;
            default:
                if(index > 0x09 && index < 0x60) {
                    return displayBuffer[accessRow][index-10];
                }
                return 0xFF;
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound data = new NBTTagCompound();

        data.setInteger("cursorX", cursorX);
        data.setInteger("cursorY", cursorY);
        data.setInteger("cursorMode", cursorMode);
        super.writeToNBT(data);

        return new S35PacketUpdateTileEntity(pos, 0, data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setInteger("accessRow", accessRow);
        data.setInteger("cursorX", cursorX);
        data.setInteger("cursorY", cursorY);
        data.setInteger("cursorMode", cursorMode);
        data.setInteger("keyBufferStart", keyBufferStart);
        data.setInteger("keyBufferPos", keyBufferPos);
        data.setInteger("blitResult", blitResult);
        data.setInteger("blitXStart", blitXStart);
        data.setInteger("blitYStart", blitYStart);
        data.setInteger("blitXOffset", blitXOffset);
        data.setInteger("blitYOffset", blitYOffset);
        data.setInteger("blitWidth", blitWidth);
        data.setInteger("blitHeight", blitHeight);
        for(int i=0;i<displayBuffer.length;i++) {
            data.setByteArray("displayBuffer_row" + i, displayBuffer[i]);
        }

        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        accessRow = data.getInteger("accessRow");
        cursorX = data.getInteger("cursorX");
        cursorY = data.getInteger("cursorY");
        cursorMode = data.getInteger("cursorMode");
        keyBufferStart = data.getInteger("keyBufferStart");
        keyBufferPos = data.getInteger("keyBufferPos");
        blitResult = data.getInteger("blitResult");
        blitXStart = data.getInteger("blitXStart");
        blitYStart = data.getInteger("blitYStart");
        blitXOffset = data.getInteger("blitXOffset");
        blitYOffset = data.getInteger("blitYOffset");
        blitWidth = data.getInteger("blitWidth");
        blitHeight = data.getInteger("blitHeight");
        for(int i=0;i<displayBuffer.length;i++) {
            displayBuffer[i] = data.getByteArray("displayBuffer_row" + i);
        }

        super.readFromNBT(data);
    }


    // TODO: fix blitter; trying to fill only the bottom row seems to fail
    private void runBlitter(int operation) {
        int xStart, endX, endY;
        switch(operation) {
            case 1:
                endX = Math.min(79, blitXOffset+blitWidth);
                endY = Math.min(49, blitYOffset+blitHeight);
                for(int i=blitYOffset;i<endY;++i) {
                    for(int j=blitXOffset;j<endX;++j) {
                        writeToDisplayBuffer(j, i, blitXStart);
                    }
                }
                break;
            case 2:
                xStart = Math.min(79, blitXStart);
                endX = Math.min(79, xStart+blitWidth);
                endY = Math.min(49, blitYStart+blitHeight);
                for(int i=blitYStart;i<endY;++i) {
                    for(int j=xStart;j<endX;++j) {
                        writeToDisplayBuffer(j, i, (displayBuffer[i][j]&255)+128);
                    }
                }
                break;
            case 3:
                xStart = Math.min(79, blitXStart);
                nextLine: for(int i=0;i<blitHeight;++i) {
                    int sourceY = blitYStart + i;
                    int destY = blitYOffset + i;
                    if(destY > 49 || sourceY > 49) {break;}

                    for(int j=0;j<blitWidth;++j) {
                        int sourceX = xStart + j;
                        int destX = blitXOffset + j;
                        if(destX > 79 || sourceX > 79) {continue nextLine;}

                        writeToDisplayBuffer(destX, destY, displayBuffer[sourceY][sourceX]);
                    }
                }
                break;
            default:
                blitResult = 0xFF;
                return;
        }
        blitResult = 0;
    }

    private void writeToDisplayBuffer(int xPos, int yPos, int value) {
        displayBuffer[yPos][xPos] = (byte) value;
        DoesNotCompute.networkWrapper.sendToDimension(new MessageUpdateDisplay(xPos, yPos, (byte) value, pos),
                worldObj.provider.getDimensionId());
    }

}
