package com.github.blackjak34.compute.proxy;

import com.github.blackjak34.compute.container.ContainerEmulator;
import com.github.blackjak34.compute.entity.tile.TileEntityConsole;
import com.github.blackjak34.compute.entity.tile.TileEntityEmulator;
import com.github.blackjak34.compute.gui.GuiConsole;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * The common proxy class. This contains code to return
 * GUI elements to clients/servers based on a given
 * mod-specific id number, specified within the GUI's
 * class.
 * 
 * @author Blackjak34
 * @since 1.0
 */
public class CommonProxy implements IGuiHandler {
	
    public void registerRenderers() {}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int blockX, int blockY, int blockZ) {
		switch(ID) {
			case GuiConsole.GUIID:
				return new ContainerEmulator((TileEntityEmulator) world.getTileEntity(new BlockPos(blockX, blockY, blockZ)));
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int blockX, int blockY, int blockZ) {
		switch(ID) {
			case GuiConsole.GUIID:
				return new GuiConsole((TileEntityConsole) world.getTileEntity(new BlockPos(blockX, blockY, blockZ)));
		}
		
		return null;
	}
    
}