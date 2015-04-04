package com.github.blackjak34.compute.enums;

import net.minecraft.util.ResourceLocation;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public enum Waveform {
    NOISE    (new ResourceLocation("doesnotcompute:sid.a2_noise"),
            new ResourceLocation("doesnotcompute:sid.a4_noise"),
            new ResourceLocation("doesnotcompute:sid.a6_noise")),
    SAW      (new ResourceLocation("doesnotcompute:sid.a2_saw"),
            new ResourceLocation("doesnotcompute:sid.a4_saw"),
            new ResourceLocation("doesnotcompute:sid.a6_saw")),
    SQUARE   (new ResourceLocation("doesnotcompute:sid.a2_square"),
            new ResourceLocation("doesnotcompute:sid.a4_square"),
            new ResourceLocation("doesnotcompute:sid.a6_square")),
    TRIANGLE (new ResourceLocation("doesnotcompute:sid.a2_triangle"),
            new ResourceLocation("doesnotcompute:sid.a4_triangle"),
            new ResourceLocation("doesnotcompute:sid.a6_triangle")),
    NONE     (null, null, null);

    private final ResourceLocation soundLow;
    private final ResourceLocation soundMed;
    private final ResourceLocation soundHigh;

    private Waveform(ResourceLocation soundLow, ResourceLocation soundMed, ResourceLocation soundHigh) {
        this.soundLow = soundLow;
        this.soundMed = soundMed;
        this.soundHigh = soundHigh;
    }

    public ResourceLocation getSoundLow() {
        return soundLow;
    }

    public ResourceLocation getSoundMed() {
        return soundMed;
    }

    public ResourceLocation getSoundHigh() {
        return soundHigh;
    }

    public static Waveform getWaveform(int ordinal) {
        for(Waveform waveform : values()) {
            if(waveform.ordinal() == ordinal) {return waveform;}
        }
        return NONE;
    }

}
