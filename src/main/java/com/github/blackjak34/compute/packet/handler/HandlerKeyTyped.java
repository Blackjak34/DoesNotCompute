package com.github.blackjak34.compute.packet.handler;

import com.github.blackjak34.compute.container.ContainerBase;
import com.github.blackjak34.compute.entity.tile.TileEntityTerminal;
import com.github.blackjak34.compute.packet.MessageKeyTyped;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HandlerKeyTyped implements IMessageHandler<MessageKeyTyped,IMessage> {

    public IMessage onMessage(MessageKeyTyped message, MessageContext context) {
        Container openContainer = context.getServerHandler().playerEntity.openContainer;
        if(!(openContainer instanceof ContainerBase)) {return null;}

        ((TileEntityTerminal) ((ContainerBase) openContainer).getTileEntity()).onKeyTyped(message.getKeyTyped());

        return null;
    }

}
