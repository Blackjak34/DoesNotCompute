package com.github.blackjak34.compute.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * A simple packet holding a single int value for sending button presses
 * in GUIs to the server from the client side.
 *
 * @author Blackjak34
 * @since 1.1.1
 */
public class MessageButtonClicked implements IMessage {

    /**
     * The button id to be sent to the server.
     */
    private int buttonId;

    /**
     * An empty constructor to allow Forge to use reflection. Do not
     * delete this!
     */
    @SuppressWarnings("unused")
    public MessageButtonClicked() {}

    /**
     * A basic constructor that sets buttonId to the given value.
     *
     * @param buttonId The id to be sent to the server
     */
    public MessageButtonClicked(int buttonId) {
        this.buttonId = buttonId;
    }

    /**
     * Writes this packet into a bytebuffer to prepare
     * for distribution on the network. Forge automatically
     * calls this function.
     */
    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(buttonId);
    }

    /**
     * Reads this packet from a bytebuffer streamed over
     * the network back into usable data. Forge automatically
     * calls this function.
     */
    @Override
    public void fromBytes(ByteBuf buffer) {
        buttonId = buffer.readInt();
    }

    /**
     * Called on the server side to retrieve the button id that was stored
     * in this packet prior to sending.
     *
     * @return The id of the button that was pressed
     */
    public int getButtonId() {
        return buttonId;
    }

}
