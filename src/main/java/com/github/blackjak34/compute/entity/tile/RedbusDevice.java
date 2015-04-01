package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.utils.TernaryTree;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class RedbusDevice extends TileEntity {

    private TernaryTree redbusNetwork;

    public RedbusDevice() {}

    public RedbusDevice(World worldIn) {}

    // TODO: get some better function names
    public int readRedbus(int address, int index) {
        validateTree();
        return redbusNetwork.redbusRead(address, index);
    }

    public void writeRedbus(int address, int index, int data) {
        validateTree();
        redbusNetwork.redbusWrite(address, index, data);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        validateTree();
        redbusNetwork.writeToNBT(data);
        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        redbusNetwork = new TernaryTree(data);
        super.readFromNBT(data);
    }

    @Override
    public void invalidate() {
        tileEntityInvalid = true;
        redbusNetwork = null;
    }

    @Override
    public void onChunkUnload() {
        redbusNetwork = null;
    }

    private void validateTree() {
        if(redbusNetwork == null) {
            redbusNetwork = new TernaryTree(worldObj, pos);
        }
    }

}
