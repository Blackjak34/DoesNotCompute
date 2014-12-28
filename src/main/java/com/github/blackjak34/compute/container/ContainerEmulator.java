package com.github.blackjak34.compute.container;

import com.github.blackjak34.compute.entity.tile.TileEntityEmulator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerEmulator extends Container {

    private final TileEntityEmulator emulator;

    public ContainerEmulator(TileEntityEmulator emulator) {
        this.emulator = emulator;
    }

    public TileEntityEmulator getEmulator() {
        return emulator;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(emulator.getPos()) < 64;
    }

}
