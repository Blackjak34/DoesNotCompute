package com.github.blackjak34.compute.block;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.entity.tile.TileEntityCardPunch;
import com.github.blackjak34.compute.entity.tile.client.TileEntityCardPunchClient;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockCardPunch extends BlockPeripheral {

    public BlockCardPunch() {
        super(Material.iron, TileEntityCardPunchClient.class, TileEntityCardPunch.class);

        setCreativeTab(DoesNotCompute.tabDoesNotCompute);
        setUnlocalizedName("blockCardPunch");
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
                return 3;
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
            case 3:
                return state.withProperty(FACING, EnumFacing.WEST);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        if(super.onBlockActivated(worldIn, pos, state, player, side, hitX, hitY, hitZ)) {return true;}

        if(!worldIn.isRemote) {
            ItemStack itemUsed = player.getHeldItem();
            TileEntityCardPunch tileEntity = (TileEntityCardPunch) worldIn.getTileEntity(pos);
            if(itemUsed == null) {
                tileEntity.ejectCard();
            } else if(tileEntity.onItemUsed(itemUsed)) {
                player.destroyCurrentEquippedItem();
            }
        }

        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if(!worldIn.isRemote) {((TileEntityCardPunch) worldIn.getTileEntity(pos)).ejectCard();}
        super.breakBlock(worldIn, pos, state);
    }

}
