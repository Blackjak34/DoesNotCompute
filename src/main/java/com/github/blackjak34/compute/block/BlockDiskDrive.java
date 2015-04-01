package com.github.blackjak34.compute.block;

import com.github.blackjak34.compute.entity.tile.TileEntityDiskDrive;
import com.github.blackjak34.compute.entity.tile.client.TileEntityDiskDriveClient;
import com.github.blackjak34.compute.utils.TernaryTree;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockDiskDrive extends BlockPeripheral implements ITileEntityProvider {

    public static final PropertyBool DISK = PropertyBool.create("disk");

    public BlockDiskDrive() {
        super(Material.iron, TileEntityDiskDriveClient.class, TileEntityDiskDrive.class, DISK);

        setCreativeTab(CreativeTabs.tabMisc);
        setUnlocalizedName("blockDiskDrive");
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING, DISK);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = (Boolean) state.getValue(DISK) ? 8 : 0;

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
            case 8:
                return getDefaultState().withProperty(DISK, true);
            case 9:
                return getDefaultState().withProperty(FACING, EnumFacing.EAST).withProperty(DISK, true);
            case 10:
                return getDefaultState().withProperty(FACING, EnumFacing.SOUTH).withProperty(DISK, true);
            case 12:
                return getDefaultState().withProperty(FACING, EnumFacing.WEST).withProperty(DISK, true);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack itemInHand = player.getCurrentEquippedItem();
        if(!worldIn.isRemote && ((TileEntityDiskDrive) worldIn.getTileEntity(pos)).onDiskUsed(itemInHand)) {
            itemInHand.stackSize--;
        }

        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if(!worldIn.isRemote) {((TileEntityDiskDrive) worldIn.getTileEntity(pos)).ejectFloppyDisk();}
        worldIn.removeTileEntity(pos);
        if(!worldIn.isRemote) {TernaryTree.updateSurroundingNetworks(worldIn, pos);}
    }

}
