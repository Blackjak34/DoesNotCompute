package com.github.blackjak34.compute.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class MessageUpdateCursor implements IMessage {

    private int cursorX;
    private int cursorY;

    @SuppressWarnings("unused")
    public MessageUpdateCursor() {}

    public MessageUpdateCursor(int cursorX, int cursorY) {
        this.cursorX = cursorX;
        this.cursorY = cursorY;
    }

    public void fromBytes(ByteBuf buffer) {
        cursorX = buffer.readInt();
        cursorY = buffer.readInt();
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(cursorX);
        buffer.writeInt(cursorY);
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

}
