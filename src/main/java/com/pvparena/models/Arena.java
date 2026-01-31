package com.pvparena.models;

import org.bukkit.Location;

import java.util.UUID;

public class Arena {
    
    private final UUID arenaId;
    private final Location center;
    private final String gameMode;
    private final long createdTime;

    public Arena(UUID arenaId, Location center, String gameMode) {
        this.arenaId = arenaId;
        this.center = center;
        this.gameMode = gameMode;
        this.createdTime = System.currentTimeMillis();
    }

    public UUID getArenaId() {
        return arenaId;
    }

    public Location getCenter() {
        return center.clone();
    }

    public String getGameMode() {
        return gameMode;
    }

    public long getCreatedTime() {
        return createdTime;
    }
}
