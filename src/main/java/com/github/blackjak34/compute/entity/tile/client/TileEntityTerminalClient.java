package com.github.blackjak34.compute.entity.tile.client;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public class TileEntityTerminalClient extends TileEntityRedbus {

    private int cursorX = 0;
    private int cursorY = 0;
    private int cursorMode = 2;

    public byte[][] displayBuffer = new byte[50][80];

    public TileEntityTerminalClient() {}

    public void onDisplayUpdate(int coordX, int coordY, byte value) {
        if(coordX < 0 || coordY < 0 || coordX > 79 || coordY > 49) {return;}
        markDirty();

        displayBuffer[coordY][coordX] = value;
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

    public int getCursorMode() {
        return cursorMode;
    }

    // TODO: cursorX and cursorY can be set negative
    @Override
    public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
        NBTTagCompound data = packet.getNbtCompound();

        cursorX = Math.min(data.getInteger("cursorX"), 79);
        cursorY = Math.min(data.getInteger("cursorY"), 49);
        cursorMode = Math.min(Math.abs(data.getInteger("cursorMode")), 3);

        super.onDataPacket(networkManager, packet);
    }

}
