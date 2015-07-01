package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.block.BlockBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;

public class TileEntityCardPunch extends TileEntity implements ISidedInventory, IUpdatePlayerListBox {

    private boolean interpretMode;
    private boolean autoSkipDupMode;
    private boolean programSelect;
    private boolean autoFeedMode;
    private boolean printMode;
    private boolean leftZeroMode;
    private boolean multiPunchMode;
    private boolean numericMode;
    private boolean programEnabled;

    private int cardStackerNum;

    private ItemStack programCard;
    private ItemStack masterCard;
    private ItemStack inkRibbon;

    private ItemStack cardPreRegister;
    private ItemStack cardPunchStation;
    private ItemStack cardPreRead;
    private ItemStack cardReadStation;
    private ItemStack cardPostRegister;

    private ItemStack[] cardHopper = new ItemStack[8];
    private ItemStack[] cardStacker = new ItemStack[8];

    @Override
    public void update() {

    }

    public void onActionPerformed(int buttonid) {

    }

    public boolean isInInterpretMode() {
        return interpretMode;
    }

    public boolean isInAutoSkipDupMode() {
        return autoSkipDupMode;
    }

    public boolean isProgramOneSelected() {
        return programSelect;
    }

    public boolean isInAutoFeedMode() {
        return autoFeedMode;
    }

    public boolean isInPrintMode() {
        return printMode;
    }

    public boolean isInLeftZeroMode() {
        return leftZeroMode;
    }

    public boolean isProgramEnabled() {
        return programEnabled;
    }

    public int getNumCardsInStacker() {
        return cardStackerNum;
    }

    public ItemStack getProgramCard() {
        return programCard;
    }

    public ItemStack getPreRegisterPos() {
        return cardPreRegister;
    }

    public ItemStack getPunchStationPos() {
        return cardPunchStation;
    }

    public ItemStack getPreReadPos() {
        return cardPreRead;
    }

    public ItemStack getReadStationPos() {
        return cardReadStation;
    }

    public ItemStack getPostRegisterPos() {
        return cardPostRegister;
    }

    private static void addStackToNBT(ItemStack stack, NBTTagCompound tagCompound, String key) {
        if(stack == null) {return;}

        NBTTagCompound stackCompound = new NBTTagCompound();
        stack.writeToNBT(stackCompound);
        tagCompound.setTag(key, stackCompound);
    }

    private static ItemStack getStackFromNBT(NBTTagCompound tagCompound, String key) {
        NBTTagCompound itemCompound = tagCompound.getCompoundTag(key);
        return ItemStack.loadItemStackFromNBT(itemCompound);
    }

    private void writeSharedNBT(NBTTagCompound tagCompound) {
        tagCompound.setBoolean("interpretMode", interpretMode);
        tagCompound.setBoolean("autoSkipDupMode", autoSkipDupMode);
        tagCompound.setBoolean("programSelect", programSelect);
        tagCompound.setBoolean("autoFeedMode", autoFeedMode);
        tagCompound.setBoolean("printMode", printMode);
        tagCompound.setBoolean("leftZeroMode", leftZeroMode);
        tagCompound.setBoolean("programEnabled", programEnabled);

        addStackToNBT(programCard, tagCompound, "programCard");
        addStackToNBT(cardPreRegister, tagCompound, "cardPreRegister");
        addStackToNBT(cardPunchStation, tagCompound, "cardPunchStation");
        addStackToNBT(cardPreRead, tagCompound, "cardPreRead");
        addStackToNBT(cardReadStation, tagCompound, "cardReadStation");
        addStackToNBT(cardPostRegister, tagCompound, "cardPostRegister");
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tagCompound = new NBTTagCompound();

        writeSharedNBT(tagCompound);
        //TODO: write number of cards in stacker into description packet

        return new S35PacketUpdateTileEntity(pos, 0, tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);

        writeSharedNBT(tagCompound);

        tagCompound.setBoolean("multiPunchMode", multiPunchMode);
        tagCompound.setBoolean("numericMode", numericMode);

        addStackToNBT(masterCard, tagCompound, "masterCard");
        addStackToNBT(inkRibbon, tagCompound, "inkRibbon");

        for(int i=0;i<cardHopper.length;++i) {
            addStackToNBT(cardHopper[i], tagCompound, "cardHopper_" + i);
        }

        for(int i=0;i<cardStacker.length;++i) {
            addStackToNBT(cardStacker[i], tagCompound, "cardStacker_" + i);
        }
    }

    @Override
    public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
        NBTTagCompound tagCompound = packet.getNbtCompound();

        interpretMode = tagCompound.getBoolean("interpretMode");
        autoSkipDupMode = tagCompound.getBoolean("autoSkipDupMode");
        programSelect = tagCompound.getBoolean("programSelect");
        autoFeedMode = tagCompound.getBoolean("autoFeedMode");
        printMode = tagCompound.getBoolean("printMode");
        leftZeroMode = tagCompound.getBoolean("leftZeroMode");
        programEnabled = tagCompound.getBoolean("programEnabled");

        cardStackerNum = tagCompound.getInteger("cardStackerNum");

        programCard = getStackFromNBT(tagCompound, "programCard");

