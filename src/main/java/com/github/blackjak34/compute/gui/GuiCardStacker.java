package com.github.blackjak34.compute.gui;

import com.github.blackjak34.compute.entity.tile.TileEntityCardPunch;
import net.minecraft.entity.player.EntityPlayer;

public class GuiCardStacker extends GuiCardHopper {

    public static final int GUIID = 92;

    public GuiCardStacker(TileEntityCardPunch tileEntity, EntityPlayer player) {
        super(tileEntity, player, false);
    }

}
