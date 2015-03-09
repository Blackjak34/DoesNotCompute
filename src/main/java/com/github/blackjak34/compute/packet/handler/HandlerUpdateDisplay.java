package com.github.blackjak34.compute.packet.handler;

import com.github.blackjak34.compute.entity.tile.client.TileEntityTerminalClient;
import com.github.blackjak34.compute.packet.MessageUpdateDisplay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HandlerUpdateDisplay implements IMessageHandler<MessageUpdateDisplay,IMessage> {

    public IMessage onMessage(MessageUpdateDisplay message, MessageContext context) {
        ((TileEntityTerminalClient) Minecraft.getMinecraft().theWorld.getTileEntity(message.getLocation()))
                .onDisplayUpdate(message.getCoordX(), message.getCoordY(), message.getValue());

        return null;
    }

}
