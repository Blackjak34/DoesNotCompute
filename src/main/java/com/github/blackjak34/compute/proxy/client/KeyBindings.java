package com.github.blackjak34.compute.proxy.client;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class KeyBindings {

    public static final KeyBinding punchFeed =
            new KeyBinding("key.punch.feed", Keyboard.KEY_LBRACKET, "key.categories.doesnotcompute");
    public static final KeyBinding punchRegister =
            new KeyBinding("key.punch.reg", Keyboard.KEY_RBRACKET, "key.categories.doesnotcompute");
    public static final KeyBinding punchRelease =
            new KeyBinding("key.punch.rel", Keyboard.KEY_BACK, "key.categories.doesnotcompute");
    public static final KeyBinding punchLZero =
            new KeyBinding("key.punch.lzero", Keyboard.KEY_RETURN, "key.categories.doesnotcompute");
    public static final KeyBinding punchMPunch =
            new KeyBinding("key.punch.mpunch", Keyboard.KEY_GRAVE, "key.categories.doesnotcompute");
    public static final KeyBinding punchDup =
            new KeyBinding("key.punch.dup", Keyboard.KEY_TAB, "key.categories.doesnotcompute");
    public static final KeyBinding punchAuxDup =
            new KeyBinding("key.punch.auxdup", Keyboard.KEY_LMENU, "key.categories.doesnotcompute");
    public static final KeyBinding punchSkip =
            new KeyBinding("key.punch.skip", Keyboard.KEY_RSHIFT, "key.categories.doesnotcompute");
    public static final KeyBinding punchProgOne =
            new KeyBinding("key.punch.progone", Keyboard.KEY_INSERT, "key.categories.doesnotcompute");
    public static final KeyBinding punchProgTwo =
            new KeyBinding("key.punch.progtwo", Keyboard.KEY_DELETE, "key.categories.doesnotcompute");
    public static final KeyBinding punchAlpha =
            new KeyBinding("key.punch.alpha", Keyboard.KEY_RCONTROL, "key.categories.doesnotcompute");
    public static final KeyBinding punchNumeric =
            new KeyBinding("key.punch.numeric", Keyboard.KEY_LCONTROL, "key.categories.doesnotcompute");
    public static final KeyBinding punchMaster =
            new KeyBinding("key.punch.master", Keyboard.KEY_RMENU, "key.categories.doesnotcompute");
    public static final KeyBinding punchCent =
            new KeyBinding("key.punch.cent", Keyboard.KEY_6, "key.categories.doesnotcompute");
    public static final KeyBinding punchPrime =
            new KeyBinding("key.punch.prime", Keyboard.KEY_BACKSLASH, "key.categories.doesnotcompute");

}
