package com.github.blackjak34.compute.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class MessageKeyTyped implements IMessage {

    private char keyTyped;

    @SuppressWarnings("unused")
    public MessageKeyTyped() {}

    public MessageKeyTyped(char keyTyped) {
        this.keyTyped = keyTyped;
    }

    public void fromBytes(ByteBuf buffer) {
        keyTyped = buffer.readChar();
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeChar(keyTyped);
    }

    public char getKeyTyped() {
        return keyTyped;
    }

}
