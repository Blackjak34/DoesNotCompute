package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.utils.TernaryTree;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

// TODO name to something more obvious (also RedbusDevice)
public class RedbusCable extends TileEntity {

    public RedbusCable() {}

    public static void updateSurroundingNetworks(World worldIn, BlockPos pos) {
        Set<TernaryTree> treesToUpdate = TernaryTree.getTreesContainingPos(worldIn, pos.offsetNorth());
        treesToUpdate.addAll(TernaryTree.getTreesContainingPos(worldIn, pos.offsetEast()));
        treesToUpdate.addAll(TernaryTree.getTreesContainingPos(worldIn, pos.offsetSouth()));
        treesToUpdate.addAll(TernaryTree.getTreesContainingPos(worldIn, pos.offsetWest()));

        for(TernaryTree tree : treesToUpdate) {tree.evaluateTree();}
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

}
