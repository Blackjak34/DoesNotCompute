package com.github.blackjak34.compute.entity.tile.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class TileEntityRedbus extends TileEntity {

    private int busAddress;

    public TileEntityRedbus() {}

    public int getBusAddress() {
        return busAddress;
    }

    @Override
    public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
        busAddress = packet.getNbtCompound().getInteger("busAddress");
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

}
