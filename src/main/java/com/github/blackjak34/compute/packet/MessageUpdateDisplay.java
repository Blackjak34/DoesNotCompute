package com.github.blackjak34.compute.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class MessageUpdateDisplay implements IMessage {

    private int index;
    private byte value;

    @SuppressWarnings("unused")
    public MessageUpdateDisplay() {}

    public MessageUpdateDisplay(int index, byte value) {
        this.index = index;
        this.value = value;
    }

    public void fromBytes(ByteBuf buffer) {
        index = buffer.readInt();
        value = buffer.readByte();
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(index);
        buffer.writeByte(value);
    }

    public int getIndex() {
        return index;
    }

    public byte getValue() {
        return value;
    }

}
