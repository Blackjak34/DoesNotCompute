package com.github.blackjak34.compute;

import com.github.blackjak34.compute.block.BlockConsole;
import com.github.blackjak34.compute.entity.tile.TileEntityConsole;
import com.github.blackjak34.compute.entity.tile.TileEntityEmulator;
import com.github.blackjak34.compute.item.ItemFloppy;
import com.github.blackjak34.compute.packet.MessageActionPerformed;
import com.github.blackjak34.compute.packet.MessageKeyTyped;
import com.github.blackjak34.compute.packet.MessageUpdateCursor;
import com.github.blackjak34.compute.packet.MessageUpdateDisplay;
import com.github.blackjak34.compute.packet.handler.HandlerActionPerformed;
import com.github.blackjak34.compute.packet.handler.HandlerKeyTyped;
import com.github.blackjak34.compute.packet.handler.HandlerUpdateCursor;
import com.github.blackjak34.compute.packet.handler.HandlerUpdateDisplay;
import com.github.blackjak34.compute.proxy.CommonProxy;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = DoesNotCompute.MODID, name = DoesNotCompute.NAME, version = DoesNotCompute.VERSION)
public class DoesNotCompute {

    public static final String MODID = "doesnotcompute";

    public static final String NAME = "Does Not Compute";

    public static final String VERSION = "1.1.1";
    
    public static SimpleNetworkWrapper networkWrapper;
    
    public static BlockConsole console;
    
    public static ItemFloppy floppy;

    @Mod.Instance(value = DoesNotCompute.MODID)
    public static DoesNotCompute instance;

    @SidedProxy(clientSide="com.github.blackjak34.compute.proxy.client.ClientProxy",
    		serverSide="com.github.blackjak34.compute.proxy.CommonProxy")
    public static CommonProxy proxy;

    @SuppressWarnings("unused")
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	console = new BlockConsole(Material.iron);
    	
    	GameRegistry.registerBlock(console, "blockConsole");
    	GameRegistry.registerTileEntity(TileEntityConsole.class, "tileEntityConsole");
        GameRegistry.registerTileEntity(TileEntityEmulator.class, "tileEntityEmulator");
    	
    	
    	floppy = new ItemFloppy();
    	
    	GameRegistry.registerItem(floppy, "itemFloppy");
    	
    	networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(DoesNotCompute.MODID);
    	networkWrapper.registerMessage(HandlerActionPerformed.class, MessageActionPerformed.class, 0, Side.SERVER);
        networkWrapper.registerMessage(HandlerKeyTyped.class, MessageKeyTyped.class, 1, Side.SERVER);

        networkWrapper.registerMessage(HandlerUpdateCursor.class, MessageUpdateCursor.class, 2, Side.CLIENT);
        networkWrapper.registerMessage(HandlerUpdateDisplay.class, MessageUpdateDisplay.class, 3, Side.CLIENT);
    }

    @SuppressWarnings("unused")
	@Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerRenderers();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
    }

    @SuppressWarnings("unused")
	@Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {}
    
}
