package com.github.blackjak34.compute.proxy.client;

import com.github.blackjak34.compute.DoesNotCompute;
import com.github.blackjak34.compute.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;

/**
 * The client-side proxy class. Currently, this does
 * nothing because the mod contains no custom rendering.
 * 
 * @author Blackjak34
 * @since 1.0
 */
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

        modelRegistry.register(DoesNotCompute.floppy, 0,
                new ModelResourceLocation("doesnotcompute:itemFloppy", "inventory"));
    }
	
}