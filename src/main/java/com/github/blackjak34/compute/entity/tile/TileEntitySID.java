package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class TileEntitySID extends TileEntity implements IRedbusCompatible {

    // 6 octave scale centered on A4, so 0 means A1 and 72 means A7
    // must be mapped to a float value from 0.5 to 2.0 to be usable with paulscode
    // since paulscode only covers 2 octaves through pitch manipulation three recordings exist for each waveform
    private int voice1Note = 35;
    private int voice2Note = 35;
    private int voice3Note = 35;

    private int voice1Waveform = 4;
    private int voice2Waveform = 4;
    private int voice3Waveform = 4;

    // ranges from 0 as silent to 255 as loudest
    // must be mapped to a float value from 0.0 to 1.0 to be usable with paulscode
    private int volume = 0;

    private int busAddress = 3;

    public TileEntitySID() {}

    public TileEntitySID(World worldIn) {}

    public boolean isDevice() {
        return true;
    }

    public int getBusAddress() {
        return busAddress;
    }

    public void setBusAddress(int newAddress) {
        busAddress = newAddress;
        worldObj.markBlockForUpdate(pos);
    }

    /*
     * 0x00 - voice 1 note
     * 0x01 - voice 2 note
     * 0x02 - voice 3 note
     * 0x03 - voice 1 waveform
     * 0x04 - voice 2 waveform
     * 0x05 - voice 3 waveform
     * 0x06 - volume
    */
    public void write(int index, int value) {
        switch(index) {
            case 0x00:
                voice1Note = Math.min(value, 72);
                break;
            case 0x01:
                voice2Note = Math.min(value, 72);
                break;
            case 0x02:
                voice3Note = Math.min(value, 72);
                break;
            case 0x03:
                voice1Waveform = Math.min(value, 4);
                break;
            case 0x04:
                voice2Waveform = Math.min(value, 4);
                break;
            case 0x05:
                voice3Waveform = Math.min(value, 4);
                break;
            case 0x06:
                volume = value;
                break;
        }
        worldObj.markBlockForUpdate(pos);
    }

    public int read(int index) {
        if(index == 0x00) {return voice1Note;}
        if(index == 0x01) {return voice2Note;}
        if(index == 0x02) {return voice3Note;}
        if(index == 0x03) {return voice1Waveform;}
        if(index == 0x04) {return voice2Waveform;}
        if(index == 0x05) {return voice3Waveform;}
        if(index == 0x06) {return volume;}
        return 0xFF;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound data = new NBTTagCompound();
        writeToNBT(data);

        return new S35PacketUpdateTileEntity(pos, 0, data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setInteger("voice1Note", voice1Note);
        data.setInteger("voice2Note", voice2Note);
        data.setInteger("voice3Note", voice3Note);
        data.setInteger("voice1Waveform", voice1Waveform);
        data.setInteger("voice2Waveform", voice2Waveform);
        data.setInteger("voice3Waveform", voice3Waveform);
        data.setInteger("volume", volume);
        data.setInteger("busAddress", busAddress);

        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        voice1Note = data.getInteger("voice1Note");
        voice2Note = data.getInteger("voice2Note");
        voice3Note = data.getInteger("voice3Note");
        voice1Waveform = data.getInteger("voice1Waveform");
        voice2Waveform = data.getInteger("voice2Waveform");
        voice3Waveform = data.getInteger("voice3Waveform");
        volume = data.getInteger("volume");
        busAddress = data.getInteger("busAddress");

        super.readFromNBT(data);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

}
