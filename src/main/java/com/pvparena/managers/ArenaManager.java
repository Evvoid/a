package com.pvparena.managers;

import com.pvparena.models.Arena;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * ArenaManager that uses WorldEdit schematics instead of block-by-block building
 * Compatible with existing Arena model
 */
public class ArenaManager {

    private final Plugin plugin;
    private final SchematicManager schematicManager;
    private final World pvpWorld;
    private final int arenaSpacing;
    private final Set<ArenaCoordinate> usedCoordinates;
    private final Map<ArenaCoordinate, Long> coordinateCooldowns;
    private final Map<Arena, ArenaCoordinate> arenaCoordinateMap; // Track arena coordinates by Arena object
    private final long cleanupDelay;

    public ArenaManager(Plugin plugin, SchematicManager schematicManager, World pvpWorld) {
        this.plugin = plugin;
        this.schematicManager = schematicManager;
        this.pvpWorld = pvpWorld;
        this.usedCoordinates = new HashSet<>();
        this.coordinateCooldowns = new HashMap<>();
        this.arenaCoordinateMap = new HashMap<>();

        // Load config values
        this.arenaSpacing = plugin.getConfig().getInt("arena-spacing", 1000);
        this.cleanupDelay = plugin.getConfig().getInt("arena-cleanup-delay", 30) * 1000L; // Convert to milliseconds

        plugin.getLogger().info("ArenaManager initialized with schematic support");
        plugin.getLogger().info("Arena spacing: " + arenaSpacing + " blocks");
    }

    /**
     * Create a new arena using a random schematic
     * @param gameMode The game mode for this arena
     * @return Arena object with spawn points, or null on failure
     */
    public Arena createArena(String gameMode) {
        return createArena(null, gameMode);
    }

    /**
     * Create a new arena using a specific schematic
     * @param schematicName Name of the schematic to use (null for random)
     * @param gameMode The game mode for this arena
     * @return Arena object with spawn points, or null on failure
     */
    public Arena createArena(String schematicName, String gameMode) {
        // Check if WorldEdit is available
        if (!schematicManager.isWorldEditAvailable()) {
            plugin.getLogger().severe("WorldEdit is not installed! Cannot create arenas.");
            return null;
        }

        // Check if schematics are available
        if (schematicManager.getSchematicCount() == 0) {
            plugin.getLogger().warning("No schematics available! Add .schem files to plugins/" +
                    plugin.getName() + "/arenas/");
            return null;
        }

        // Check if pvpWorld is available
        if (pvpWorld == null) {
            plugin.getLogger().warning("PVP world is not available! Cannot create arena.");
            return null;
        }

        // Find available coordinates
        ArenaCoordinate coordinates = findAvailableCoordinates();
        if (coordinates == null) {
            plugin.getLogger().warning("Could not find available coordinates for arena");
            return null;
        }

        // Get location to paste schematic
        Location pasteLocation = new Location(
                pvpWorld,
                coordinates.getX(),
                64, // Base Y level
                coordinates.getZ()
        );

        // Paste the schematic
        SchematicManager.SchematicDimensions dimensions;
        if (schematicName != null) {
            java.io.File schematic = schematicManager.getSchematic(schematicName);
            dimensions = schematicManager.pasteSchematic(schematic, pasteLocation);
        } else {
            dimensions = schematicManager.pasteRandomSchematic(pasteLocation);
        }

        if (dimensions == null) {
            plugin.getLogger().warning("Failed to paste schematic at coordinates: " + coordinates);
            return null;
        }

        // Mark coordinates as used
        usedCoordinates.add(coordinates);

        // Calculate spawn points based on schematic dimensions
        Location[] spawnPoints = calculateSpawnPoints(pasteLocation, dimensions);

        plugin.getLogger().info("Created arena at " + coordinates +
                " (dimensions: " + dimensions.getWidth() + "x" + dimensions.getHeight() + "x" + dimensions.getLength() + ")");

        // Create Arena using the actual constructor: Arena(UUID, Location, String)
        // We'll use the center location between the two spawn points
        Location centerLocation = spawnPoints[0].clone().add(
                (spawnPoints[1].getX() - spawnPoints[0].getX()) / 2,
                0,
                (spawnPoints[1].getZ() - spawnPoints[0].getZ()) / 2
        );

        UUID arenaId = UUID.randomUUID();
        String arenaGameMode = gameMode != null ? gameMode : "default";
        Arena arena = new Arena(arenaId, centerLocation, arenaGameMode);

        // Store the coordinates mapping for cleanup
        arenaCoordinateMap.put(arena, coordinates);

        return arena;
    }

