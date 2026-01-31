package com.pvparena.managers;

import com.pvparena.PVPArenaPlugin;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {

    private final PVPArenaPlugin plugin;
    private final Map<String, Queue<Player>> queues;
    private final Map<UUID, String> playerQueues;

    public QueueManager(PVPArenaPlugin plugin) {
        this.plugin = plugin;
        this.queues = new ConcurrentHashMap<>();
        this.playerQueues = new ConcurrentHashMap<>();
        
        // Initialize queues for each game mode
        for (String gameMode : plugin.getConfigManager().getGameModes().keySet()) {
            queues.put(gameMode, new LinkedList<>());
        }
    }

    public synchronized boolean addToQueue(Player player, String gameMode) {
        // Check if player is already in a queue
        if (playerQueues.containsKey(player.getUniqueId())) {
            return false;
        }
        
        // Check if player is already in a match
        if (plugin.getMatchManager().isInMatch(player)) {
            return false;
        }
        
        Queue<Player> queue = queues.get(gameMode);
        if (queue == null) {
            return false;
        }
        
        // Add player to queue
        queue.add(player);
        playerQueues.put(player.getUniqueId(), gameMode);
        
        // Check for match
        checkForMatch(gameMode);
        
        return true;
    }

    public synchronized boolean removeFromQueue(Player player) {
        String gameMode = playerQueues.remove(player.getUniqueId());
        if (gameMode == null) {
            return false;
        }
        
        Queue<Player> queue = queues.get(gameMode);
        if (queue != null) {
            queue.remove(player);
            return true;
        }
        
        return false;
    }

    private void checkForMatch(String gameMode) {
        Queue<Player> queue = queues.get(gameMode);
        if (queue == null || queue.size() < 2) {
            return;
        }
        
        // Get two players from queue
        Player player1 = queue.poll();
        Player player2 = queue.poll();
        
        if (player1 == null || player2 == null) {
            return;
        }
        
        // Remove from tracking
        playerQueues.remove(player1.getUniqueId());
        playerQueues.remove(player2.getUniqueId());
        
        // Create match
        plugin.getMatchManager().createMatch(player1, player2, gameMode);
    }

    public boolean isInQueue(Player player) {
        return playerQueues.containsKey(player.getUniqueId());
    }

    public String getQueueMode(Player player) {
        return playerQueues.get(player.getUniqueId());
    }

    public int getQueueSize(String gameMode) {
        Queue<Player> queue = queues.get(gameMode);
        return queue != null ? queue.size() : 0;
    }

    public int getTotalInQueue() {
        return playerQueues.size();
    }

    public void cleanup() {
        queues.clear();
        playerQueues.clear();
    }
}
