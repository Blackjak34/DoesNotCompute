package com.github.blackjak34.compute.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageActionPerformed implements IMessage {

    private int buttonId;

    @SuppressWarnings("unused")
    public MessageActionPerformed() {}

    public MessageActionPerformed(int buttonId) {
        this.buttonId = buttonId;
    }

    public void fromBytes(ByteBuf buffer) {
        buttonId = buffer.readInt();
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(buttonId);
    }

    public int getButtonId() {
        return buttonId;
    }

}
