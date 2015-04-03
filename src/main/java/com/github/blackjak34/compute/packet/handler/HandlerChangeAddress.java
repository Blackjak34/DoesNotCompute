package com.github.blackjak34.compute.packet.handler;

import com.github.blackjak34.compute.container.ContainerBase;
import com.github.blackjak34.compute.interfaces.IRedbusCompatible;
import com.github.blackjak34.compute.packet.MessageChangeAddress;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class HandlerChangeAddress implements IMessageHandler<MessageChangeAddress,IMessage> {

    public IMessage onMessage(MessageChangeAddress message, MessageContext context) {
        Container openContainer = context.getServerHandler().playerEntity.openContainer;
        if(!(openContainer instanceof ContainerBase)) {return null;}

        ((IRedbusCompatible) ((ContainerBase) openContainer).getTileEntity()).setBusAddress(message.getBusAddress());

        return null;
    }

}
