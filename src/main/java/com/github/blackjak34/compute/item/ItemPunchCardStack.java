package com.github.blackjak34.compute.item;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.gui.GuiCardStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemPunchCardStack extends Item {

    public ItemPunchCardStack() {
        setCreativeTab(DoesNotCompute.tabDoesNotCompute);
        setUnlocalizedName("itemPunchCardStack");
        setMaxDamage(0);
        setNoRepair();
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
        BlockPos playerPos = playerIn.getPosition();
        playerIn.openGui(DoesNotCompute.instance, GuiCardStack.GUIID, worldIn,
                playerPos.getX(), playerPos.getY(), playerPos.getZ());
        return itemStackIn;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
        tooltip.add("Contains " + getNumCardsInStack(stack) + " punch cards");
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getNumCardsInStack(stack) > 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1 - (getNumCardsInStack(stack) / 64.0);
    }

    private int getNumCardsInStack(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if(tagCompound == null) {return 0;}

        int numCards = 0;
        for(int i=0;i<64;++i) {
            if(tagCompound.hasKey("card_" + i)) {++numCards;}
        }

        return numCards;
    }

}
