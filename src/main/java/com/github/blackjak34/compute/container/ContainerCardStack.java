package com.github.blackjak34.compute.container;

import com.github.blackjak34.compute.DoesNotCompute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ContainerCardStack extends Container {

    private final InventoryBasic inventory = new InventoryBasic("", false, 64);

    public ContainerCardStack(InventoryPlayer playerInventory, ItemStack punchCardStack) {
        NBTTagCompound tagCompound = punchCardStack.hasTagCompound()
                                     ? punchCardStack.getTagCompound()
                                     : new NBTTagCompound();
        for(int i=0;i<64;++i) {
            NBTTagCompound cardCompound = tagCompound.getCompoundTag("card_" + i);
            if(cardCompound == null) {continue;}

            inventory.setInventorySlotContents(i, ItemStack.loadItemStackFromNBT(cardCompound));
            addSlotToContainer(new Slot(inventory, i, 17+((i%8)*18), 17+((i/8)*18)) {

                                   public boolean isItemValid(ItemStack stack) {
                                       return stack.getItem() == DoesNotCompute.punchCard;
                                   }

                                   public int getSlotStackLimit() {
                                       return 1;
                                   }

                               });
        }

        for(int i=0;i<3;++i) {
            for(int j=0;j<9;++j) {
                addSlotToContainer(new Slot(playerInventory, j+i*9+9, 8+j*18, i*18+174));
            }
        }

        addSlotToContainer(new Slot(playerInventory, 0, 8, 232) {

                              @Override
                              public boolean canTakeStack(EntityPlayer player) {
                                  return false;
                              }

                           });
        for(int i=1;i<9;++i) {
            addSlotToContainer(new Slot(playerInventory, i, 8+i*18, 232));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);

        ItemStack punchCardStack = player.getCurrentEquippedItem();

        NBTTagCompound tagCompound;
        if(punchCardStack.hasTagCompound()) {
            tagCompound = punchCardStack.getTagCompound();
        } else {
            tagCompound = new NBTTagCompound();
            punchCardStack.setTagCompound(tagCompound);
        }
        for(Object object : inventorySlots) {
            Slot slot = (Slot) object;
            if(slot.inventory != inventory) {continue;}

            ItemStack card = slot.getStack();
            String key = "card_" + slot.getSlotIndex();
            if(card == null) {
                tagCompound.removeTag(key);
                continue;
            }

            NBTTagCompound cardCompound = new NBTTagCompound();
            card.writeToNBT(cardCompound);
            tagCompound.setTag(key, cardCompound);
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

    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean mergeSucceeded = false;

        int slotNum = reverseDirection ? endIndex-1 : startIndex;
        if(stack.isStackable()) {
            while(stack.stackSize > 0 && (!reverseDirection && slotNum < endIndex || reverseDirection && slotNum >= startIndex)) {
                Slot slot = (Slot) inventorySlots.get(slotNum);
                ItemStack stackInSlot = slot.getStack();

                if(stackInSlot != null && stackInSlot.getItem() == stack.getItem() &&
                   (!stack.getHasSubtypes() || stack.getMetadata() == stackInSlot.getMetadata()) &&
                   ItemStack.areItemStackTagsEqual(stack, stackInSlot)) {
                    int mergedSize = stackInSlot.stackSize + stack.stackSize;

                    if(mergedSize <= stack.getMaxStackSize()) {
                        stack.stackSize = 0;
                        stackInSlot.stackSize = mergedSize;
                        slot.onSlotChanged();
                        mergeSucceeded = true;
                    } else if(stackInSlot.stackSize < stack.getMaxStackSize()) {
                        stack.stackSize -= stack.getMaxStackSize() - stackInSlot.stackSize;
                        stackInSlot.stackSize = stack.getMaxStackSize();
                        slot.onSlotChanged();
                        mergeSucceeded = true;
                    }
                }

                if(reverseDirection) {
                    --slotNum;
                } else {
                    ++slotNum;
                }
            }
        }

        if(stack.stackSize > 0) {
            slotNum = reverseDirection ? endIndex-1 : startIndex;
            while(stack.stackSize > 0 && !reverseDirection && slotNum < endIndex || reverseDirection && slotNum >= startIndex) {
                Slot slot = (Slot) inventorySlots.get(slotNum);
                ItemStack stackInSlot = slot.getStack();
                if(stackInSlot == null && slot.isItemValid(stack)) {
                    slot.putStack(stack.copy());
                    if(stack.stackSize > slot.getSlotStackLimit()) {
                        slot.getStack().stackSize = slot.getSlotStackLimit();
                        slot.onSlotChanged();
                        stack.stackSize -= slot.getSlotStackLimit();
                        mergeSucceeded = true;
                    } else {
                        slot.onSlotChanged();
                        stack.stackSize = 0;
                        mergeSucceeded = true;
                        break;
                    }
                }

                if(reverseDirection) {
                    --slotNum;
                } else {
                    ++slotNum;
                }
            }
        }

        return mergeSucceeded;
    }

}
