package com.github.blackjak34.compute.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.github.blackjak34.compute.Compute;
import com.github.blackjak34.compute.entity.tile.TileEntityComputer;
import com.github.blackjak34.compute.enums.StateComputer;

/**
 * The Computer block. Serves as the physical world
 * component of the computer that allows players to access
 * the 6502 emulator within {@link TileEntityComputer}.
 * 
 * @author	Blackjak34
 * @since	1.0
 */
public class BlockComputer extends Block implements ITileEntityProvider {
	
	private int rotation;
	
	/**
	 * An array of icons containing the various sprites
	 * used (mostly) for the front of the computer.
	 */
	private static final IIcon[] icons = new IIcon[10];
	
	/**
	 * Almost identical to the constructor for
	 * {@link Block}, but also sets miscellaneous
	 * information about the block.
	 * 
	 * @param material The material to use for the block
	 */
	public BlockComputer(Material material) {
		super(material);
		
		setCreativeTab(CreativeTabs.tabMisc);
		setBlockName("blockComputer");
		setHarvestLevel("pickaxe", 1);
		setBlockTextureName("doesnotcompute:Computer_Side");
	}
	
	/**
	 * Registers the icons (textures) used for this Block.
	 * Most of these textures are used for the various
	 * states of the front face of the computer.
	 */
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons[0] = iconRegister.registerIcon("doesnotcompute:Computer_Front4");
		icons[1] = iconRegister.registerIcon("doesnotcompute:Computer_Front4_Disk");
		icons[2] = iconRegister.registerIcon("doesnotcompute:Computer_Front4_Reset");
		icons[3] = iconRegister.registerIcon("doesnotcompute:Computer_Front4_Reset_Disk");
		icons[4] = iconRegister.registerIcon("doesnotcompute:Computer_Front4_Halt");
		icons[5] = iconRegister.registerIcon("doesnotcompute:Computer_Front4_Halt_Disk");
		icons[6] = iconRegister.registerIcon("doesnotcompute:Computer_Front4_Run");
		icons[7] = iconRegister.registerIcon("doesnotcompute:Computer_Front4_Run_Disk");
		icons[8] = iconRegister.registerIcon("doesnotcompute:Computer_Back");
		icons[9] = iconRegister.registerIcon("doesnotcompute:Computer_Side");
		
		super.registerBlockIcons(iconRegister);
	}
	
	/**
	 * Automatically called by Forge to fetch the face
	 * textures of the block whenever its metadata changes.
	 * This function sets the front face accordingly, while
	 * keeping the other faces constant and accounting for
	 * rotation.
	 * 
	 * @return The texture associated with the specified side
	 */
	@Override
	public IIcon getIcon(int side, int metadata) {
		switch(rotation) {
			case 0:
				switch(side) {
					case 2:
						return icons[metadata];
					case 3:
						return icons[8];
					default:
						return icons[9];
				}
			case 1:
				switch(side) {
					case 5:
						return icons[metadata];
					case 4:
						return icons[8];
					default:
						return icons[9];
				}
			case 2:
				switch(side) {
					case 3:
						return icons[metadata];
					case 2:
						return icons[8];
					default:
						return icons[9];
				}
			case 3:
				switch(side) {
					case 4:
						return icons[metadata];
					case 5:
						return icons[8];
					default:
						return icons[9];
				}
			default:
				return icons[9];
		}
	}
	
	/**
	 * Called by Forge whenever a player right clicks on
	 * this block. The return value is whether or not the
	 * default action for the item in the player's hand
	 * should be performed (for example, placing a bucket
	 * of water). This is used to open a GUI interface
	 * that provides input to the emulator.
	 * 
	 * @return Whether or not to perform the item action
	 */
	@Override
	public boolean onBlockActivated(World world, int blockX, int blockY, int blockZ,
			EntityPlayer player, int par6, float playerX, float playerY, float playerZ) {
		player.openGui(Compute.instance, 42, world, blockX, blockY, blockZ);
		return true;
	}
	
	/**
	 * Called by Forge whenever this block is placed.
	 * A new instance of {@link TileEntityComputer} is
	 * returned.
	 * 
	 * @return A new TileEntityComputer instance
	 */
	@Override
	public TileEntity createNewTileEntity(World world, int par2) {
		return new TileEntityComputer();
	}
	
	/**
	 * Called by Forge when this block gets broken by any
	 * means (players or server). This function deletes the
	 * emulator instance that was associated with this
	 * block.
	 * THIS FUNCTION IS CALLED ON THE SERVER ONLY.
	 */
	@Override
	public void onBlockPreDestroy(World world, int blockX, int blockY, int blockZ, int metadataOld) {
		world.removeTileEntity(blockX, blockY, blockZ);
	}
	
	/**
	 * Forge calls this function whenever a block of this type
	 * is placed into the world by a player. The only purpose
	 * that this function serves is to calculate the rotation
	 * of the block based on the player's head yaw.
	 */
	@Override
	public int onBlockPlaced(World world, int blockX, int blockY, int blockZ, int side, float hitX, float hitY, float hitZ, int metadata) {
		rotation = (((int) Minecraft.getMinecraft().thePlayer.getRotationYawHead() + 45) / 90) % 4;
		
		return metadata;
	}
	
	/**
	 * This function performs some post-initialization for
	 * the TileEntity associated with this block. Currently
	 * the only thing that this function does is set the
	 * initial state ({@link StateComputer}) of the block.
	 */
	@Override
    public void onPostBlockPlaced(World world, int blockX, int blockY, int blockZ, int metadata) {
		((TileEntityComputer) world.getTileEntity(blockX, blockY, blockZ)).setState(StateComputer.RESET);
	}
	
}
