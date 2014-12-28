package com.github.blackjak34.compute.packet.handler;

import com.github.blackjak34.compute.container.ContainerEmulator;
import com.github.blackjak34.compute.packet.MessageKeyTyped;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class HandlerKeyTyped implements IMessageHandler<MessageKeyTyped,IMessage> {

    public IMessage onMessage(MessageKeyTyped message, MessageContext context) {
        Container openContainer = context.getServerHandler().playerEntity.openContainer;
        if(!(openContainer instanceof ContainerEmulator)) {return null;}

        ((ContainerEmulator) openContainer).getEmulator().onKeyTyped(message.getKeyTyped());

        return null;
    }

}
