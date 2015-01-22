package com.github.blackjak34.compute.entity.tile;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import com.github.blackjak34.compute.packet.MessageUpdateDisplay;
import com.github.blackjak34.compute.redbus.RedbusDataPacket;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class TileEntityTerminal extends TileEntity implements IRedbusCompatible {

    public static final int BUS_ADDR = 1;

    private byte[] keyBuffer = new byte[16];
    private byte[] redbusWindow = new byte[256];
    private byte[][] displayBuffer = new byte[50][80];

    public TileEntityTerminal() {}

    public TileEntityTerminal(World worldIn) {}

    public void onKeyTyped(char keyTyped) {
        markDirty();

        keyBuffer[redbusWindow[5]] = (byte) keyTyped;
        redbusWindow[5]++;
        redbusWindow[5] &= 15;
        RedbusDataPacket.sendPacket(worldObj, pos, new RedbusDataPacket(TileEntityCPU.BUS_ADDR, redbusWindow[5], 5));
    }

    public void onPacketReceived(RedbusDataPacket dataPacket) {
        if(dataPacket.address != BUS_ADDR && (dataPacket.address&255) != 0xFF) {return;}
        markDirty();

        redbusWindow[dataPacket.index&255] = dataPacket.data;
        switch(dataPacket.index) {
            case 0:
                System.arraycopy(displayBuffer[(dataPacket.data&255) % 50], 0, redbusWindow, 16, 80);
                for(int i=16;i<96;i++) {
                    RedbusDataPacket.sendPacket(worldObj, pos, new RedbusDataPacket(TileEntityCPU.BUS_ADDR, redbusWindow[i], i));
                }
                break;
            case 1:case 2:case 3:
                worldObj.markBlockForUpdate(pos);
                break;
            case 4:
                redbusWindow[6] = keyBuffer[dataPacket.data&15];
                redbusWindow[4] &= 15;
                RedbusDataPacket.sendPacket(worldObj, pos, new RedbusDataPacket(TileEntityCPU.BUS_ADDR, redbusWindow[6], 6));
                break;
            case 5:
                redbusWindow[5] &= 15;
            case 7:
                int xStart = redbusWindow[8];
                int yStart = redbusWindow[9];
                int xOffset = redbusWindow[10];
                int yOffset = redbusWindow[11];
                int width = redbusWindow[12];
                int height = redbusWindow[13];

                switch(dataPacket.data) {
                    case 1:
                        int endY = yOffset + height;
                        int endX = xOffset + width;

                        for(int i=yOffset;i<endY;i++) {
                            for(int j=xOffset;j<endX;j++) {
                                displayBuffer[i][j] = (byte) xStart;
                                DoesNotCompute.networkWrapper.sendToDimension(
                                        new MessageUpdateDisplay(j, i, (byte) xStart, pos),
                                        worldObj.provider.getDimensionId());
                            }
                        }
                        break;
                    case 2:
                        break;
                    case 3:
                        for(int i=0;i<height;i++) {
                            for(int j=0;j<width;j++) {
                                byte newValue = displayBuffer[yStart+i][xStart+j];
                                displayBuffer[yOffset+i][xOffset+j] = newValue;
                                DoesNotCompute.networkWrapper.sendToDimension(
                                        new MessageUpdateDisplay(xOffset+j, yOffset+i, newValue, pos),
                                        worldObj.provider.getDimensionId());
                            }
                        }
                        break;
                }

                System.arraycopy(displayBuffer[(redbusWindow[0]&255) % 50], 0, redbusWindow, 16, 80);
                for(int i=16;i<96;i++) {
                    RedbusDataPacket.sendPacket(worldObj, pos, new RedbusDataPacket(TileEntityCPU.BUS_ADDR, redbusWindow[i], i));
                }
                break;
            default:
                if((dataPacket.index&255) > 0x09 && (dataPacket.index&255) < 0x60) {
                    DoesNotCompute.networkWrapper.sendToDimension(
                            new MessageUpdateDisplay((dataPacket.index&255)-16, redbusWindow[0], dataPacket.data, pos),
                            worldObj.provider.getDimensionId());
                }
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound data = new NBTTagCompound();

        data.setInteger("cursorX", redbusWindow[1]);
        data.setInteger("cursorY", redbusWindow[2]);
        data.setInteger("cursorMode", redbusWindow[3]);
        super.writeToNBT(data);

        return new S35PacketUpdateTileEntity(pos, 0, data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setByteArray("redbusWindow", redbusWindow);
        for(int i=0;i<displayBuffer.length;i++) {
            data.setByteArray("displayBuffer_row" + i, displayBuffer[i]);
        }

        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        redbusWindow = data.getByteArray("redbusWindow");
        for(int i=0;i<displayBuffer.length;i++) {
            displayBuffer[i] = data.getByteArray("displayBuffer_row" + i);
        }

        super.readFromNBT(data);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos coords, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

}
