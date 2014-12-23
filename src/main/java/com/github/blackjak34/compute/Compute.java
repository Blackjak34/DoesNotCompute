package com.github.blackjak34.compute;

import com.github.blackjak34.compute.block.BlockComputer;
import com.github.blackjak34.compute.entity.tile.TileEntityComputer;
import com.github.blackjak34.compute.item.ItemFloppy;
import com.github.blackjak34.compute.packet.MessageButtonClicked;
import com.github.blackjak34.compute.packet.MessageKeyPressed;
import com.github.blackjak34.compute.packet.handler.HandlerButtonClicked;
import com.github.blackjak34.compute.packet.handler.HandlerKeyPressed;
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
    public static final String VERSION = "1.1.1";
    
    public static SimpleNetworkWrapper networkWrapper;
    
    public static BlockComputer computer;
    
    public static ItemFloppy floppy;
    
    /**
     * The instance of this class that Forge uses. This is
     * the only instance that will ever be created.
     */
    @Mod.Instance(value = Compute.MODID)
    public static Compute instance;
    
    /**
     * The proxy to use. This proxy is {@link com.github.blackjak34.compute.proxy.client.ClientProxy}
     * on the client-side, and {@link com.github.blackjak34.compute.proxy.CommonProxy} on the
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
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	computer = new BlockComputer(Material.iron);
    	
    	GameRegistry.registerBlock(computer, "blockComputer");
    	GameRegistry.registerTileEntity(TileEntityComputer.class, "tileEntityComputer");
    	
    	
    	floppy = new ItemFloppy();
    	
    	GameRegistry.registerItem(floppy, "itemFloppy");
    	
    	networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Compute.MODID);
    	networkWrapper.registerMessage(HandlerKeyPressed.class, MessageKeyPressed.class, 1, Side.SERVER);
        networkWrapper.registerMessage(HandlerButtonClicked.class, MessageButtonClicked.class, 2, Side.SERVER);
    }
    
    /**
     * This function is called by Forge after Minecraft is
     * initialized and before all other mods are
     * initialized. Utilized the proxy to register custom
     * renderers with Forge.
     * 
     * @param event The Forge event for initialization
     */
    @SuppressWarnings("unused")
	@Mod.EventHandler
    public void init(FMLInitializationEvent event) {
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
	@Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {}
    
}
