package com.github.blackjak34.compute.block;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.entity.tile.TileEntityDiskDrive;
import com.github.blackjak34.compute.entity.tile.client.TileEntityDiskDriveClient;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockDiskDrive extends BlockPeripheral implements ITileEntityProvider {

    public static final PropertyBool DISK = PropertyBool.create("disk");
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockDiskDrive() {
        super(Material.iron, TileEntityDiskDriveClient.class, TileEntityDiskDrive.class, DISK, ACTIVE);

        setCreativeTab(DoesNotCompute.tabDoesNotCompute);
        setUnlocalizedName("blockDiskDrive");
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING, DISK, ACTIVE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = (Boolean) state.getValue(DISK) ? 8 : 0;
        meta += (Boolean) state.getValue(ACTIVE) ? 4 : 0;

        switch((EnumFacing) state.getValue(FACING)) {
            case NORTH:default:
                return meta;
            case EAST:
                return meta + 1;
            case SOUTH:
                return meta + 2;
            case WEST:
                return meta + 3;
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
            case 3:
                return getDefaultState().withProperty(FACING, EnumFacing.WEST);
            case 4:
                return getDefaultState().withProperty(ACTIVE, true);
            case 5:
                return getDefaultState().withProperty(FACING, EnumFacing.EAST).withProperty(ACTIVE, true);
            case 6:
                return getDefaultState().withProperty(FACING, EnumFacing.SOUTH).withProperty(ACTIVE, true);
            case 7:
                return getDefaultState().withProperty(FACING, EnumFacing.WEST).withProperty(ACTIVE, true);
            case 8:
                return getDefaultState().withProperty(DISK, true);
            case 9:
                return getDefaultState().withProperty(FACING, EnumFacing.EAST).withProperty(DISK, true);
            case 10:
                return getDefaultState().withProperty(FACING, EnumFacing.SOUTH).withProperty(DISK, true);
            case 11:
                return getDefaultState().withProperty(FACING, EnumFacing.WEST).withProperty(DISK, true);
            case 12:
                return getDefaultState().withProperty(DISK, true).withProperty(ACTIVE, true);
            case 13:
                return getDefaultState().withProperty(FACING, EnumFacing.EAST).withProperty(DISK, true).withProperty(ACTIVE, true);
            case 14:
                return getDefaultState().withProperty(FACING, EnumFacing.SOUTH).withProperty(DISK, true).withProperty(ACTIVE, true);
            case 15:
                return getDefaultState().withProperty(FACING, EnumFacing.WEST).withProperty(DISK, true).withProperty(ACTIVE, true);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        if(super.onBlockActivated(worldIn, pos, state, player, side, hitX, hitY, hitZ)) {return true;}

        ItemStack itemInHand = player.getCurrentEquippedItem();
        if(!worldIn.isRemote && ((TileEntityDiskDrive) worldIn.getTileEntity(pos)).onDiskUsed(itemInHand)) {
            itemInHand.stackSize--;
        }

        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if(!worldIn.isRemote) {((TileEntityDiskDrive) worldIn.getTileEntity(pos)).ejectFloppyDisk();}
        super.breakBlock(worldIn, pos, state);
    }

}
