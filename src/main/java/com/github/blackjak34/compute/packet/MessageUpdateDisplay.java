package com.github.blackjak34.compute.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageUpdateDisplay implements IMessage {

    private int coordX;
    private int coordY;
    private byte value;

    private BlockPos location;

    @SuppressWarnings("unused")
    public MessageUpdateDisplay() {}

    public MessageUpdateDisplay(int coordX, int coordY, byte value, BlockPos location) {
        this.coordX = coordX;
        this.coordY = coordY;
        this.value = value;
        this.location = location;
    }

    public void fromBytes(ByteBuf buf) {
        coordX = buf.readInt();
        coordY = buf.readInt();
        value = buf.readByte();
        location = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(coordX);
        buf.writeInt(coordY);
        buf.writeByte(value);
        buf.writeInt(location.getX());
        buf.writeInt(location.getY());
        buf.writeInt(location.getZ());
    }

    public int getCoordX() {
        return coordX;
    }

    public int getCoordY() {
        return coordY;
    }

    public byte getValue() {
        return value;
    }

    public BlockPos getLocation() {
        return location;
    }

}
