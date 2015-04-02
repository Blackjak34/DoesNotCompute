package com.github.blackjak34.compute.item;

import com.github.blackjak34.compute.DoesNotCompute;
import net.minecraft.item.Item;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class ItemScrewdriver extends Item {

    public ItemScrewdriver() {
        setCreativeTab(DoesNotCompute.tabDoesNotCompute);
        setUnlocalizedName("itemScrewdriver");
        setNoRepair();
        setMaxStackSize(1);
    }

}
