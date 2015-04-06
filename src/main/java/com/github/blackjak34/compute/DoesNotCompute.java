package com.github.blackjak34.compute;

import com.github.blackjak34.compute.block.*;
import com.github.blackjak34.compute.entity.tile.*;
import com.github.blackjak34.compute.entity.tile.client.*;
import com.github.blackjak34.compute.item.ItemFloppy;
import com.github.blackjak34.compute.item.ItemScrewdriver;
import com.github.blackjak34.compute.packet.MessageActionPerformed;
import com.github.blackjak34.compute.packet.MessageChangeAddress;
import com.github.blackjak34.compute.packet.MessageKeyTyped;
import com.github.blackjak34.compute.packet.MessageUpdateDisplay;
import com.github.blackjak34.compute.packet.handler.HandlerActionPerformed;
import com.github.blackjak34.compute.packet.handler.HandlerChangeAddress;
import com.github.blackjak34.compute.packet.handler.HandlerKeyTyped;
import com.github.blackjak34.compute.packet.handler.HandlerUpdateDisplay;
import com.github.blackjak34.compute.proxy.CommonProxy;
import com.google.common.io.Files;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
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
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

@Mod(modid = DoesNotCompute.MODID, name = DoesNotCompute.NAME, version = DoesNotCompute.VERSION)
public class DoesNotCompute {

    public static final String MODID = "doesnotcompute";

    public static final String NAME = "Does Not Compute";

    public static final String VERSION = "1.1.12";
    
    public static SimpleNetworkWrapper networkWrapper;
    
    public static BlockTerminal terminal;
    public static BlockCPU cpu;
    public static BlockDiskDrive diskDrive;
    public static BlockCableRibbon ribbonCable;
    public static BlockSID sid;
    
    public static ItemFloppy floppy;
    public static ItemScrewdriver screwdriver;

    @Mod.Instance(value = DoesNotCompute.MODID)
    public static DoesNotCompute instance;

    @SidedProxy(clientSide="com.github.blackjak34.compute.proxy.client.ClientProxy",
    		serverSide="com.github.blackjak34.compute.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static final CreativeTabs tabDoesNotCompute = new CreativeTabs("tabDoesNotCompute") {

        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return Item.getItemFromBlock(terminal);
        }

    };

    @SuppressWarnings("unused")
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	terminal = new BlockTerminal();
        cpu = new BlockCPU();
        diskDrive = new BlockDiskDrive();
        ribbonCable = new BlockCableRibbon();
        sid = new BlockSID();
    	
    	GameRegistry.registerBlock(terminal, "blockTerminal");
        GameRegistry.registerBlock(cpu, "blockCPU");
        GameRegistry.registerBlock(diskDrive, "blockDiskDrive");
        GameRegistry.registerBlock(ribbonCable, "blockCableRibbon");
        GameRegistry.registerBlock(sid, "blockSID");
        GameRegistry.registerTileEntity(TileEntityTerminal.class, "tileEntityTerminal");
    	GameRegistry.registerTileEntity(TileEntityTerminalClient.class, "tileEntityTerminalClient");
        GameRegistry.registerTileEntity(TileEntityCPU.class, "tileEntityEmulator");
        GameRegistry.registerTileEntity(TileEntityCPUClient.class, "tileEntityCPUClient");
        GameRegistry.registerTileEntity(TileEntityDiskDrive.class, "tileEntityDiskDrive");
        GameRegistry.registerTileEntity(TileEntityDiskDriveClient.class, "tileEntityDiskDriveClient");
        GameRegistry.registerTileEntity(TileEntityCableRibbon.class, "tileEntityCableRibbon");
        GameRegistry.registerTileEntity(TileEntityCableRibbonClient.class, "tileEntityCableRibbonClient");
        GameRegistry.registerTileEntity(TileEntitySID.class, "tileEntitySID");
        GameRegistry.registerTileEntity(TileEntitySIDClient.class, "tileEntitySIDClient");
    	
    	floppy = new ItemFloppy();
        screwdriver = new ItemScrewdriver();
    	
    	GameRegistry.registerItem(floppy, "itemFloppy");
        GameRegistry.registerItem(screwdriver, "itemScrewdriver");
    	
    	networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(DoesNotCompute.MODID);
    	networkWrapper.registerMessage(HandlerActionPerformed.class, MessageActionPerformed.class, 0, Side.SERVER);
        networkWrapper.registerMessage(HandlerKeyTyped.class, MessageKeyTyped.class, 1, Side.SERVER);
        networkWrapper.registerMessage(HandlerChangeAddress.class, MessageChangeAddress.class, 2, Side.SERVER);

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

    public static RandomAccessFile getRandomAccessFile(World world, String filename, String mode) {
        File modDirectory = new File(world.getSaveHandler().getWorldDirectory(), "/doesnotcompute/");
        File file = new File(modDirectory, filename);

        if(!modDirectory.exists() && !modDirectory.mkdir()) {
            System.err.println("Failed to generate a new directory in the world folder. Is it read only?");
            return null;
        }

        try {
            if(file.createNewFile()) {
                System.out.println("Generated new file " + filename + " in the mod directory.");
            }
        } catch(IOException e) {
            System.err.println("Failed to generate new file " + filename + " in the mod directory. Is it read only?");
            return null;
        }

        try {
            return new RandomAccessFile(file, mode);
        } catch(FileNotFoundException e) {
            System.err.println("Couldn't find the file " + filename + " in mod directory.");
            return null;
        }
    }
    
}
