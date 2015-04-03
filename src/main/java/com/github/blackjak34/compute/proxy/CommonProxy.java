package com.github.blackjak34.compute.proxy;

import com.github.blackjak34.compute.container.ContainerBase;
import com.github.blackjak34.compute.entity.tile.client.TileEntityCPUClient;
import com.github.blackjak34.compute.entity.tile.client.TileEntityRedbus;
import com.github.blackjak34.compute.entity.tile.client.TileEntityTerminalClient;
import com.github.blackjak34.compute.gui.GuiCPU;
import com.github.blackjak34.compute.gui.GuiRedbus;
import com.github.blackjak34.compute.gui.GuiTerminal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler {
	
    public void registerRenderers() {}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int blockX, int blockY, int blockZ) {
		switch(ID) {
			case GuiTerminal.GUIID:case GuiCPU.GUIID:case GuiRedbus.GUIID:
				return new ContainerBase(world.getTileEntity(new BlockPos(blockX, blockY, blockZ)));
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int blockX, int blockY, int blockZ) {
		switch(ID) {
			case GuiTerminal.GUIID:
				return new GuiTerminal((TileEntityTerminalClient) world.getTileEntity(new BlockPos(blockX, blockY, blockZ)));
			case GuiCPU.GUIID:
				return new GuiCPU((TileEntityCPUClient) world.getTileEntity(new BlockPos(blockX, blockY, blockZ)));
            case GuiRedbus.GUIID:
                return new GuiRedbus((TileEntityRedbus) world.getTileEntity(new BlockPos(blockX, blockY, blockZ)));
		}
		
		return null;
	}
    
}