package com.pvparena.listeners;

import com.pvparena.PVPArenaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final PVPArenaPlugin plugin;

    public PlayerQuitListener(PVPArenaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Remove from queue if in queue
        plugin.getQueueManager().removeFromQueue(player);
        
        // Handle match if in match
        if (plugin.getMatchManager().isInMatch(player)) {
            plugin.getMatchManager().handlePlayerQuit(player);
        }
    }
}
