package com.github.blackjak34.compute.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class ContainerBase extends Container {

    private final TileEntity tileEntity;

    public ContainerBase(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(tileEntity.getPos()) < 64;
    }

}
