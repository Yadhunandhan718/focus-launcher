package com.example.focuslauncher;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Tiny wrapper around SharedPreferences.
 * Each home tile ("slot") stores the package name of the app it opens.
 * Slots are numbered left-to-right, top-to-bottom: 0, 1, 2, ...
 */
final class Prefs {
    private static final String FILE = "launcher_prefs";
    private static final String KEY_SLOT = "slot_";
    private static final String KEY_GRAYSCALE = "grayscale";
    private static final String KEY_SEEDED = "seeded";

    private final SharedPreferences sp;

    Prefs(Context context) {
        sp = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** Package name assigned to a tile, or null if the tile is empty. */
    String getSlot(int slot) {
        return sp.getString(KEY_SLOT + slot, null);
    }

    /** Assign an app to a tile. Pass null to empty the tile. */
    void setSlot(int slot, String packageName) {
        SharedPreferences.Editor e = sp.edit();
        if (packageName == null) {
            e.remove(KEY_SLOT + slot);
        } else {
            e.putString(KEY_SLOT + slot, packageName);
        }
        e.apply();
    }

    boolean isGrayscale() {
        return sp.getBoolean(KEY_GRAYSCALE, false);
    }

    void setGrayscale(boolean on) {
        sp.edit().putBoolean(KEY_GRAYSCALE, on).apply();
    }

    /** True once the tiles have been filled with default apps the first time. */
    boolean isSeeded() {
        return sp.getBoolean(KEY_SEEDED, false);
    }

    void markSeeded() {
        sp.edit().putBoolean(KEY_SEEDED, true).apply();
    }
}
