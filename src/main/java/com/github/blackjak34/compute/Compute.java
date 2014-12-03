package com.github.blackjak34.compute;

import net.minecraft.block.material.Material;

import com.github.blackjak34.compute.block.BlockComputer;
import com.github.blackjak34.compute.entity.tile.TileEntityComputer;
import com.github.blackjak34.compute.item.ItemFloppy;
import com.github.blackjak34.compute.packet.MessageKeyPressed;
import com.github.blackjak34.compute.packet.handler.HandlerKeyPressed;
import com.github.blackjak34.compute.proxy.CommonProxy;
import com.github.blackjak34.compute.proxy.client.ClientProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * The base class for the mod. Performs general setup,
 * cleanup, and registration.
 * 
 * @author Blackjak34
 * @since 1.0
 */
@Mod(modid = Compute.MODID, name = Compute.NAME, version = Compute.VERSION)
public class Compute {
	
	/**
	 * The unique identifier (used by Forge) for this mod.
	 */
    public static final String MODID = "doesnotcompute";
    
    /**
     * The human-readable name for this mod.
     */
    public static final String NAME = "Does Not Compute";
    
    /**
     * The version of this release.
     */
    public static final String VERSION = "1.0.1";
    
    public static SimpleNetworkWrapper networkWrapper;
    
    public static BlockComputer computer;
    
    public static ItemFloppy floppy;
    
    /**
     * The instance of this class that Forge uses. This is
     * the only instance that will ever be created.
     */
    @Instance(value = Compute.MODID)
    public static Compute instance;
    
    /**
     * The proxy to use. This proxy is {@link ClientProxy}
     * on the client-side, and {@link CommonProxy} on the
     * server-side.
     */
    @SidedProxy(clientSide="com.github.blackjak34.compute.proxy.client.ClientProxy",
    		serverSide="com.github.blackjak34.compute.proxy.CommonProxy")
    public static CommonProxy proxy;
    
    /**
     * This function is called by Forge before Minecraft is
     * initialized. Instantiates blocks/items, registers them
     * with Forge, and sets up packet channels for client/server
     * synchronization.
     * 
     * @param event The Forge event for pre-initialization
     */
    @SuppressWarnings("unused")
	@EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	computer = new BlockComputer(Material.iron);
    	
    	GameRegistry.registerBlock(computer, "blockComputer");
    	GameRegistry.registerTileEntity(TileEntityComputer.class, "tileEntityComputer");
    	
    	
    	floppy = new ItemFloppy();
    	
    	GameRegistry.registerItem(floppy, "itemFloppy");
    	
    	
    	networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Compute.MODID);
    	networkWrapper.registerMessage(HandlerKeyPressed.class, MessageKeyPressed.class, 1, Side.SERVER);
    }
    
    /**
     * This function is called by Forge after Minecraft is
     * initalized and before all other mods are
     * initialized. Utilized the proxy to register custom
     * renderers with Forge.
     * 
     * @param event The Forge event for initialization
     */
    @SuppressWarnings("unused")
	@EventHandler
    public void load(FMLInitializationEvent event) {
            proxy.registerRenderers();
            NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
    }
    
    /**
     * This function is called by Forge after Minecraft and
     * all mods have been loaded. Currently not used
     * because this mod does not interact with other mods.
     * 
     * @param event The Forge event for post-initialization
     */
    @SuppressWarnings("unused")
	@EventHandler
    public void postInit(FMLPostInitializationEvent event) {}
    
}
