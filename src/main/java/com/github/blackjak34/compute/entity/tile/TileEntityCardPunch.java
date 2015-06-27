package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.enums.CharacterComputer;
import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class TileEntityCardPunch extends TileEntity implements IRedbusCompatible, IUpdatePlayerListBox {

    private int busAddress = 4;
    private int punchPos = 0;
    private int opsRemaining = 2;

    private NBTTagCompound punchCardData;

    public TileEntityCardPunch() {}

    public TileEntityCardPunch(World worldIn) {}

    @Override
    public void update() {
        opsRemaining = 2;
    }

    @Override
    public boolean isDevice() {
        return true;
    }

    @Override
    public int getBusAddress() {
        return busAddress;
    }

    @Override
    public void setBusAddress(int newAddress) {
        busAddress = newAddress;
        worldObj.markBlockForUpdate(pos);
    }

    @Override
    public int read(int index) {
        if(index == 0) {return punchCardData != null ? 0 : 1;}
        if(index == 1) {return opsRemaining > 0 ? 0 : 1;}
        if(index == 2 && opsRemaining > 0 && punchCardData != null) {
            --opsRemaining;

            int columnVal;
            String key = "hole_" + punchPos;
            if(!punchCardData.hasKey(key)) {
                columnVal = CharacterComputer.SPACE.getCharCode();
            } else {
                columnVal = punchCardData.getInteger(key);
            }

            advancePunch();
            return columnVal;
        }
        return 0xFF;
    }

    @Override
    public void write(int index, int value) {
        if(index == 0 && value != 0) {ejectCard();}
        if(index == 2 && opsRemaining > 0 && punchCardData != null && !punchCardData.hasKey("punched")) {
            --opsRemaining;

            String key = "hole_" + punchPos;
            punchCardData.setInteger(key, value);

            advancePunch();
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("busAddress", busAddress);

        return new S35PacketUpdateTileEntity(pos, 0, data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setInteger("busAddress", busAddress);
        data.setInteger("punchPos", punchPos);
        data.setInteger("opsRemaining", opsRemaining);
        if(punchCardData != null) {data.setTag("punchCardData", punchCardData);}

        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        busAddress = data.getInteger("busAddress");
        punchPos = data.getInteger("punchPos");
        opsRemaining = data.getInteger("opsRemaining");
        punchCardData = (NBTTagCompound) data.getTag("punchCardData");

        super.readFromNBT(data);
    }

    public void ejectCard() {
        if(punchCardData == null) {return;}

        ItemStack ejectedStack = new ItemStack(DoesNotCompute.punchCard);
        punchCardData.setBoolean("punched", true);
        ejectedStack.setTagCompound(punchCardData);

        worldObj.spawnEntityInWorld(new EntityItem(worldObj,
                        pos.getX(), pos.getY(), pos.getZ(), ejectedStack));

        punchPos = 0;
        punchCardData = null;
    }

    public boolean onItemUsed(ItemStack itemUsed) {
        if(punchCardData != null || itemUsed.getItem() != DoesNotCompute.punchCard) {return false;}

        if(itemUsed.hasTagCompound()) {
            punchCardData = itemUsed.getTagCompound();
        } else {
            punchCardData = new NBTTagCompound();
        }
        return true;
    }

    private void advancePunch() {
        if(punchCardData == null) {return;}

        ++punchPos;
        if(punchPos == 80) {ejectCard();}
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

}
