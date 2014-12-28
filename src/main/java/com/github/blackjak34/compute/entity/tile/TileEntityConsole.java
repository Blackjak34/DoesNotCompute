package com.github.blackjak34.compute.entity.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;

public class TileEntityConsole extends TileEntity {

    private int cursorX = 0;
    private int cursorY = 0;

    private boolean running = false;
    private boolean floppyInDrive = false;

    public byte[][] displayBuffer = new byte[50][80];

    public TileEntityConsole() {
        for(byte[] row : displayBuffer) {
            Arrays.fill(row, (byte) 0xFF);
        }
    }

    public void onDisplayUpdate(int index, byte value) {
        if(index < 0 || index > 3999) {return;}

        displayBuffer[index/80][index%80] = value;
    }

    public void onCursorUpdate(int cursorX, int cursorY) {
        if(cursorX < 0 || cursorX > 79 || cursorY < 0 || cursorY > 49) {return;}

        this.cursorX = cursorX;
        this.cursorY = cursorY;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isFloppyInDrive() {
        return floppyInDrive;
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

    @Override
    public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
        NBTTagCompound data = packet.getNbtCompound();

        cursorX = data.getInteger("cursorX");
        cursorY = data.getInteger("cursorY");
        running = data.getBoolean("running");
        floppyInDrive = data.getBoolean("floppyInDrive");
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setInteger("cursorX", cursorX);
        data.setInteger("cursorY", cursorY);
        data.setBoolean("running", running);
        data.setBoolean("floppyInDrive", floppyInDrive);

        for(int i=0;i<displayBuffer.length;i++) {
            data.setByteArray("displayBuffer_row" + i, displayBuffer[i]);
        }

        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        cursorX = data.getInteger("cursorX");
        cursorY = data.getInteger("cursorY");
        running = data.getBoolean("running");
        floppyInDrive = data.getBoolean("floppyInDrive");

        for(int i=0;i<displayBuffer.length;i++) {
            displayBuffer[i] = data.getByteArray("displayBuffer_row" + i);
        }

        super.readFromNBT(data);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

}
