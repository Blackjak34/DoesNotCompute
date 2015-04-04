package com.github.blackjak34.compute.entity.tile.client;

import com.github.blackjak34.compute.sound.MovingSoundDiskDrive;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public class TileEntityDiskDriveClient extends TileEntityRedbus {

    private boolean inProgress = false;

    public TileEntityDiskDriveClient() {}

    public boolean isInProgress() {
        return inProgress;
    }

    @Override
    public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
        inProgress = packet.getNbtCompound().getBoolean("inProgress");

        super.onDataPacket(networkManager, packet);
    }

    @Override
    public void validate() {
        Minecraft.getMinecraft().getSoundHandler().playSound(new MovingSoundDiskDrive(this));

        tileEntityInvalid = false;
    }

}
