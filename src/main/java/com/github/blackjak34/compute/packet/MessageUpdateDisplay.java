package com.github.blackjak34.compute.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
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

    public void fromBytes(ByteBuf buffer) {
        coordX = buffer.readInt();
        coordY = buffer.readInt();
        value = buffer.readByte();
        location = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(coordX);
        buffer.writeInt(coordY);
        buffer.writeByte(value);
        buffer.writeInt(location.getX());
        buffer.writeInt(location.getY());
        buffer.writeInt(location.getZ());
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
