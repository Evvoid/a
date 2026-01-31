package com.pvparena.managers;

import com.pvparena.PVPArenaPlugin;
import com.pvparena.models.Arena;
import com.pvparena.models.Kit;
import com.pvparena.models.Match;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MatchManager {

    private final PVPArenaPlugin plugin;
    private final Map<UUID, Match> activeMatches;
    private final Map<UUID, UUID> playerMatches;

    public MatchManager(PVPArenaPlugin plugin) {
        this.plugin = plugin;
        this.activeMatches = new ConcurrentHashMap<>();
        this.playerMatches = new ConcurrentHashMap<>();
    }

    public void createMatch(Player player1, Player player2, String gameMode) {
        // Create arena
        Arena arena = plugin.getArenaManager().createArena(gameMode);
        if (arena == null) {
            player1.sendMessage("§cFailed to create arena!");
            player2.sendMessage("§cFailed to create arena!");
            return;
        }
        
        // Create match
        Match match = new Match(UUID.randomUUID(), player1, player2, arena, gameMode);
        activeMatches.put(match.getMatchId(), match);
        playerMatches.put(player1.getUniqueId(), match.getMatchId());
        playerMatches.put(player2.getUniqueId(), match.getMatchId());
        
        // Notify players
        player1.sendMessage(plugin.getConfigManager().getMessage("match-found"));
        player2.sendMessage(plugin.getConfigManager().getMessage("match-found"));
        
        // Teleport players and start match
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            startMatch(match);
        }, 20L);
    }

    private void startMatch(Match match) {
        Player player1 = match.getPlayer1();
        Player player2 = match.getPlayer2();
        Arena arena = match.getArena();
        
        if (player1 == null || !player1.isOnline() || player2 == null || !player2.isOnline()) {
            endMatch(match, null);
            return;
        }
        
        // Get spawn points
        Location spawn1 = plugin.getConfigManager().getSpawnPoint1(arena.getCenter());
        Location spawn2 = plugin.getConfigManager().getSpawnPoint2(arena.getCenter());
        
        // Prepare players
        preparePlayer(player1);
        preparePlayer(player2);
        
        // Teleport players
        player1.teleport(spawn1);
        player2.teleport(spawn2);
        
        // Give kits
        giveKit(player1, match.getGameMode());
        giveKit(player2, match.getGameMode());
        
        // Start countdown
        startCountdown(match);
    }

    private void preparePlayer(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setFireTicks(0);
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.setGameMode(GameMode.SURVIVAL);
        player.getActivePotionEffects().forEach(effect -> 
            player.removePotionEffect(effect.getType()));
    }

    private void giveKit(Player player, String gameMode) {
        com.pvparena.models.GameMode mode = plugin.getConfigManager().getGameMode(gameMode);
        if (mode == null) return;
        
        Kit kit = plugin.getConfigManager().getKit(mode.getKitName());
        if (kit == null) return;
        
        // Give armor
        ItemStack[] armor = new ItemStack[4];
        armor[3] = kit.getHelmet() != null ? new ItemStack(kit.getHelmet()) : null;
        armor[2] = kit.getChestplate() != null ? new ItemStack(kit.getChestplate()) : null;
        armor[1] = kit.getLeggings() != null ? new ItemStack(kit.getLeggings()) : null;
        armor[0] = kit.getBoots() != null ? new ItemStack(kit.getBoots()) : null;
        player.getInventory().setArmorContents(armor);
        
        // Give items
        for (ItemStack item : kit.getItems()) {
            player.getInventory().addItem(item.clone());
        }
    }

    private void startCountdown(Match match) {
        int countdown = plugin.getConfigManager().getCountdownDuration();
        
        for (int i = countdown; i > 0; i--) {
            final int seconds = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player player1 = match.getPlayer1();
                Player player2 = match.getPlayer2();
                
                if (player1 != null && player1.isOnline()) {
                    player1.sendMessage(plugin.getConfigManager().getMessage("match-start", 
                        "seconds", String.valueOf(seconds)));
                }
                if (player2 != null && player2.isOnline()) {
                    player2.sendMessage(plugin.getConfigManager().getMessage("match-start", 
                        "seconds", String.valueOf(seconds)));
                }
            }, 20L * (countdown - i));
        }
        
        // Start match after countdown
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            match.setState(Match.MatchState.ACTIVE);
        }, 20L * countdown);
    }

    public void endMatch(Match match, Player winner) {
        if (match == null) return;

        match.setState(Match.MatchState.ENDED);

        Player player1 = match.getPlayer1();
        Player player2 = match.getPlayer2();
        Player loser = null;

        if (winner != null) {
            loser = player1 != null && player1.equals(winner) ? player2 : player1;
        }

        // Send messages
        if (winner != null && winner.isOnline()) {
            winner.sendMessage(plugin.getConfigManager().getMessage("match-won"));
        }
        if (loser != null && loser.isOnline()) {
            loser.sendMessage(plugin.getConfigManager().getMessage("match-lost"));
        }

        // Teleport players back
        Location spawn = plugin.getConfigManager().getMainWorldSpawn();
        if (player1 != null && player1.isOnline()) {
            player1.teleport(spawn);
            preparePlayer(player1);
        }
        if (player2 != null && player2.isOnline()) {
            player2.teleport(spawn);
            preparePlayer(player2);
        }

        // Set the loser to spectator mode and prevent respawning
        if (loser != null && loser.isOnline()) {
            loser.setGameMode(GameMode.SPECTATOR);
            loser.sendMessage(plugin.getConfigManager().getMessage("spectator-mode"));
        }

        // Remove from tracking
        if (player1 != null) {
            playerMatches.remove(player1.getUniqueId());
        }
        if (player2 != null) {
            playerMatches.remove(player2.getUniqueId());
        }
        activeMatches.remove(match.getMatchId());

        // Delete arena after delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getArenaManager().deleteArena(match.getArena());
        }, 20L * plugin.getConfigManager().getArenaCleanupDelay());
    }


    public Match getMatch(UUID playerId) {
        UUID matchId = playerMatches.get(playerId);
        return matchId != null ? activeMatches.get(matchId) : null;
    }

    public boolean isInMatch(Player player) {
        return playerMatches.containsKey(player.getUniqueId());
    }

    public int getActiveMatches() {
        return activeMatches.size();
    }

    public void handlePlayerDeath(Player player) {
        Match match = getMatch(player.getUniqueId());
        if (match == null || match.getState() != Match.MatchState.ACTIVE) {
            return;
        }
        
        Player winner = match.getPlayer1().equals(player) ? match.getPlayer2() : match.getPlayer1();
        endMatch(match, winner);
    }

    public void handlePlayerQuit(Player player) {
        Match match = getMatch(player.getUniqueId());
        if (match == null) {
            return;
        }
        
        Player winner = match.getPlayer1().equals(player) ? match.getPlayer2() : match.getPlayer1();
        endMatch(match, winner);
    }

    public void cleanup() {
        for (Match match : new ArrayList<>(activeMatches.values())) {
            endMatch(match, null);
        }
        activeMatches.clear();
        playerMatches.clear();
    }
}
