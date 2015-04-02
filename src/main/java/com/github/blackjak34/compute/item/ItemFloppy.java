package com.github.blackjak34.compute.item;

import com.github.blackjak34.compute.DoesNotCompute;
import net.minecraft.item.Item;

public class ItemFloppy extends Item {

	public ItemFloppy() {
		setCreativeTab(DoesNotCompute.tabDoesNotCompute);
		setUnlocalizedName("itemFloppy");
		setNoRepair();
	}
	
}
