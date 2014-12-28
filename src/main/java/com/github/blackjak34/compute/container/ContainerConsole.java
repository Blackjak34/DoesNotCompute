package com.github.blackjak34.compute.container;

import com.github.blackjak34.compute.entity.tile.TileEntityConsole;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerConsole extends Container {

    private final TileEntityConsole console;

    public ContainerConsole(TileEntityConsole console) {
        this.console = console;
    }

    public TileEntityConsole getConsole() {
        return console;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(console.getPos()) < 64;
    }

}
