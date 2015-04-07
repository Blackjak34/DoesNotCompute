package com.github.blackjak34.compute.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;

public class BlockBase extends Block implements ITileEntityProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    private final Class<? extends TileEntity> clientTE;
    private final Class<? extends TileEntity> serverTE;

    public BlockBase(Material material, Class<? extends TileEntity> clientTE,
                     Class<? extends TileEntity> serverTE, IProperty... addlProps) {
        super(material);
        setHarvestLevel("pickaxe", 1);
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);

        this.clientTE = clientTE;
        this.serverTE = serverTE;

        IBlockState defaultState = blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH);
        for(IProperty property : addlProps) {
            if(property instanceof PropertyBool) {
                defaultState = defaultState.withProperty(property, false);
            } else if(property instanceof PropertyDirection) {
                defaultState = defaultState.withProperty(property, EnumFacing.NORTH);
            } else if(property instanceof PropertyEnum) {
                System.out.println("Did not add property " + property.getName() +
                        " to default state; cannot dynamically add PropertyEnums.");
            } else if(property instanceof PropertyInteger) {
                defaultState = defaultState.withProperty(property, 0);
            }
        }
        setDefaultState(defaultState);
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING);
    }

    @Override
    public IBlockState onBlockPlaced(World worldInIn, BlockPos pos, EnumFacing facing,
                                     float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, EnumFacing.fromAngle(placer.getRotationYawHead()).getOpposite());
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        worldIn.removeTileEntity(pos);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        try {
            if (worldIn.isRemote) {
                return clientTE.newInstance();
            } else {
                return serverTE.getConstructor(World.class).newInstance(worldIn);
            }
        } catch(InstantiationException e) {
            System.err.println("Failed to initialize tile entity, " +
                    "make sure all tile entities have parameterless constructors.");
        } catch(IllegalAccessException e) {
            System.err.println("Could not access tile entity, " +
                    "make sure it is declared in a scope that allows it to be initialized.");
        } catch(NoSuchMethodException e) {
            System.err.println("The tile entity " + serverTE.getName() +
                    " does not provide a suitable constructor and failed to be initialized.");
        } catch(InvocationTargetException e) {
            System.err.println("An InvocationTargetException occurred while attempting to initialize tile entity " +
                    serverTE.getName() + ":");
            e.printStackTrace();
        }

        return null;
    }

}
