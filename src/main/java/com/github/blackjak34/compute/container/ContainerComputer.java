package com.github.blackjak34.compute.container;

import com.github.blackjak34.compute.entity.tile.TileEntityComputer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * A Container for the serverside implementation of
 * GuiComputer. All this does at the moment
 * is store a reference to the serverside TileEntity
 * because the GUI doesn't actually have any inventory
 * that the server needs to manage; all of the managing
 * is done in the TileEntity class.
 * 
 * @author Blackjak34
 * @since 1.1.0
 */
public class ContainerComputer extends Container {
	
	/**
	 * The TileEntityComputer that created this Container
	 */
	public final TileEntityComputer tiledata;
	
	/**
	 * Only serves to store a reference to the TileEntity
	 * that generated this Container.
	 * 
	 * @param tiledata The TileEntityComputer associated with this Container
	 */
	public ContainerComputer(TileEntityComputer tiledata) {
		this.tiledata = tiledata;
	}
	
	/**
	 * Not sure exactly how this is used, but always
	 * returning true since this has no inventory seems
	 * to do the trick. Will most likely change once I
	 * figure out what this does.
	 */
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

}
