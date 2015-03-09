package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.redbus.RedbusDataPacket;
import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class TileEntityCableRibbon extends TileEntity implements IRedbusCompatible {

    public TileEntityCableRibbon() {}

    public TileEntityCableRibbon(World worldIn) {}

    public void onPacketReceived(RedbusDataPacket dataPacket) {
        RedbusDataPacket.sendPacket(worldObj, pos, dataPacket);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

}
