package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.block.BlockCPU;
import com.github.blackjak34.compute.interfaces.IEmulatorIO;
import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import static com.github.blackjak34.compute.enums.GuiConstantCPU.*;

public class TileEntityCPU extends RedbusMaster implements IUpdatePlayerListBox, IRedbusCompatible, IEmulatorIO {

    private boolean running = false;

    private int busAddress = 0;

    private Emulator65EL02 emulator = new Emulator65EL02(this);

    public TileEntityCPU() {}

    public TileEntityCPU(World worldIn) {
        emulator.clearMemory();
        copyBootloader(worldIn);
    }

    public void onActionPerformed(int buttonID) {
        if(buttonID == BUTTON_STP.getValue()) {
            setRunning(false);
        } else if(buttonID == BUTTON_START.getValue()) {
            if(running) {
                emulator.setProgramCounter(emulator.getAddressPOR());
            } else {
                setRunning(true);
            }
        } else if(buttonID == BUTTON_RST.getValue()) {
            setRunning(false);
            emulator.setProgramCounter(0x0400);
            emulator.clearMemory();
            copyBootloader(worldObj);
        } else if(buttonID == BUTTON_DUMP.getValue()) {
            //DoesNotCompute.copyArrayIntoFile(worldObj, "memorydump", memory);
        } else {
            return;
        }

        worldObj.markBlockForUpdate(pos);
    }

    public void update() {
        if(!running) {return;}
        markDirty();

        emulator.resetCyclesElapsed();
        while(emulator.getCyclesElapsed() < 3500) {emulator.executeInstruction();}
    }

    public void halt() {
        setRunning(false);
    }

    public void lightOnFire() {
        BlockPos topSide = pos.offsetUp();
        if(worldObj.getBlockState(topSide).getBlock() == Blocks.air && Blocks.fire.canPlaceBlockAt(worldObj, topSide)) {
            worldObj.setBlockState(topSide, Blocks.fire.getDefaultState());
        }
    }

    public int getBusAddress() {
        return busAddress;
    }

    public void setBusAddress(int newAddress) {
        busAddress = newAddress;
        worldObj.markBlockForUpdate(pos);
    }

    public boolean isDevice() {
        return true;
    }

    public int read(int index) {
        return emulator.readWindow(index);
    }

    public void write(int index, int value) {
        markDirty();
        emulator.writeWindow(index, value);
    }

    public int readNetwork(int address, int index) {
        return readRedbus(address, index);
    }

    public void writeNetwork(int address, int index, int value) {
        writeRedbus(address, index, value);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("busAddress", busAddress);
        data.setBoolean("running", running);

        return new S35PacketUpdateTileEntity(pos, 0, data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setBoolean("running", running);
        data.setInteger("busAddress", busAddress);

        emulator.writeToNBT(data);
        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        running = data.getBoolean("running");
        busAddress = data.getInteger("busAddress");

        emulator.readFromNBT(data);
        super.readFromNBT(data);
    }

    private void setRunning(boolean value) {
        running = value;

        worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockCPU.RUNNING, value), 2);
    }

    private void copyBootloader(World worldIn) {
        byte[] bootloader = new byte[256];
        DoesNotCompute.copyFileIntoArray(worldIn, "bootloader", bootloader, 0, 256);
        emulator.copyArrayIntoMemory(bootloader, 0x0400, 256);
    }

}
