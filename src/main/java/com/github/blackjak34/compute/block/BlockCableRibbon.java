package com.github.blackjak34.compute.block;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.entity.tile.TileEntityCableRibbon;
import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import com.github.blackjak34.compute.utils.TernaryTree;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockCableRibbon extends Block implements ITileEntityProvider {

    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");

    public BlockCableRibbon() {
        super(Material.circuits);
        setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 0.1f, 0.75f);

        setCreativeTab(DoesNotCompute.tabDoesNotCompute);
        setUnlocalizedName("blockCableRibbon");
        setDefaultState(blockState.getBaseState()
                        .withProperty(NORTH, false)
                        .withProperty(EAST, false)
                        .withProperty(SOUTH, false)
                        .withProperty(WEST, false)
        );
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        boolean north = (Boolean) state.getValue(NORTH);
        boolean east = (Boolean) state.getValue(EAST);
        boolean south = (Boolean) state.getValue(SOUTH);
        boolean west = (Boolean) state.getValue(WEST);

        return (north ? 1 : 0) + (east ? 2 : 0) + (south ? 4 : 0) + (west ? 8 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        IBlockState state = getDefaultState();

        switch(metadata) {
            case 1:
                return state.withProperty(NORTH, true);
            case 2:
                return state.withProperty(EAST, true);
            case 3:
                return state.withProperty(NORTH, true).withProperty(EAST, true);
            case 4:
                return state.withProperty(SOUTH, true);
            case 5:
                return state.withProperty(NORTH, true).withProperty(SOUTH, true);
            case 6:
                return state.withProperty(EAST, true).withProperty(SOUTH, true);
            case 7:
                return state.withProperty(NORTH, true).withProperty(EAST, true).withProperty(SOUTH, true);
            case 8:
                return state.withProperty(WEST, true);
            case 9:
                return state.withProperty(NORTH, true).withProperty(WEST, true);
            case 10:
                return state.withProperty(EAST, true).withProperty(WEST, true);
            case 11:
                return state.withProperty(NORTH, true).withProperty(EAST, true).withProperty(WEST, true);
            case 12:
                return state.withProperty(SOUTH, true).withProperty(WEST, true);
            case 13:
                return state.withProperty(NORTH, true).withProperty(SOUTH, true).withProperty(WEST, true);
            case 14:
                return state.withProperty(EAST, true).withProperty(SOUTH, true).withProperty(WEST, true);
            case 15:
                return state.withProperty(NORTH, true).withProperty(EAST, true).withProperty(SOUTH, true).withProperty(WEST, true);
            default:
                return state;
        }
    }

    private IBlockState updateState(World world, BlockPos pos) {
        BlockPos posNorth = pos.offsetNorth();
        BlockPos posEast = pos.offsetEast();
        BlockPos posSouth = pos.offsetSouth();
        BlockPos posWest = pos.offsetWest();

        TileEntity blockNorth = world.getTileEntity(posNorth);
        TileEntity blockEast = world.getTileEntity(posEast);
        TileEntity blockSouth = world.getTileEntity(posSouth);
        TileEntity blockWest = world.getTileEntity(posWest);

        return getDefaultState().withProperty(NORTH, blockNorth instanceof IRedbusCompatible)
                .withProperty(EAST, blockEast instanceof IRedbusCompatible)
                .withProperty(SOUTH, blockSouth instanceof IRedbusCompatible)
                .withProperty(WEST, blockWest instanceof IRedbusCompatible);
    }

    @Override
    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing,
                                     float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return updateState(world, pos);
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        if(!canPlaceBlockAt(world, pos)) {
            world.destroyBlock(pos, true);
        } else {
            world.setBlockState(pos, updateState(world, pos));
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        if(world.isRemote) {
            return null;
        } else {
            return new TileEntityCableRibbon();
        }
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return World.doesBlockHaveSolidTopSurface(worldIn, pos.offsetDown());
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
