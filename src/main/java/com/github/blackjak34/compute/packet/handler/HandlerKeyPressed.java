package com.github.blackjak34.compute.packet.handler;

import net.minecraft.inventory.Container;

import com.github.blackjak34.compute.container.ContainerComputer;
import com.github.blackjak34.compute.packet.MessageKeyPressed;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * The serverside handler for a key pressed packet, sent by
 * GuiComputer whenever a valid key gets pressed.
 * All this handler does is simply write the key into the
 * TileEntity's screen buffer through the Container associated
 * with it and then marks it for an update to sync it with
 * the client.
 * 
 * @author Blackjak34
 * @since 1.1.0
 */
public class HandlerKeyPressed implements IMessageHandler<MessageKeyPressed, IMessage> {
	
	/**
	 * The function that is called whenever a MessageKeyPressed
	 * packet is recieved from the client.
	 * 
	 * @param message The packet that was sent (already deciphered)
	 * @param context Some additional data corresponding to the player that sent the packet
	 */
	@Override
	public IMessage onMessage(MessageKeyPressed message, MessageContext context) {
		Container container = context.getServerHandler().playerEntity.openContainer;
		
		if(container instanceof ContainerComputer) {
			((ContainerComputer) container).tiledata.onKeyPressed(message.getKeyPressed());
		}
		
		return null;
	}

}
