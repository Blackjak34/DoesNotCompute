package com.github.blackjak34.compute.packet.handler;

import com.github.blackjak34.compute.container.ContainerComputer;
import com.github.blackjak34.compute.packet.MessageButtonClicked;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * A simple handler for {@link MessageButtonClicked} packets being
 * received on the server side. Calls the corresponding TileEntity's
 * onButtonClicked function with the packet's button id.
 *
 * @author Blackjak34
 * @since 1.1.1
 */
public class HandlerButtonClicked implements IMessageHandler<MessageButtonClicked, IMessage> {

    @Override
    public IMessage onMessage(MessageButtonClicked message, MessageContext context) {
        Container container = context.getServerHandler().playerEntity.openContainer;

        if(container instanceof ContainerComputer) {
            ((ContainerComputer) container).tiledata.onButtonClicked(message.getButtonId());
        }

        return null;
    }

}
