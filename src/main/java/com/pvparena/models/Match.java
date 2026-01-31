package com.pvparena.models;

import org.bukkit.entity.Player;

import java.util.UUID;

public class Match {
    
    public enum MatchState {
        WAITING,
        COUNTDOWN,
        ACTIVE,
        ENDED
    }
    
    private final UUID matchId;
    private final UUID player1Id;
    private final UUID player2Id;
    private final Arena arena;
    private final String gameMode;
    private MatchState state;
    private final long startTime;

    public Match(UUID matchId, Player player1, Player player2, Arena arena, String gameMode) {
        this.matchId = matchId;
        this.player1Id = player1.getUniqueId();
        this.player2Id = player2.getUniqueId();
        this.arena = arena;
        this.gameMode = gameMode;
        this.state = MatchState.COUNTDOWN;
        this.startTime = System.currentTimeMillis();
    }

    public UUID getMatchId() {
        return matchId;
    }

    public Player getPlayer1() {
        return org.bukkit.Bukkit.getPlayer(player1Id);
    }

    public Player getPlayer2() {
        return org.bukkit.Bukkit.getPlayer(player2Id);
    }

    public Arena getArena() {
        return arena;
    }

    public String getGameMode() {
        return gameMode;
    }

    public MatchState getState() {
        return state;
    }

    public void setState(MatchState state) {
        this.state = state;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean containsPlayer(UUID playerId) {
        return player1Id.equals(playerId) || player2Id.equals(playerId);
    }
}
