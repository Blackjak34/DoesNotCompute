package com.github.blackjak34.compute.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class MessageChangeAddress implements IMessage {

    private int busAddress;

    @SuppressWarnings("unused")
    public MessageChangeAddress() {}

    public MessageChangeAddress(int busAddress) {
        this.busAddress = busAddress;
    }

    public void fromBytes(ByteBuf buffer) {
        busAddress = buffer.readInt();
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(busAddress);
    }

    public int getBusAddress() {
        return busAddress;
    }

}
