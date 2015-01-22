package com.github.blackjak34.compute.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemFloppy extends Item {

	public ItemFloppy() {
		setCreativeTab(CreativeTabs.tabMisc);
		setUnlocalizedName("itemFloppy");
		setNoRepair();
	}
	
}
