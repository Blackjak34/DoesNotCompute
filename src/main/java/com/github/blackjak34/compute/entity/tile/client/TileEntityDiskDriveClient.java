package com.github.blackjak34.compute.entity.tile.client;

import com.github.blackjak34.compute.sound.MovingSoundDiskDrive;
import net.minecraft.client.Minecraft;

public class TileEntityDiskDriveClient extends TileEntityRedbus {

    public TileEntityDiskDriveClient() {}

    @Override
    public void validate() {
        Minecraft.getMinecraft().getSoundHandler().playSound(new MovingSoundDiskDrive(this));

        tileEntityInvalid = false;
    }

}
