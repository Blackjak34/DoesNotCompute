package com.github.blackjak34.compute.sound;

import com.github.blackjak34.compute.entity.tile.client.TileEntityDiskDriveClient;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MovingSoundDiskDrive extends MovingSound {

    private final TileEntityDiskDriveClient diskDrive;

    public MovingSoundDiskDrive(TileEntityDiskDriveClient diskDrive) {
        super(new ResourceLocation("doesnotcompute:computer.diskspin"));

        this.diskDrive = diskDrive;
        BlockPos diskDrivePos = diskDrive.getPos();
        xPosF = diskDrivePos.getX();
        yPosF = diskDrivePos.getY();
        zPosF = diskDrivePos.getZ();

        repeat = true;
        repeatDelay = 0;
    }

    public void update() {
        if(diskDrive.isInvalid()) {
            donePlaying = true;
        } else {
            volume = diskDrive.isInProgress() ? 0.5F : 0.0F;
        }
    }

}
