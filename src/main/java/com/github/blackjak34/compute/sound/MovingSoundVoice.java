package com.github.blackjak34.compute.sound;

import com.github.blackjak34.compute.entity.tile.client.TileEntitySIDClient;
import com.github.blackjak34.compute.enums.Waveform;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MovingSoundVoice extends MovingSound {

    private TileEntitySIDClient sid;

    private final Waveform waveform;

    public MovingSoundVoice(TileEntitySIDClient sid, Waveform waveform, ResourceLocation soundName) {
        super(soundName);
        this.waveform = waveform;

        this.sid = sid;
        BlockPos sidPos = sid.getPos();
        xPosF = sidPos.getX();
        yPosF = sidPos.getY();
        zPosF = sidPos.getZ();

        repeat = true;
        repeatDelay = 0;
    }

    public void update() {
        if(sid == null || sid.isInvalid()) {donePlaying = true;}
    }

    public Waveform getWaveform() {
        return waveform;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void delete() {
        sid = null;
    }

}
