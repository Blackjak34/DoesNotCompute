package com.github.blackjak34.compute.block;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.entity.tile.RedbusCable;
import com.github.blackjak34.compute.entity.tile.TileEntityTerminal;
import com.github.blackjak34.compute.entity.tile.client.TileEntityTerminalClient;
import com.github.blackjak34.compute.gui.GuiTerminal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockTerminal extends BlockBase implements ITileEntityProvider {

	public BlockTerminal() {
		super(TileEntityTerminalClient.class, TileEntityTerminal.class);
		
		setCreativeTab(CreativeTabs.tabMisc);
		setUnlocalizedName("blockTerminal");
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

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumFacing side, float hitX, float hitY, float hitZ) {
		player.openGui(DoesNotCompute.instance, GuiTerminal.GUIID, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if(worldIn.isRemote) {return;}
        RedbusCable.updateSurroundingNetworks(worldIn, pos);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        worldIn.removeTileEntity(pos);
        if(!worldIn.isRemote) {RedbusCable.updateSurroundingNetworks(worldIn, pos);}
    }

}
