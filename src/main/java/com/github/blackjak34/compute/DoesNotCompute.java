package com.github.blackjak34.compute;

import com.github.blackjak34.compute.block.BlockCPU;
import com.github.blackjak34.compute.block.BlockCableRibbon;
import com.github.blackjak34.compute.block.BlockDiskDrive;
import com.github.blackjak34.compute.block.BlockTerminal;
import com.github.blackjak34.compute.entity.tile.TileEntityCableRibbon;
import com.github.blackjak34.compute.entity.tile.TileEntityDiskDrive;
import com.github.blackjak34.compute.entity.tile.TileEntityTerminal;
import com.github.blackjak34.compute.entity.tile.client.TileEntityCPUClient;
import com.github.blackjak34.compute.entity.tile.client.TileEntityCableRibbonClient;
import com.github.blackjak34.compute.entity.tile.client.TileEntityDiskDriveClient;
import com.github.blackjak34.compute.entity.tile.client.TileEntityTerminalClient;
import com.github.blackjak34.compute.entity.tile.TileEntityCPU;
import com.github.blackjak34.compute.item.ItemFloppy;
import com.github.blackjak34.compute.packet.MessageActionPerformed;
import com.github.blackjak34.compute.packet.MessageKeyTyped;
import com.github.blackjak34.compute.packet.MessageUpdateDisplay;
import com.github.blackjak34.compute.packet.handler.HandlerActionPerformed;
import com.github.blackjak34.compute.packet.handler.HandlerKeyTyped;
import com.github.blackjak34.compute.packet.handler.HandlerUpdateDisplay;
import com.github.blackjak34.compute.proxy.CommonProxy;
import com.google.common.io.Files;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.io.IOException;

@Mod(modid = DoesNotCompute.MODID, name = DoesNotCompute.NAME, version = DoesNotCompute.VERSION)
public class DoesNotCompute {

    public static final String MODID = "doesnotcompute";

    public static final String NAME = "Does Not Compute";

    public static final String VERSION = "1.1.1";
    
    public static SimpleNetworkWrapper networkWrapper;
    
    public static BlockTerminal terminal;
    public static BlockCPU cpu;
    public static BlockDiskDrive diskDrive;
    public static BlockCableRibbon ribbonCable;
    
    public static ItemFloppy floppy;

    @Mod.Instance(value = DoesNotCompute.MODID)
    public static DoesNotCompute instance;

    @SidedProxy(clientSide="com.github.blackjak34.compute.proxy.client.ClientProxy",
    		serverSide="com.github.blackjak34.compute.proxy.CommonProxy")
    public static CommonProxy proxy;

    @SuppressWarnings("unused")
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	terminal = new BlockTerminal();
        cpu = new BlockCPU();
        diskDrive = new BlockDiskDrive();
        ribbonCable = new BlockCableRibbon();
    	
    	GameRegistry.registerBlock(terminal, "blockTerminal");
        GameRegistry.registerBlock(cpu, "blockCPU");
        GameRegistry.registerBlock(diskDrive, "blockDiskDrive");
        GameRegistry.registerBlock(ribbonCable, "blockCableRibbon");
        GameRegistry.registerTileEntity(TileEntityTerminal.class, "tileEntityTerminal");
    	GameRegistry.registerTileEntity(TileEntityTerminalClient.class, "tileEntityTerminalClient");
        GameRegistry.registerTileEntity(TileEntityCPU.class, "tileEntityEmulator");
        GameRegistry.registerTileEntity(TileEntityCPUClient.class, "tileEntityCPUClient");
        GameRegistry.registerTileEntity(TileEntityDiskDrive.class, "tileEntityDiskDrive");
        GameRegistry.registerTileEntity(TileEntityDiskDriveClient.class, "tileEntityDiskDriveClient");
        GameRegistry.registerTileEntity(TileEntityCableRibbon.class, "tileEntityCableRibbon");
        GameRegistry.registerTileEntity(TileEntityCableRibbonClient.class, "tileEntityCableRibbonClient");
    	
    	
    	floppy = new ItemFloppy();
    	
    	GameRegistry.registerItem(floppy, "itemFloppy");
    	
    	networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(DoesNotCompute.MODID);
    	networkWrapper.registerMessage(HandlerActionPerformed.class, MessageActionPerformed.class, 0, Side.SERVER);
        networkWrapper.registerMessage(HandlerKeyTyped.class, MessageKeyTyped.class, 1, Side.SERVER);

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

    public static byte[] getFileAsArray(World world, String filename) {
        File modDirectory = new File(world.getSaveHandler().getWorldDirectory(), "/doesnotcompute/");
        File dataFile = new File(modDirectory, filename);

        if(!modDirectory.exists() && !modDirectory.mkdir()) {
            System.err.println("Could not generate the mod directory. Is the world folder read only?");
            return new byte[0];
        }

        if(!dataFile.exists()) {
            System.err.println("The requested file at " + dataFile.getAbsolutePath() + " does not exist.");
            return new byte[0];
        }

        try {
            return Files.toByteArray(dataFile);
        } catch(IOException e) {
            System.err.println("There was an error reading data from the file at" + dataFile.getAbsolutePath() + ":");
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static void copyFileIntoArray(World world, String filename, byte[] dest, int index, int maxLength) {
        byte[] data = getFileAsArray(world, filename);

        System.arraycopy(data, 0, dest, index, Math.min(data.length, maxLength));
    }

    public static boolean copyArrayIntoFile(World world, String filename, byte[] src) {
        File modDirectory = new File(world.getSaveHandler().getWorldDirectory(), "/doesnotcompute/");
        File dataFile = new File(modDirectory, filename);

        if(!modDirectory.exists() && !modDirectory.mkdir()) {
            System.err.println("Could not generate the mod directory. Is the world folder read only?");
            return false;
        }

        try {
            if(dataFile.createNewFile()) {
                System.out.println("Generated a new data file at " + dataFile.getAbsolutePath() + ".");
            }
        } catch(IOException e) {
            System.err.println("Could not generate a new data file at " + dataFile.getAbsolutePath() +
                    ". Is the world folder read only?");
            return false;
        }

        try {
            Files.write(src, dataFile);
        } catch(IOException e) {
            System.err.println("There was an error writing data to the file at" + dataFile.getAbsolutePath() + ":");
            e.printStackTrace();
            return false;
        }

        return true;
    }
    
}
