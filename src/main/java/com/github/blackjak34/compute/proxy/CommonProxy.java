package com.github.blackjak34.compute.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.github.blackjak34.compute.container.ContainerComputer;
import com.github.blackjak34.compute.entity.tile.TileEntityComputer;
import com.github.blackjak34.compute.gui.GuiComputer;

import cpw.mods.fml.common.network.IGuiHandler;

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
			case GuiComputer.GUIID:
				return new ContainerComputer((TileEntityComputer) world.getTileEntity(blockX, blockY, blockZ));
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int blockX, int blockY, int blockZ) {
		switch(ID) {
			case GuiComputer.GUIID:
				return new GuiComputer((TileEntityComputer) world.getTileEntity(blockX, blockY, blockZ));
		}
		
		return null;
	}
    
}