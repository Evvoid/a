package com.pvparena.listeners;

import com.pvparena.PVPArenaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final PVPArenaPlugin plugin;

    public ChatListener(PVPArenaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String senderWorld = sender.getWorld().getName();
        
        // Remove players from different worlds
        event.getRecipients().removeIf(recipient -> 
            !recipient.getWorld().getName().equals(senderWorld));
    }
}