        cardPreRegister = getStackFromNBT(tagCompound, "cardPreRegister");
        cardPunchStation = getStackFromNBT(tagCompound, "cardPunchStation");
        cardPreRead = getStackFromNBT(tagCompound, "cardPreRead");
        cardReadStation = getStackFromNBT(tagCompound, "cardReadStation");
        cardPostRegister = getStackFromNBT(tagCompound, "cardPostRegister");
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        interpretMode = tagCompound.getBoolean("interpretMode");
        autoSkipDupMode = tagCompound.getBoolean("autoSkipDupMode");
        programSelect = tagCompound.getBoolean("programSelect");
        autoFeedMode = tagCompound.getBoolean("autoFeedMode");
        printMode = tagCompound.getBoolean("printMode");
        leftZeroMode = tagCompound.getBoolean("leftZeroMode");
        multiPunchMode = tagCompound.getBoolean("multiPunchMode");
        numericMode = tagCompound.getBoolean("numericMode");
        programEnabled = tagCompound.getBoolean("programEnabled");

        programCard = getStackFromNBT(tagCompound, "programCard");
        masterCard = getStackFromNBT(tagCompound, "masterCard");
        inkRibbon = getStackFromNBT(tagCompound, "inkRibbon");

        cardPreRegister = getStackFromNBT(tagCompound, "cardPreRegister");
        cardPunchStation = getStackFromNBT(tagCompound, "cardPunchStation");
        cardPreRead = getStackFromNBT(tagCompound, "cardPreRead");
        cardReadStation = getStackFromNBT(tagCompound, "cardReadStation");
        cardPostRegister = getStackFromNBT(tagCompound, "cardPostRegister");

        for(int i=0;i<cardHopper.length;++i) {
            cardHopper[i] = getStackFromNBT(tagCompound, "cardHopper_" + i);
        }

        for(int i=0;i<cardStacker.length;++i) {
            cardStacker[i] = getStackFromNBT(tagCompound, "cardStacker_" + i);
        }
    }

    @Override
    public int getSizeInventory() {
        return cardHopper.length + cardStacker.length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if(index < 0) {return null;}
        if(index < cardHopper.length) {return cardHopper[index];}
        if(index < getSizeInventory()) {return cardStacker[index-cardHopper.length];}

        return null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stackInSlot = getStackInSlot(index);
        if(stackInSlot == null) {return null;}

        if(stackInSlot.stackSize <= count) {
            setInventorySlotContents(index, null);
            return stackInSlot;
        }

        stackInSlot.stackSize -= count;
        return new ItemStack(stackInSlot.getItem(), count, stackInSlot.getItemDamage());
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        ItemStack stackInSlot = getStackInSlot(index);
        setInventorySlotContents(index, null);
        return stackInSlot;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if(index < 0 || !isItemValidForSlot(index, stack)) {return;}

        if(stack != null) {
            int stackLimit = getInventoryStackLimit();
            if(stack.stackSize > stackLimit) {stack.stackSize = stackLimit;}
        }

        if(index < cardHopper.length) {
            cardHopper[index] = stack;
        } else if(index < getSizeInventory()) {
            cardStacker[index-cardHopper.length] = stack;
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(pos.add(0.5, 0.5, 0.5)) <= 64.0;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index > 0 && index < getSizeInventory() &&
               (stack == null || stack.getItem() == DoesNotCompute.punchCardStack);
    }

    @Override
    public void clear() {
        for(int i=0;i<cardHopper.length;++i) {
            cardHopper[i] = null;
        }

        for(int i=0;i<cardStacker.length;++i) {
            cardStacker[i] = null;
        }
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        EnumFacing blockFacing = (EnumFacing) worldObj.getBlockState(pos).getValue(BlockBase.FACING);

        if(side == blockFacing.rotateYCCW()) {
            int[] slots = new int[cardHopper.length];
            for(int i=0;i<cardHopper.length;++i) {
                slots[i] = i;
            }
            return slots;
        }

        if(side == blockFacing.rotateY()) {
            int[] slots = new int[cardStacker.length];
            for(int i=0;i<cardStacker.length;++i) {
                slots[i] = i + cardHopper.length;
            }
            return slots;
        }

        return new int[0];
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        if(!isItemValidForSlot(index, itemStackIn) || getStackInSlot(index) != null) {return false;}

        EnumFacing blockFacing = (EnumFacing) worldObj.getBlockState(pos).getValue(BlockBase.FACING);
        return (index < cardHopper.length && direction == blockFacing.rotateYCCW()) ||
               (index < getSizeInventory() && direction == blockFacing.rotateY());
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        if(index < 0) {return false;}

        EnumFacing blockFacing = (EnumFacing) worldObj.getBlockState(pos).getValue(BlockBase.FACING);
        return (index < cardHopper.length && direction == blockFacing.rotateYCCW()) ||
               (index < getSizeInventory() && direction == blockFacing.rotateY());
    }

    @Override
    public void openInventory(EntityPlayer playerIn) {}

    public void closeInventory(EntityPlayer playerIn) {}

    @Override
    public String getName() {
        return "container.cardPunch";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public IChatComponent getDisplayName() {
        return new ChatComponentText("DNC 029 Card Punch");
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

}
