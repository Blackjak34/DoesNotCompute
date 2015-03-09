package com.github.blackjak34.compute.block;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.entity.tile.TileEntityCPU;
import com.github.blackjak34.compute.entity.tile.client.TileEntityCPUClient;
import com.github.blackjak34.compute.gui.GuiCPU;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockCPU extends BlockBase implements ITileEntityProvider {

    public static final PropertyBool RUNNING = PropertyBool.create("running");

    public BlockCPU() {
        super(TileEntityCPUClient.class, TileEntityCPU.class, RUNNING);

        setCreativeTab(CreativeTabs.tabMisc);
        setUnlocalizedName("blockCPU");
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING, RUNNING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = (Boolean) state.getValue(RUNNING) ? 8 : 0;

        switch((EnumFacing) state.getValue(FACING)) {
            case NORTH:default:
                return meta;
            case EAST:
                return meta + 1;
            case SOUTH:
                return meta + 2;
            case WEST:
                return meta + 4;
        }
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        IBlockState state = getDefaultState();

        switch(metadata) {
            case 0:default:
                return state;
            case 1:
                return state.withProperty(FACING, EnumFacing.EAST);
            case 2:
                return state.withProperty(FACING, EnumFacing.SOUTH);
            case 4:
                return state.withProperty(FACING, EnumFacing.WEST);
            case 8:
                return state.withProperty(RUNNING, true);
            case 9:
                return state.withProperty(FACING, EnumFacing.EAST).withProperty(RUNNING, true);
            case 10:
                return state.withProperty(FACING, EnumFacing.SOUTH).withProperty(RUNNING, true);
            case 12:
                return state.withProperty(FACING, EnumFacing.WEST).withProperty(RUNNING, true);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumFacing side, float hitX, float hitY, float hitZ) {
        player.openGui(DoesNotCompute.instance, GuiCPU.GUIID, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

}
