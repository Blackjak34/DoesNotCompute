package com.github.blackjak34.compute.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class ContainerCardStacker extends ContainerCardHopper {

    public ContainerCardStacker(EntityPlayer player, IInventory inventory) {
        super(player.inventory, inventory, false);
    }

}
