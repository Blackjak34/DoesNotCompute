package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.block.BlockDiskDrive;
import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import com.github.blackjak34.compute.item.ItemFloppy;
import com.github.blackjak34.compute.item.ItemSystemFloppy;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

public class TileEntityDiskDrive extends TileEntity implements IUpdatePlayerListBox, IRedbusCompatible {

    private static String DEFAULT_DISK_NAME = "Floppy Disk";

    private int diskCommand = 0;
    private int sectorNumber = 0;
    private int busAddress = 2;

    private boolean diskInDrive = false;
    private boolean isDiskSystem = false;
    private boolean inProgress = false;
    private boolean operationComplete = true;

    private byte[] sectorBuffer;

    private String diskPath;
    private String diskName = DEFAULT_DISK_NAME;

    public TileEntityDiskDrive() {}

    public TileEntityDiskDrive(World worldIn) {
        sectorBuffer = new byte[128];
    }

    public void update() {
        if(!inProgress) {
            return;
        } else if(operationComplete) {
            setInProgess(false);
        }
        operationComplete = true;

        markDirty();

        if(!diskInDrive) {
            diskCommand = 0xFF;
            return;
        }

        byte[] diskNameBytes;
        switch(diskCommand) {
            case 1:
                diskNameBytes = diskName.getBytes(Charset.forName("US-ASCII"));
                System.arraycopy(diskNameBytes, 0, sectorBuffer, 0, 64);
                for(int i=diskNameBytes.length;i<64;++i) {sectorBuffer[i] = 0;}
                break;
            case 2:
                if(isDiskSystem) {
                    diskCommand = 0xFF;
                    return;
                }
                int nameLength = 0;
                for(;nameLength<64;++nameLength) {
                    if(sectorBuffer[nameLength] == 0) {break;}
                }
                diskName = new String(sectorBuffer, 0, nameLength, Charset.forName("US-ASCII"));
                break;
            case 3:
                if(isDiskSystem) {
                    for(int i=0;i<15;++i) {sectorBuffer[i] = 0;}
                    sectorBuffer[15] = 1;
                } else {
                    UUID diskID = UUID.fromString(diskPath.substring(5));
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    buffer.putLong(diskID.getMostSignificantBits());
                    buffer.putLong(diskID.getLeastSignificantBits());
                    System.arraycopy(buffer.array(), 0, sectorBuffer, 0, 16);
                }
                break;
            case 4:
                if(isDiskSystem) {
                    InputStream inputStream = DoesNotCompute.instance.getResourceFromAssetsDirectory(diskPath);

                    try {
                        if(inputStream.available() < (sectorNumber+1)*128) {
                            diskCommand = 0xFF;
                            return;
                        }

                        inputStream.skip(sectorNumber * 128);
                        inputStream.read(sectorBuffer, 0, 128);
                    } catch(IOException e) {
                        e.printStackTrace();
                        diskCommand = 0xFF;
                        return;
                    } finally {
                        try {
                            inputStream.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // TODO make this persistent instead of fetching a new object every time
                    RandomAccessFile inputStream = DoesNotCompute.getFileFromWorldDirectory(worldObj, diskPath, "r");
                    if (inputStream == null) {
                        diskCommand = 0xFF;
                        return;
                    }

                    try {
                        if (inputStream.length() < (sectorNumber + 1) * 128) {
                            diskCommand = 0xFF;
                            return;
                        }

                        inputStream.seek(sectorNumber * 128);
                        inputStream.read(sectorBuffer, 0, 128);
                    } catch (IOException e) {
                        e.printStackTrace();
                        diskCommand = 0xFF;
                        return;
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case 5:
                if(isDiskSystem) {
                    diskCommand = 0xFF;
                    return;
                }

                // TODO make this persistent instead of fetching a new object every time
                RandomAccessFile outputStream = DoesNotCompute.getFileFromWorldDirectory(worldObj, diskPath, "rw");
                if(outputStream == null) {
                    diskCommand = 0xFF;
                    return;
                }

                try {
                    outputStream.seek(sectorNumber * 128);
                    outputStream.write(sectorBuffer, 0, 128);
                } catch(IOException e) {
                    e.printStackTrace();
                    diskCommand = 0xFF;
                    return;
                } finally {
                    try {
                        outputStream.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                diskCommand = 0xFF;
                return;
        }
        diskCommand = 0;
    }

    public boolean onDiskUsed(ItemStack itemStack) {
        if(itemStack == null) {
            if(!diskInDrive) {return false;}
            markDirty();

            ejectFloppyDisk();

            setDiskInDrive(false);
            diskPath = null;
            diskName = null;
        } else if(itemStack.getItem() instanceof ItemFloppy) {
            if(diskInDrive) {return false;}
            markDirty();

            setDiskInDrive(true);
            diskPath = itemStack.hasTagCompound() ? itemStack.getTagCompound().getString("diskPath") : null;
            if(diskPath == null) {diskPath = "disk_" + UUID.randomUUID().toString();}
            if(itemStack.hasDisplayName()) {
                diskName = itemStack.getDisplayName();
            } else {
                diskName = DEFAULT_DISK_NAME;
            }

            isDiskSystem = false;
            return true;
        } else if(itemStack.getItem() instanceof ItemSystemFloppy) {
            if(diskInDrive) {return false;}
            markDirty();

            setDiskInDrive(true);
            diskPath = "roms/forth";
            diskName = "System Floppy Disk";

            isDiskSystem = true;
            return true;
        }

        return false;
    }

    // This should only be called externally when the block gets broken
    public void ejectFloppyDisk() {
        if(!diskInDrive) {return;}

        ItemStack ejectedStack;
        if(isDiskSystem) {
            ejectedStack = new ItemStack(DoesNotCompute.systemFloppy);
        } else {
            ejectedStack = new ItemStack(DoesNotCompute.floppy);

            NBTTagCompound stackData = new NBTTagCompound();
            stackData.setString("diskPath", diskPath);
            ejectedStack.setTagCompound(stackData);

            if(diskName != DEFAULT_DISK_NAME) {
                ejectedStack.setStackDisplayName(diskName);
            }
        }

        worldObj.spawnEntityInWorld(new EntityItem(worldObj, pos.getX(), pos.getY(), pos.getZ(), ejectedStack));
    }

    public boolean isDevice() {
        return true;
    }

    public int getBusAddress() {
        return busAddress;
    }

    public void setBusAddress(int newAddress) {
        busAddress = newAddress;
        worldObj.markBlockForUpdate(pos);
    }

    public void write(int index, int value) {
        if(index < 0x80) {
            sectorBuffer[index] = (byte) value;
        } else if(index == 0x80) {
            sectorNumber = (sectorNumber&0xFF00) | value;
        } else if(index == 0x81) {
            sectorNumber = (sectorNumber&0xFF) | (value << 8);
        } else if(index == 0x82) {
            diskCommand = value;
            setInProgess(true);
            operationComplete = false;
        }
    }

    public int read(int index) {
        if(index < 0x80) {return sectorBuffer[index] & 255;}
        if(index == 0x80) {return sectorNumber & 255;}
        if(index == 0x81) {return sectorNumber >>> 8;}
        if(index == 0x82) {return diskCommand;}
        return 0xFF;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("busAddress", busAddress);

        return new S35PacketUpdateTileEntity(pos, 0, data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setInteger("diskCommand", diskCommand);
        data.setInteger("sectorNumber", sectorNumber);
        data.setInteger("busAddress", busAddress);
        data.setBoolean("inProgress", inProgress);
        data.setBoolean("diskInDrive", diskInDrive);
        data.setBoolean("isDiskSystem", isDiskSystem);
        if(diskInDrive) {
            data.setString("diskPath", diskPath);
            if(diskName != DEFAULT_DISK_NAME) {
                data.setString("diskName", diskName);
            }
        }

        data.setByteArray("sectorBuffer", sectorBuffer);

        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        diskCommand = data.getInteger("diskCommand");
        sectorNumber = data.getInteger("sectorNumber");
        busAddress = data.getInteger("busAddress");
        inProgress = data.getBoolean("inProgress");
        diskInDrive = data.getBoolean("diskInDrive");
        isDiskSystem = data.getBoolean("isDiskSystem");
        if(diskInDrive) {
            diskPath = data.getString("diskPath");
            if(data.hasKey("diskName")) {
                diskName = data.getString("diskName");
            }
        }

        sectorBuffer = data.getByteArray("sectorBuffer");

        super.readFromNBT(data);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    private void setDiskInDrive(boolean value) {
        diskInDrive = value;

        worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockDiskDrive.DISK, value));
    }

    private void setInProgess(boolean value) {
        inProgress = value;

        worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockDiskDrive.ACTIVE, value));
    }

}
