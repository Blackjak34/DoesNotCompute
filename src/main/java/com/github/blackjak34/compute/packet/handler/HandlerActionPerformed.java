package com.github.blackjak34.compute.packet.handler;

import com.github.blackjak34.compute.container.ContainerEmulator;
import com.github.blackjak34.compute.packet.MessageActionPerformed;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class HandlerActionPerformed implements IMessageHandler<MessageActionPerformed,IMessage> {

    public IMessage onMessage(MessageActionPerformed message, MessageContext context) {
        Container openContainer = context.getServerHandler().playerEntity.openContainer;
        if(!(openContainer instanceof ContainerEmulator)) {return null;}

        ((ContainerEmulator) openContainer).getEmulator().onActionPerformed(message.getButtonId());

        return null;
    }

}
