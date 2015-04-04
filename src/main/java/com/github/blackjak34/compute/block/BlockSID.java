package com.github.blackjak34.compute.block;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.entity.tile.TileEntitySID;
import com.github.blackjak34.compute.entity.tile.client.TileEntitySIDClient;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class BlockSID extends BlockPeripheral implements ITileEntityProvider {

    public BlockSID() {
        super(Material.iron, TileEntitySIDClient.class, TileEntitySID.class);

        setCreativeTab(DoesNotCompute.tabDoesNotCompute);
        setUnlocalizedName("blockSID");
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        switch((EnumFacing) state.getValue(FACING)) {
            case NORTH:default:
                return 0;
            case EAST:
                return 1;
            case SOUTH:
                return 2;
            case WEST:
                return 4;
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        switch(meta) {
            case 0:default:
                return getDefaultState();
            case 1:
                return getDefaultState().withProperty(FACING, EnumFacing.EAST);
            case 2:
                return getDefaultState().withProperty(FACING, EnumFacing.SOUTH);
            case 4:
                return getDefaultState().withProperty(FACING, EnumFacing.WEST);
        }
    }

}
