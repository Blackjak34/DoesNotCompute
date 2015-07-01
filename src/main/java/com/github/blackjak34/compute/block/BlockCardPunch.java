package com.github.blackjak34.compute.block;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.entity.tile.TileEntityCardPunch;
import com.github.blackjak34.compute.gui.GuiCardHopper;
import com.github.blackjak34.compute.gui.GuiCardPunch;
import com.github.blackjak34.compute.gui.GuiCardStacker;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockCardPunch extends Block implements ITileEntityProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockCardPunch() {
        super(Material.iron);

        setHarvestLevel("pickaxe", 1);
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setCreativeTab(DoesNotCompute.tabDoesNotCompute);
        setUnlocalizedName("blockCardPunch");

        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        EnumFacing blockFacing = (EnumFacing) state.getValue(FACING);

        if(side == blockFacing) {
            player.openGui(DoesNotCompute.instance, GuiCardPunch.GUIID,
                    worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }

        if(side == blockFacing.rotateY()) {
            player.openGui(DoesNotCompute.instance, GuiCardStacker.GUIID,
                    worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }

        if(side == blockFacing.rotateYCCW()) {
            player.openGui(DoesNotCompute.instance, GuiCardHopper.GUIID,
                    worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }

        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCardPunch();
    }

    @Override
    public IBlockState onBlockPlaced(World worldInIn, BlockPos pos, EnumFacing facing,
                                     float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, EnumFacing.fromAngle(placer.getRotationYawHead()).getOpposite());
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        //TODO drop items when broken
        worldIn.removeTileEntity(pos);
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING);
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

}
