package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.block.BlockDiskDrive;
import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import com.github.blackjak34.compute.item.ItemFloppy;
import com.github.blackjak34.compute.redbus.RedbusDataPacket;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

public class TileEntityDiskDrive extends TileEntity implements IUpdatePlayerListBox, IRedbusCompatible {

    public static final int BUS_ADDR = 2;

    private static final char[] DEFAULT_NAME = "Floppy Disk".toCharArray();

    private static final Charset CHARSET = Charset.forName("US-ASCII");

    private int currentOperation = 0;

    private boolean diskInDrive = false;
    private boolean inProgress = false;

    private byte[] redbusWindow = new byte[256];
    private byte[] diskData;

    private String diskFileName;
    private String diskItemName;

    public TileEntityDiskDrive() {}

    public TileEntityDiskDrive(World worldIn) {}

    private void setDiskInDrive(boolean value) {
        diskInDrive = value;

        worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockDiskDrive.DISK, value));
    }

    public void update() {
        if(!inProgress) {return;}
        //System.out.println("Performing operation " + currentOperation);

        boolean commandSuccessful = true;
        if(!diskInDrive) {currentOperation = 6;}
        switch(currentOperation) {
            case 1:
                char[] diskNameChars;
                if(diskItemName != null) {
                    diskNameChars = diskItemName.toCharArray();
                } else {
                    diskNameChars = DEFAULT_NAME;
                }
                for(int i=0;i<128;i++) {
                    if(i < diskNameChars.length) {
                        redbusWindow[i] = (byte) diskNameChars[i];
                    } else {
                        redbusWindow[i] = 0;
                    }
                }
                break;
            case 2:
                byte[] diskNameBytes = new byte[128];
                System.arraycopy(redbusWindow, 0, diskNameBytes, 0, 128);
                diskItemName = new String(diskNameBytes, CHARSET);

                int nullIndex = diskItemName.indexOf('\0');
                if(nullIndex >= 0) {diskItemName = diskItemName.substring(0, nullIndex);}
                break;
            case 3:
                char[] fileNameChars = diskFileName.substring(5).toCharArray();
                for(int i=0;i<128;i++) {
                    if(i < fileNameChars.length) {
                        redbusWindow[i] = (byte) fileNameChars[i];
                    } else {
                        redbusWindow[i] = 0;
                    }
                }
                break;
            case 4:
                int sectorToLoad = ((redbusWindow[129]&255) << 8) | (redbusWindow[128]&255);
                //System.out.printf("Sector to load: %4X\n", sectorToLoad);
                if(sectorToLoad < diskData.length / 128) {
                    System.arraycopy(diskData, sectorToLoad * 128, redbusWindow, 0, 128);
                } else {
                    commandSuccessful = false;
                }
                break;
            case 5:
                int sectorToWrite = ((redbusWindow[129]&255) << 8) | (redbusWindow[128]&255);
                //System.out.printf("Sector to write: %4X\n", sectorToWrite);
                if(sectorToWrite < 0x800) {
                    if(sectorToWrite >= diskData.length / 128) {
                        diskData = Arrays.copyOf(diskData, (sectorToWrite+1)*128);
                    }
                    System.arraycopy(redbusWindow, 0, diskData, sectorToWrite * 128, 128);
                } else {
                    commandSuccessful = false;
                }
                break;
            case 6:default:
                commandSuccessful = false;
                break;
        }

        if(commandSuccessful) {
            //System.out.println("Operation successful.");
            redbusWindow[0x82] = 0x00;
            RedbusDataPacket.sendPacket(worldObj, pos, new RedbusDataPacket(TileEntityCPU.BUS_ADDR, 0x00, 0x82));

            for(int i=0;i<128;i++) {
                RedbusDataPacket.sendPacket(worldObj, pos,
                        new RedbusDataPacket(TileEntityCPU.BUS_ADDR, redbusWindow[i], i));
            }
        } else {
            //System.out.println("Operation failed.");
            redbusWindow[0x82] = (byte) 0xFF;
            RedbusDataPacket.sendPacket(worldObj, pos, new RedbusDataPacket(TileEntityCPU.BUS_ADDR, 0xFF, 0x82));
        }

        inProgress = false;
    }

    public boolean onDiskUsed(ItemStack itemStack) {
        if(itemStack == null) {
            if(!diskInDrive) {return false;}
            markDirty();

            ItemStack ejectedStack = new ItemStack(DoesNotCompute.floppy);

            NBTTagCompound stackData = new NBTTagCompound();
            stackData.setString("diskFileName", diskFileName);
            ejectedStack.setTagCompound(stackData);
            if(diskItemName != null) {ejectedStack.setStackDisplayName(diskItemName);}

            worldObj.spawnEntityInWorld(new EntityItem(worldObj, pos.getX(), pos.getY(), pos.getZ(), ejectedStack));

            DoesNotCompute.copyArrayIntoFile(worldObj, diskFileName, diskData);

            setDiskInDrive(false);
            diskFileName = null;
            diskItemName = null;
        } else if(itemStack.getItem() instanceof ItemFloppy) {
            if(diskInDrive) {return false;}
            markDirty();

            setDiskInDrive(true);
            diskFileName = itemStack.hasTagCompound() ? itemStack.getTagCompound().getString("diskFileName") : null;
            if(diskFileName == null) {diskFileName = "disk_" + UUID.randomUUID().toString();}
            if(itemStack.hasDisplayName()) {diskItemName = itemStack.getDisplayName();}

            diskData = DoesNotCompute.getFileAsArray(worldObj, diskFileName);
            if(diskData.length > 0x40000) {diskData = Arrays.copyOf(diskData, 0x40000);}

            return true;
        }

        return false;
    }

    public void onPacketReceived(RedbusDataPacket dataPacket) {
        if(dataPacket.address != BUS_ADDR) {return;}
        markDirty();

        if(dataPacket.index == (byte) 0xFF && dataPacket.data == (byte) 0xFF) {
            for(int i=0;i<256;++i) {
                RedbusDataPacket.sendPacket(worldObj, pos, new RedbusDataPacket(TileEntityCPU.BUS_ADDR, redbusWindow[i], i));
            }
            return;
        }

        redbusWindow[dataPacket.index&255] = dataPacket.data;
        if((dataPacket.index&255) == 130 && dataPacket.data > 0 && dataPacket.data < 6) {
            if(diskInDrive) {
                currentOperation = dataPacket.data;
                inProgress = true;
            } else {
                currentOperation = 6;
                inProgress = true;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setBoolean("diskInDrive", diskInDrive);
        if(diskInDrive) {
            data.setString("diskFileName", diskFileName);
            if (diskItemName != null) {
                data.setString("diskItemName", diskItemName);
            }
        }

        data.setByteArray("redbusWindow", redbusWindow);
        data.setByteArray("diskData", diskData);

        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        diskInDrive = data.getBoolean("diskInDrive");
        if(diskInDrive) {
            diskFileName = data.getString("diskFileName");
            if(data.hasKey("diskItemName")) {
                diskItemName = data.getString("diskItemName");
            }
        }

        redbusWindow = data.getByteArray("redbusWindow");
        diskData = data.getByteArray("diskData");

        super.readFromNBT(data);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

}
