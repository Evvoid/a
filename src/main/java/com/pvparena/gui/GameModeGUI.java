package com.pvparena.gui;

import com.pvparena.PVPArenaPlugin;
import com.pvparena.models.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameModeGUI {

    private final PVPArenaPlugin plugin;

    public GameModeGUI(PVPArenaPlugin plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§c§lSelect Game Mode");
        
        Map<String, GameMode> gameModes = plugin.getConfigManager().getGameModes();
        
        for (GameMode mode : gameModes.values()) {
            ItemStack item = new ItemStack(mode.getIcon());
            ItemMeta meta = item.getItemMeta();
            
            meta.setDisplayName(mode.getDisplayName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7");
            lore.add("§7Click to join queue");
            int queueSize = plugin.getQueueManager().getQueueSize(mode.getKey());
            lore.add("§7In queue: §e" + queueSize);
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(mode.getSlot(), item);
        }
        
        // Add leave queue button if in queue
        if (plugin.getQueueManager().isInQueue(player)) {
            ItemStack leaveItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = leaveItem.getItemMeta();
            meta.setDisplayName("§c§lLeave Queue");
            
            List<String> lore = new ArrayList<>();
            lore.add("§7");
            lore.add("§7Click to leave the queue");
            meta.setLore(lore);
            
            leaveItem.setItemMeta(meta);
            inv.setItem(26, leaveItem);
        }
        
        player.openInventory(inv);
    }

    public void handleClick(Player player, ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        // Check for leave queue button
        if (clickedItem.getType() == Material.BARRIER) {
            if (plugin.getQueueManager().removeFromQueue(player)) {
                player.sendMessage(plugin.getConfigManager().getMessage("queue-left"));
            }
            player.closeInventory();
            return;
        }
        
        // Find matching game mode
        Map<String, GameMode> gameModes = plugin.getConfigManager().getGameModes();
        for (GameMode mode : gameModes.values()) {
            if (mode.getIcon() == clickedItem.getType() && 
                clickedItem.hasItemMeta() && 
                clickedItem.getItemMeta().getDisplayName().equals(mode.getDisplayName())) {
                
                // Try to join queue
                if (plugin.getMatchManager().isInMatch(player)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("already-in-match"));
                    player.closeInventory();
                    return;
                }
                
                if (plugin.getQueueManager().isInQueue(player)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("already-in-queue"));
                    player.closeInventory();
                    return;
                }
                
                if (plugin.getQueueManager().addToQueue(player, mode.getKey())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("queue-joined", 
                        "gamemode", mode.getDisplayName()));
                }
                
                player.closeInventory();
                return;
            }
        }
    }
}
