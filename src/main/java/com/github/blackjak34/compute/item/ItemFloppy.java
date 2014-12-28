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

	@Override
	public void onUpdate(ItemStack item, World world, Entity player, int par4, boolean par5) {
		setFloppyFilename(item, null);
	}

	public static ItemStack setFloppyFilename(ItemStack item, String filename) {
		NBTTagCompound itemData;
		if(!item.hasTagCompound()) {
			itemData = new NBTTagCompound();
		} else {
			itemData = item.getTagCompound();
			if(itemData.getString("filename") != null) {return item;}
		}

		if(filename == null) {filename = "disk_" + UUID.randomUUID().toString();}
		itemData.setString("filename", filename);
		item.setTagCompound(itemData);

		return item;
	}
	
}
