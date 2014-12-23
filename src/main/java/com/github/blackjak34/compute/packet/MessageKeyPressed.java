package com.github.blackjak34.compute.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * A packet containing a single char to be sent to the
 * server from the client whenever a key is pressed
 * within GuiComputer.
 * 
 * @author Blackjak34
 * @since 1.1.0
 */
public class MessageKeyPressed implements IMessage {
	
	/**
	 * The key that was pressed.
	 */
	private char keyPressed;
	
	/**
	 * An empty constructor to allow Forge to perform some
	 * reflection magic when it deciphers this packet. Even
	 * though this shouldn't be used in actual mod code, it
	 * shouldn't be removed either.
	 */
	@SuppressWarnings("unused")
	public MessageKeyPressed() {}
	
	/**
	 * The actual constructor, serving only to save the key
	 * that got pressed.
	 * 
	 * @param keyPressed The key that was pressed
	 */
	public MessageKeyPressed(char keyPressed) {
		this.keyPressed = keyPressed;
	}
	
	/**
	 * Writes this packet into a bytebuffer to prepare
	 * for distribution on the network. Forge automatically
	 * calls this function.
	 */
	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeChar(keyPressed);
	}
	
	/**
	 * Reads this packet from a bytebuffer streamed over
	 * the network back into usable data. Forge automatically
	 * calls this function.
	 */
	@Override
	public void fromBytes(ByteBuf buffer) {
		keyPressed = buffer.readChar();
	}
	
	/**
	 * A getter for the key stored in this packet.
	 * 
	 * @return The key that the client pressed
	 */
	public char getKeyPressed() {
		return keyPressed;
	}

}
