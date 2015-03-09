package com.github.blackjak34.compute.redbus;

import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class RedbusDataPacket {

    public final byte address;
    public final byte data;
    public final byte index;

    private final Set<BlockPos> visitedCables = new HashSet<BlockPos>();

    public RedbusDataPacket(int address, int data, int index) {
        this.address = (byte) address;
        this.data = (byte) data;
        this.index = (byte) index;
    }

    public boolean addCable(BlockPos pos) {
        return visitedCables.add(pos);
    }
    
    public static void sendPacket(World world, BlockPos pos, RedbusDataPacket dataPacket) {
        dataPacket.addCable(pos);

        BlockPos posNorth = pos.offsetNorth();
        BlockPos posEast = pos.offsetEast();
        BlockPos posSouth = pos.offsetSouth();
        BlockPos posWest = pos.offsetWest();

        TileEntity blockNorth = world.getTileEntity(posNorth);
        TileEntity blockEast = world.getTileEntity(posEast);
        TileEntity blockSouth = world.getTileEntity(posSouth);
        TileEntity blockWest = world.getTileEntity(posWest);

        if(blockNorth instanceof IRedbusCompatible && dataPacket.addCable(posNorth)) {
            ((IRedbusCompatible) blockNorth).onPacketReceived(dataPacket);
        }
        if(blockEast instanceof IRedbusCompatible && dataPacket.addCable(posEast)) {
            ((IRedbusCompatible) blockEast).onPacketReceived(dataPacket);
        }
        if(blockSouth instanceof IRedbusCompatible && dataPacket.addCable(posSouth)) {
            ((IRedbusCompatible) blockSouth).onPacketReceived(dataPacket);
        }
        if(blockWest instanceof IRedbusCompatible && dataPacket.addCable(posWest)) {
            ((IRedbusCompatible) blockWest).onPacketReceived(dataPacket);
        }
    }

}