    /**
     * Calculate spawn points based on schematic dimensions
     * Default: Places spawns at 1/4 and 3/4 of the arena width, centered on Z
     */
    private Location[] calculateSpawnPoints(Location baseLocation, SchematicManager.SchematicDimensions dimensions) {
        ConfigurationSection spawnConfig = plugin.getConfig().getConfigurationSection("arena.spawn-points");

        // Get center of the arena
        int centerX = baseLocation.getBlockX();
        int centerZ = baseLocation.getBlockZ();
        int baseY = baseLocation.getBlockY() + 1; // One block above the base

        Location spawn1, spawn2;

        if (spawnConfig != null && spawnConfig.contains("point1") && spawnConfig.contains("point2")) {
            // Use configured spawn points (relative to center)
            spawn1 = new Location(
                    pvpWorld,
                    centerX + spawnConfig.getInt("point1.x"),
                    baseY + spawnConfig.getInt("point1.y"),
                    centerZ + spawnConfig.getInt("point1.z")
            );

            spawn2 = new Location(
                    pvpWorld,
                    centerX + spawnConfig.getInt("point2.x"),
                    baseY + spawnConfig.getInt("point2.y"),
                    centerZ + spawnConfig.getInt("point2.z")
            );
        } else {
            // Auto-calculate spawn points based on schematic size
            int quarterWidth = dimensions.getWidth() / 4;

            spawn1 = new Location(
                    pvpWorld,
                    centerX - quarterWidth,
                    baseY + 2,
                    centerZ
            );

            spawn2 = new Location(
                    pvpWorld,
                    centerX + quarterWidth,
                    baseY + 2,
                    centerZ
            );
        }

        return new Location[]{spawn1, spawn2};
    }

    /**
     * Find available coordinates for a new arena
     */
    private ArenaCoordinate findAvailableCoordinates() {
        // Clean up expired cooldowns
        long currentTime = System.currentTimeMillis();
        coordinateCooldowns.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > cleanupDelay);

        // Try to find coordinates in a grid pattern
        int maxAttempts = 100;
        int gridSize = arenaSpacing;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int gridX = (attempt % 10) * gridSize;
            int gridZ = (attempt / 10) * gridSize;

            ArenaCoordinate candidate = new ArenaCoordinate(gridX, gridZ);

            if (!usedCoordinates.contains(candidate) &&
                    !coordinateCooldowns.containsKey(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Delete an arena (marks coordinates as available after cooldown)
     * @param arena The arena to delete
     */
    public void deleteArena(Arena arena) {
        if (arena == null) return;

        // Get coordinates from our mapping
        ArenaCoordinate coords = arenaCoordinateMap.get(arena);

        if (coords != null) {
            usedCoordinates.remove(coords);
            coordinateCooldowns.put(coords, System.currentTimeMillis());
            arenaCoordinateMap.remove(arena);

            plugin.getLogger().info("Arena at " + coords +
                    " marked for cleanup (cooldown: " + (cleanupDelay / 1000) + "s)");
        } else {
            plugin.getLogger().warning("Could not find coordinates for arena to delete");
        }
    }

    /**
     * Get the number of active arenas
     */
    public int getActiveArenaCount() {
        return usedCoordinates.size();
    }

    /**
     * Arena coordinate data class
     */
    private static class ArenaCoordinate {
        private final int x;
        private final int z;

        public ArenaCoordinate(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
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

        @Override
        public String toString() {
            return "(" + x + ", " + z + ")";
        }
    }
}