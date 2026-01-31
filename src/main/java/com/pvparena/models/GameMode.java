package com.pvparena.models;

import org.bukkit.Material;

public class GameMode {
    
    private final String key;
    private final String displayName;
    private final Material icon;
    private final int slot;
    private final String kitName;

    public GameMode(String key, String displayName, Material icon, int slot, String kitName) {
        this.key = key;
        this.displayName = displayName;
        this.icon = icon;
        this.slot = slot;
        this.kitName = kitName;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public int getSlot() {
        return slot;
    }

    public String getKitName() {
        return kitName;
    }
}
