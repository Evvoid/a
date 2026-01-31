package com.pvparena.listeners;

import com.pvparena.PVPArenaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final PVPArenaPlugin plugin;

    public PlayerDeathListener(PVPArenaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Check if player is in a match
        if (plugin.getMatchManager().isInMatch(player)) {
            event.setKeepInventory(false);
            event.setKeepLevel(false);
            event.getDrops().clear();
            event.setDroppedExp(0);
            
            // Handle match end
            plugin.getMatchManager().handlePlayerDeath(player);
        }
    }
}
