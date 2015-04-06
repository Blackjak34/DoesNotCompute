package com.github.blackjak34.compute.entity.tile.client;

import com.github.blackjak34.compute.enums.Waveform;
import com.github.blackjak34.compute.sound.MovingSoundVoice;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public class TileEntitySIDClient extends TileEntityRedbus {

    private MovingSoundVoice voice1;
    private MovingSoundVoice voice2;
    private MovingSoundVoice voice3;

    // 6 octave scale centered on A4, so 1 means Ab1 and 75 means A#7
    // must be mapped to a float value from 0.5 to 2.0 to be usable with paulscode
    // since paulscode only covers 2 octaves through pitch manipulation three recordings exist for each waveform
    private int voice1Note = 0;
    private int voice2Note = 0;
    private int voice3Note = 0;

    // Notes that were in use before this SID was updated
    private int oldVoice1Note = 0;
    private int oldVoice2Note = 0;
    private int oldVoice3Note = 0;

    private int voice1Waveform = 3;
    private int voice2Waveform = 3;
    private int voice3Waveform = 3;

    // ranges from 0 as silent to 255 as loudest
    // must be mapped to a float value from 0.0 to 1.0 to be usable with paulscode
    private int volume = 0;

    public TileEntitySIDClient() {}

    @Override
    public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
        NBTTagCompound data = packet.getNbtCompound();
        oldVoice1Note = voice1Note;
        voice1Note = data.getInteger("voice1Note");
        oldVoice2Note = voice2Note;
        voice2Note = data.getInteger("voice2Note");
        oldVoice3Note = voice3Note;
        voice3Note = data.getInteger("voice3Note");
        voice1Waveform = data.getInteger("voice1Waveform");
        voice2Waveform = data.getInteger("voice2Waveform");
        voice3Waveform = data.getInteger("voice3Waveform");
        volume = data.getInteger("volume");

        updateVoices();

        super.onDataPacket(networkManager, packet);
    }

    private void updateVoices() {
        if(volume == 0) {
            deleteAllVoices();
            return;
        }

        Waveform waveform1 = Waveform.getWaveform(voice1Waveform);
        if(voice1Note != 0) {
            if(voice1 == null) {
                voice1 = getNewVoice(waveform1, voice1Note, volume);
            } else if(voice1.getWaveform() != waveform1
                    || (oldVoice1Note < 26 && voice1Note >= 26)
                    || (oldVoice1Note < 51 && voice1Note >= 51)
                    || (voice1Note < 26 && oldVoice1Note >= 26)
                    || (voice1Note < 51 && oldVoice1Note >= 51)) {
                voice1.delete();
                voice1 = getNewVoice(waveform1, voice1Note, volume);
            } else {
                changeVolume(voice1, volume);
                changeNote(voice1, voice1Note);
            }
        } else if(voice1 != null) {
            voice1.delete();
            voice1 = null;
        }

        Waveform waveform2 = Waveform.getWaveform(voice2Waveform);
        if(voice2Note != 0) {
            if(voice2 == null) {
                voice2 = getNewVoice(waveform2, voice2Note, volume);
            } else if(voice2.getWaveform() != waveform2
                    || (oldVoice2Note < 26 && voice2Note >= 26)
                    || (oldVoice2Note < 51 && voice2Note >= 51)
                    || (voice2Note < 26 && oldVoice2Note >= 26)
                    || (voice2Note < 51 && oldVoice2Note >= 51)) {
                voice2.delete();
                voice2 = getNewVoice(waveform2, voice2Note, volume);
            } else {
                changeVolume(voice2, volume);
                changeNote(voice2, voice2Note);
            }
        } else if(voice2 != null) {
            voice2.delete();
            voice2 = null;
        }

        Waveform waveform3 = Waveform.getWaveform(voice3Waveform);
        if(voice3Note != 0) {
            if(voice3 == null) {
                voice3 = getNewVoice(waveform3, voice3Note, volume);
            } else if(voice3.getWaveform() != waveform3
                    || (oldVoice3Note < 26 && voice3Note >= 26)
                    || (oldVoice3Note < 51 && voice3Note >= 51)
                    || (voice3Note < 26 && oldVoice3Note >= 26)
                    || (voice3Note < 51 && oldVoice3Note >= 51)) {
                voice3.delete();
                voice3 = getNewVoice(waveform3, voice3Note, volume);
            } else {
                changeVolume(voice3, volume);
                changeNote(voice3, voice3Note);
            }
        } else if(voice3 != null) {
            voice3.delete();
            voice3 = null;
        }
    }

    private void deleteAllVoices() {
        if(voice1 != null) {
            voice1.delete();
            voice1 = null;
        }
        if(voice2 != null) {
            voice2.delete();
            voice2 = null;
        }
        if(voice3 != null) {
            voice3.delete();
            voice3 = null;
        }
    }

    private MovingSoundVoice getNewVoice(Waveform waveform, int note, int volume) {
        ResourceLocation soundName;
        if(note == 0) {
            return null;
        } else if(note < 26) {
            soundName = waveform.getSoundLow();
        } else if(note < 51) {
            soundName = waveform.getSoundMed();
        } else {
            soundName = waveform.getSoundHigh();
        }

        MovingSoundVoice newVoice = new MovingSoundVoice(this, waveform, soundName);
        changeNote(newVoice, note);
        changeVolume(newVoice, volume);

        Minecraft.getMinecraft().getSoundHandler().playSound(newVoice);
        return newVoice;
    }

    private void changeNote(MovingSoundVoice voice, int newNote) {
        if(newNote > 50) {
            newNote -= 50;
        } else if(newNote > 25) {
            newNote -= 25;
        } else if(newNote < 1) {
            newNote = 1;
        }

        changePitch(voice, newNote);
    }

    private void changePitch(MovingSoundVoice voice, int newPitch) {
        voice.setPitch((newPitch * 0.06F) + 0.5F);
    }

    private void changeVolume(MovingSoundVoice voice, int volume) {
        voice.setVolume(volume / 510.0F);
    }

}
