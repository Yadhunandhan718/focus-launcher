package com.example.focuslauncher;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Persistent launcher configuration: approved apps and independently arranged pages. */
final class Prefs {
    private static final String FILE = "launcher_prefs";
    private static final String KEY_GRAYSCALE = "grayscale";
    private static final String KEY_SETUP = "setup_complete";
    private static final String KEY_ALLOWED = "allowed_apps";
    private static final String KEY_PAGE_COUNT = "page_count";
    private static final String KEY_PAGE_NAME = "page_name_";
    private static final String KEY_SLOT = "page_";
    private final SharedPreferences sp;

    Prefs(Context context) { sp = context.getSharedPreferences(FILE, Context.MODE_PRIVATE); }

    boolean isSetupComplete() { return sp.getBoolean(KEY_SETUP, false); }
    Set<String> getAllowedApps() { return new HashSet<>(sp.getStringSet(KEY_ALLOWED, new HashSet<>())); }
    boolean isAllowed(String pkg) { return getAllowedApps().contains(pkg); }
    void completeSetup(Set<String> packages) {
        SharedPreferences.Editor e = sp.edit().putStringSet(KEY_ALLOWED, new HashSet<>(packages)).putBoolean(KEY_SETUP, true);
        e.putInt(KEY_PAGE_COUNT, 1).putString(KEY_PAGE_NAME + 0, "Home");
        for (int i = 0; i < 6; i++) {
            String key = slotKey(0, i);
            if (i < packages.size()) e.putString(key, new ArrayList<>(packages).get(i)); else e.remove(key);
        }
        e.apply();
    }
    void setAllowedApps(Set<String> packages) { sp.edit().putStringSet(KEY_ALLOWED, new HashSet<>(packages)).apply(); }

    int getPageCount() { return Math.max(1, sp.getInt(KEY_PAGE_COUNT, 1)); }
    String getPageName(int page) { return sp.getString(KEY_PAGE_NAME + page, page == 0 ? "Home" : "Page " + (page + 1)); }
    void setPageName(int page, String name) { sp.edit().putString(KEY_PAGE_NAME + page, name).apply(); }
    void addPage(String name) { int count = getPageCount(); sp.edit().putInt(KEY_PAGE_COUNT, count + 1).putString(KEY_PAGE_NAME + count, name).apply(); }
    void deletePage(int page) {
        int count = getPageCount(); if (count <= 1 || page < 0 || page >= count) return;
        SharedPreferences.Editor e = sp.edit();
        for (int p = page; p < count - 1; p++) {
            e.putString(KEY_PAGE_NAME + p, getPageName(p + 1));
            for (int s = 0; s < 6; s++) { String value = sp.getString(slotKey(p + 1, s), null); if (value == null) e.remove(slotKey(p, s)); else e.putString(slotKey(p, s), value); }
        }
        e.remove(KEY_PAGE_NAME + (count - 1)); for (int s = 0; s < 6; s++) e.remove(slotKey(count - 1, s));
        e.putInt(KEY_PAGE_COUNT, count - 1).apply();
    }
    String getSlot(int page, int slot) { return sp.getString(slotKey(page, slot), null); }
    void setSlot(int page, int slot, String packageName) { SharedPreferences.Editor e = sp.edit(); if (packageName == null) e.remove(slotKey(page, slot)); else e.putString(slotKey(page, slot), packageName); e.apply(); }
    private String slotKey(int page, int slot) { return KEY_SLOT + page + "_slot_" + slot; }

    boolean isGrayscale() { return sp.getBoolean(KEY_GRAYSCALE, false); }
    void setGrayscale(boolean on) { sp.edit().putBoolean(KEY_GRAYSCALE, on).apply(); }
}
