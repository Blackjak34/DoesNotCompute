package com.github.blackjak34.compute.block;

import com.github.blackjak34.compute.utils.TernaryTree;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class BlockPeripheral extends BlockBase {

    public BlockPeripheral(Material material, Class<? extends TileEntity> clientTE,
                           Class<? extends TileEntity> serverTE, IProperty... addlProps) {
        super(material, clientTE, serverTE, addlProps);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if(worldIn.isRemote) {return;}
        TernaryTree.updateSurroundingNetworks(worldIn, pos);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        worldIn.removeTileEntity(pos);

        if(worldIn.isRemote) {return;}
        TernaryTree.updateSurroundingNetworks(worldIn, pos);
    }

}
