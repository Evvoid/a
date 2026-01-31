package com.pvparena.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Kit {
    
    private final String name;
    private Material helmet;
    private Material chestplate;
    private Material leggings;
    private Material boots;
    private final List<ItemStack> items;

    public Kit(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Material getHelmet() {
        return helmet;
    }

    public void setHelmet(Material helmet) {
        this.helmet = helmet;
    }

    public Material getChestplate() {
        return chestplate;
    }

    public void setChestplate(Material chestplate) {
        this.chestplate = chestplate;
    }

    public Material getLeggings() {
        return leggings;
    }

    public void setLeggings(Material leggings) {
        this.leggings = leggings;
    }

    public Material getBoots() {
        return boots;
    }

    public void setBoots(Material boots) {
        this.boots = boots;
    }

    public List<ItemStack> getItems() {
        return new ArrayList<>(items);
    }

    public void addItem(ItemStack item) {
        this.items.add(item);
    }
}
