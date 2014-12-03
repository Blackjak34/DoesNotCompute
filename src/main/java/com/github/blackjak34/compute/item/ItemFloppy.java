package com.github.blackjak34.compute.item;

import java.util.UUID;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * A floppy disk with a unique id attached to it that
 * corresponds to a file in the world directory. This
 * file contains the actual data stored on this disk.
 * 
 * @author Blackjak34
 * @since 1.1.0
 */
public class ItemFloppy extends Item {
	
	/**
	 * Initializes this item and sets some miscellaneous
	 * properties about it.
	 */
	public ItemFloppy() {
		setCreativeTab(CreativeTabs.tabMisc);
		setUnlocalizedName("itemFloppy");
		setTextureName("doesnotcompute:Floppy_Blue");
		setNoRepair();
	}
	
	/**
	 * Checks each tick that this item is in a player's
	 * inventory to see if a unique id has been set on
	 * this floppy's NBT data. If it hasn't, then it
	 * generates a new one and writes it to the NBT data.
	 */
	@Override
	public void onUpdate(ItemStack item, World world, Entity player, int par4, boolean par5) {
		NBTTagCompound itemData;
		if(!item.hasTagCompound()) {
			itemData = new NBTTagCompound();
		} else {
			itemData = item.getTagCompound();
			if(itemData.getString("filename") != null) {return;}
		}
		
		itemData.setString("filename", "disk_" + UUID.randomUUID().toString());
		item.setTagCompound(itemData);
	}
	
}
