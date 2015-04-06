package com.github.blackjak34.compute.enums;

import net.minecraft.util.ResourceLocation;

/**
 * @author Blackjak34
 * @since 1.0.0
 */
public enum Waveform {
    NOISE    (new ResourceLocation("doesnotcompute:sid.noise_low"),
            new ResourceLocation("doesnotcompute:sid.noise_med"),
            new ResourceLocation("doesnotcompute:sid.noise_high")),
    SAW      (new ResourceLocation("doesnotcompute:sid.saw_low"),
            new ResourceLocation("doesnotcompute:sid.saw_med"),
            new ResourceLocation("doesnotcompute:sid.saw_high")),
    SQUARE   (new ResourceLocation("doesnotcompute:sid.square_low"),
            new ResourceLocation("doesnotcompute:sid.square_med"),
            new ResourceLocation("doesnotcompute:sid.square_high")),
    TRIANGLE (new ResourceLocation("doesnotcompute:sid.triangle_low"),
            new ResourceLocation("doesnotcompute:sid.triangle_med"),
            new ResourceLocation("doesnotcompute:sid.triangle_high"));

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
        return TRIANGLE;
    }

}
