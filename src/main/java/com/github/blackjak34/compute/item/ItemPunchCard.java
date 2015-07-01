package com.github.blackjak34.compute.item;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.gui.GuiPunchCard;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class ItemPunchCard extends Item {

    public ItemPunchCard() {
        setCreativeTab(DoesNotCompute.tabDoesNotCompute);
        setUnlocalizedName("itemPunchCard");
        setNoRepair();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        BlockPos playerPos = player.getPosition();
        player.openGui(DoesNotCompute.instance, GuiPunchCard.GUIID, world,
                playerPos.getX(), playerPos.getY(), playerPos.getZ());
        return itemStack;
    }

}
