package com.github.blackjak34.compute.item;

import com.github.blackjak34.compute.DoesNotCompute;
import net.minecraft.item.Item;

public class ItemSystemFloppy extends Item {

    public ItemSystemFloppy() {
        setCreativeTab(DoesNotCompute.tabDoesNotCompute);
        setUnlocalizedName("itemSystemFloppy");
        setNoRepair();
        setMaxStackSize(1);
    }

}
