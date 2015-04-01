package com.github.blackjak34.compute.entity.tile.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class TileEntityTerminalClient extends TileEntity {

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

    @Override
    public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
        NBTTagCompound data = packet.getNbtCompound();

        cursorX = Math.min(data.getInteger("cursorX"), 79);
        cursorY = Math.min(data.getInteger("cursorY"), 49);
        cursorMode = Math.min(Math.abs(data.getInteger("cursorMode")), 3);

        super.readFromNBT(data);
        markDirty();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

}
