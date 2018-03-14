package com.github.blackjak34.compute.proxy.client;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

	@Override
    public void registerRenderers() {
        ItemModelMesher modelRegistry = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        modelRegistry.register(Item.getItemFromBlock(DoesNotCompute.terminal), 0,
                new ModelResourceLocation("doesnotcompute:blockTerminal", "inventory"));
        modelRegistry.register(Item.getItemFromBlock(DoesNotCompute.cpu), 0,
                new ModelResourceLocation("doesnotcompute:blockCPU", "inventory"));
        modelRegistry.register(Item.getItemFromBlock(DoesNotCompute.diskDrive), 0,
                new ModelResourceLocation("doesnotcompute:blockDiskDrive", "inventory"));
        modelRegistry.register(Item.getItemFromBlock(DoesNotCompute.ribbonCable), 0,
                new ModelResourceLocation("doesnotcompute:blockCableRibbon", "inventory"));
        modelRegistry.register(Item.getItemFromBlock(DoesNotCompute.sid), 0,
                new ModelResourceLocation("doesnotcompute:blockSID", "inventory"));
        modelRegistry.register(Item.getItemFromBlock(DoesNotCompute.cardPunch), 0,
                new ModelResourceLocation("doesnotcompute:blockCardPunch", "inventory"));

        modelRegistry.register(DoesNotCompute.floppy, 0,
                new ModelResourceLocation("doesnotcompute:itemFloppy", "inventory"));
        modelRegistry.register(DoesNotCompute.systemFloppy, 0,
                new ModelResourceLocation("doesnotcompute:itemSystemFloppy", "inventory"));
        modelRegistry.register(DoesNotCompute.screwdriver, 0,
                new ModelResourceLocation("doesnotcompute:itemScrewdriver", "inventory"));
        modelRegistry.register(DoesNotCompute.punchCard, 0,
                new ModelResourceLocation("doesnotcompute:itemPunchCard", "inventory"));
        modelRegistry.register(DoesNotCompute.punchCardStack, 0,
                new ModelResourceLocation("doesnotcompute:itemPunchCardStack", "inventory"));
        modelRegistry.register(DoesNotCompute.playerSaddle, 0,
                new ModelResourceLocation("doesnotcompute:itemPlayerSaddle", "inventory"));

        ClientRegistry.registerKeyBinding(KeyBindings.punchFeed);
        ClientRegistry.registerKeyBinding(KeyBindings.punchRegister);
        ClientRegistry.registerKeyBinding(KeyBindings.punchRelease);
        ClientRegistry.registerKeyBinding(KeyBindings.punchLZero);
        ClientRegistry.registerKeyBinding(KeyBindings.punchMPunch);
        ClientRegistry.registerKeyBinding(KeyBindings.punchDup);
        ClientRegistry.registerKeyBinding(KeyBindings.punchAuxDup);
        ClientRegistry.registerKeyBinding(KeyBindings.punchSkip);
        ClientRegistry.registerKeyBinding(KeyBindings.punchProgOne);
        ClientRegistry.registerKeyBinding(KeyBindings.punchProgTwo);
        ClientRegistry.registerKeyBinding(KeyBindings.punchAlpha);
        ClientRegistry.registerKeyBinding(KeyBindings.punchNumeric);
        ClientRegistry.registerKeyBinding(KeyBindings.punchMaster);
        ClientRegistry.registerKeyBinding(KeyBindings.punchCent);
        ClientRegistry.registerKeyBinding(KeyBindings.punchPrime);
    }

}
