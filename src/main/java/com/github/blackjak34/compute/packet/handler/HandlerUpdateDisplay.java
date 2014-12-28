package com.github.blackjak34.compute.packet.handler;

import com.github.blackjak34.compute.container.ContainerConsole;
import com.github.blackjak34.compute.packet.MessageUpdateDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class HandlerUpdateDisplay implements IMessageHandler<MessageUpdateDisplay,IMessage> {

    public IMessage onMessage(MessageUpdateDisplay message, MessageContext context) {
        Container openContainer = Minecraft.getMinecraft().thePlayer.openContainer;
        if(!(openContainer instanceof ContainerConsole)) {return null;}

        ((ContainerConsole) openContainer).getConsole().onDisplayUpdate(message.getIndex(), message.getValue());

        return null;
    }

}
