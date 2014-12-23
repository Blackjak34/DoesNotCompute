package com.github.blackjak34.compute.proxy.client;

import com.github.blackjak34.compute.Compute;
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
        modelRegistry.register(Item.getItemFromBlock(Compute.computer), 0, new ModelResourceLocation("doesnotcompute:blockComputer", "inventory"));
        modelRegistry.register(Compute.floppy, 0, new ModelResourceLocation("doesnotcompute:itemFloppy", "inventory"));
    }
	
}