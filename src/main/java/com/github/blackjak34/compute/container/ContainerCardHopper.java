package com.github.blackjak34.compute.container;

import com.github.blackjak34.compute.DoesNotCompute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCardHopper extends Container {

    private final IInventory inventory;

    public ContainerCardHopper(EntityPlayer player, IInventory inventory) {
        this(player.inventory, inventory, true);
    }

    protected ContainerCardHopper(InventoryPlayer playerInventory, IInventory inventory, boolean openCardHopper) {
        this.inventory = inventory;

        for(int i=0;i<8;++i) {
            addSlotToContainer(new Slot(inventory, i+(openCardHopper?0:8), 17+i*18, 20) {

                                  public boolean isItemValid(ItemStack stack) {
                                      return stack.getItem() == DoesNotCompute.punchCardStack;
                                  }

                                  public int getSlotStackLimit() {
                                      return 1;
                                  }

                               });
        }

        for(int i=0;i<3;++i) {
            for(int j=0;j<9;++j) {
                addSlotToContainer(new Slot(playerInventory, j+i*9+9, 8+j*18, i*18+51));
            }
        }

        for(int i=0;i<9;++i) {
            addSlotToContainer(new Slot(playerInventory, i, 8+i*18, 109));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return inventory.isUseableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot) inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if(index < inventory.getSizeInventory()) {
                if(!mergeItemStack(itemstack1, inventory.getSizeInventory(),
                        inventorySlots.size(), true)) {return null;}
            } else if(!mergeItemStack(itemstack1, 0, inventory.getSizeInventory(), false)) {
                return null;
            }

            if(itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

}
