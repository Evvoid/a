package com.pvparena.managers;

import com.pvparena.PVPArenaPlugin;
import com.pvparena.models.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaManager {

    private final PVPArenaPlugin plugin;
    private final Set<ArenaCoordinate> usedCoordinates;
    private final Map<ArenaCoordinate, Long> cooldownCoordinates;
    private final Map<UUID, Arena> activeArenas;
    private int nextArenaId = 0;

    public ArenaManager(PVPArenaPlugin plugin) {
        this.plugin = plugin;
        this.usedCoordinates = ConcurrentHashMap.newKeySet();
        this.cooldownCoordinates = new ConcurrentHashMap<>();
        this.activeArenas = new ConcurrentHashMap<>();
        
        // Start cleanup task
        startCleanupTask();
    }

    public Arena createArena(String gameMode) {
        World pvpWorld = Bukkit.getWorld(plugin.getConfigManager().getPvpWorldName());
        if (pvpWorld == null) {
            plugin.getLogger().severe("PVP world not found!");
            return null;
        }
        
        ArenaCoordinate coordinate = findAvailableCoordinate();
        if (coordinate == null) {
            plugin.getLogger().warning("No available coordinates for arena!");
            return null;
        }
        
        usedCoordinates.add(coordinate);
        
        UUID arenaId = UUID.randomUUID();
        Location center = new Location(pvpWorld, coordinate.x, plugin.getConfigManager().getFloorY(), coordinate.z);
        Arena arena = new Arena(arenaId, center, gameMode);
        
        activeArenas.put(arenaId, arena);
        
        // Build arena structure
        buildArena(center);
        
        return arena;
    }

    private ArenaCoordinate findAvailableCoordinate() {
        // Clean up expired cooldowns
        long currentTime = System.currentTimeMillis();
        cooldownCoordinates.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > plugin.getConfigManager().getArenaCleanupDelay() * 1000L);
        
        int spacing = plugin.getConfigManager().getArenaSpacing();
        Random random = new Random();
        
        // Try to find available coordinate
        for (int i = 0; i < 100; i++) {
            int x = (random.nextInt(20) - 10) * spacing;
            int z = (random.nextInt(20) - 10) * spacing;
            
            ArenaCoordinate candidate = new ArenaCoordinate(x, z);
            
            if (!usedCoordinates.contains(candidate) && !cooldownCoordinates.containsKey(candidate)) {
                return candidate;
            }
        }
        
        return null;
    }

    private void buildArena(Location center) {
        int size = plugin.getConfigManager().getArenaSize();
        int halfSize = size / 2;
        int floorY = plugin.getConfigManager().getFloorY();
        Material floorMaterial = plugin.getConfigManager().getFloorMaterial();
        Material barrierMaterial = plugin.getConfigManager().getBarrierMaterial();
        int barrierHeight = plugin.getConfigManager().getBarrierHeight();
        
        World world = center.getWorld();
        if (world == null) return;
        
        // Build floor
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                Location loc = center.clone().add(x, 0, z);
                loc.setY(floorY);
                world.getBlockAt(loc).setType(floorMaterial);
            }
        }
        
        // Build barriers
        for (int y = 0; y < barrierHeight; y++) {
            // North and South walls
            for (int x = -halfSize; x <= halfSize; x++) {
                world.getBlockAt(center.clone().add(x, floorY + y + 1, -halfSize)).setType(barrierMaterial);
                world.getBlockAt(center.clone().add(x, floorY + y + 1, halfSize)).setType(barrierMaterial);
            }
            
            // East and West walls
            for (int z = -halfSize; z <= halfSize; z++) {
                world.getBlockAt(center.clone().add(-halfSize, floorY + y + 1, z)).setType(barrierMaterial);
                world.getBlockAt(center.clone().add(halfSize, floorY + y + 1, z)).setType(barrierMaterial);
            }
        }
    }

    public void deleteArena(Arena arena) {
        if (arena == null) return;
        
        // Remove blocks
        clearArena(arena.getCenter());
        
        // Mark coordinate as in cooldown
        Location center = arena.getCenter();
        ArenaCoordinate coordinate = new ArenaCoordinate((int) center.getX(), (int) center.getZ());
        usedCoordinates.remove(coordinate);
        cooldownCoordinates.put(coordinate, System.currentTimeMillis());
        
        // Remove from active arenas
        activeArenas.remove(arena.getArenaId());
    }

    private void clearArena(Location center) {
        int size = plugin.getConfigManager().getArenaSize();
        int halfSize = size / 2;
        int floorY = plugin.getConfigManager().getFloorY();
        int barrierHeight = plugin.getConfigManager().getBarrierHeight();
        
        World world = center.getWorld();
        if (world == null) return;
        
        // Clear floor
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                Location loc = center.clone().add(x, 0, z);
                loc.setY(floorY);
                world.getBlockAt(loc).setType(Material.AIR);
            }
        }
        
        // Clear barriers
        for (int y = 0; y < barrierHeight; y++) {
            for (int x = -halfSize; x <= halfSize; x++) {
                world.getBlockAt(center.clone().add(x, floorY + y + 1, -halfSize)).setType(Material.AIR);
                world.getBlockAt(center.clone().add(x, floorY + y + 1, halfSize)).setType(Material.AIR);
            }
            
            for (int z = -halfSize; z <= halfSize; z++) {
                world.getBlockAt(center.clone().add(-halfSize, floorY + y + 1, z)).setType(Material.AIR);
                world.getBlockAt(center.clone().add(halfSize, floorY + y + 1, z)).setType(Material.AIR);
            }
        }
    }

    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            int cleanupDelay = plugin.getConfigManager().getArenaCleanupDelay() * 1000;
            
            cooldownCoordinates.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > cleanupDelay);
        }, 20L * 10, 20L * 10); // Run every 10 seconds
    }

    public Arena getArena(UUID arenaId) {
        return activeArenas.get(arenaId);
    }

    public int getAvailableCoordinates() {
        return 400 - usedCoordinates.size() - cooldownCoordinates.size();
    }

    public void cleanup() {
        // Delete all active arenas
        for (Arena arena : new ArrayList<>(activeArenas.values())) {
            deleteArena(arena);
        }
        
        usedCoordinates.clear();
        cooldownCoordinates.clear();
        activeArenas.clear();
    }

    private static class ArenaCoordinate {
        final int x;
        final int z;

        ArenaCoordinate(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ArenaCoordinate that = (ArenaCoordinate) o;
            return x == that.x && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }
}
