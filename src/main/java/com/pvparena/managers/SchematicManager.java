package com.pvparena.managers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 * Manages loading and pasting WorldEdit schematics for arenas
 */
public class SchematicManager {

    private final Plugin plugin;
    private final File schematicsFolder;
    private final List<File> arenaSchematicFiles;
    private final Random random;

    public SchematicManager(Plugin plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "arenas");
        this.arenaSchematicFiles = new ArrayList<>();
        this.random = new Random();

        // Create arenas folder if it doesn't exist
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
            plugin.getLogger().info("Created arenas folder at: " + schematicsFolder.getAbsolutePath());
        }

        loadSchematicFiles();
    }

    /**
     * Load all schematic files from the arenas folder
     */
    private void loadSchematicFiles() {
        arenaSchematicFiles.clear();

        File[] files = schematicsFolder.listFiles((dir, name) ->
                name.endsWith(".schem") || name.endsWith(".schematic"));

        if (files != null && files.length > 0) {
            for (File file : files) {
                arenaSchematicFiles.add(file);
                plugin.getLogger().info("Found arena schematic: " + file.getName());
            }
        } else {
            plugin.getLogger().warning("No schematic files found in " + schematicsFolder.getAbsolutePath());
            plugin.getLogger().warning("Please add .schem or .schematic files to this folder");
        }
    }

    /**
     * Reload schematic files from disk
     */
    public void reload() {
        loadSchematicFiles();
        plugin.getLogger().info("Reloaded " + arenaSchematicFiles.size() + " arena schematics");
    }

    /**
     * Get a random schematic file
     * @return Random schematic file, or null if none available
     */
    public File getRandomSchematic() {
        if (arenaSchematicFiles.isEmpty()) {
            return null;
        }
        return arenaSchematicFiles.get(random.nextInt(arenaSchematicFiles.size()));
    }

    /**
     * Get a specific schematic file by name
     * @param name Name of the schematic (without extension)
     * @return Schematic file, or null if not found
     */
    public File getSchematic(String name) {
        for (File file : arenaSchematicFiles) {
            String fileName = file.getName();
            if (fileName.equals(name + ".schem") || fileName.equals(name + ".schematic") ||
                    fileName.equals(name)) {
                return file;
            }
        }
        return null;
    }

    /**
     * Paste a schematic at the specified location
     * @param schematicFile The schematic file to paste
     * @param location The location to paste at (will be the center bottom of the schematic)
     * @return The dimensions of the pasted schematic (width, height, length), or null on failure
     */
    public SchematicDimensions pasteSchematic(File schematicFile, Location location) {
        if (schematicFile == null || !schematicFile.exists()) {
            plugin.getLogger().warning("Schematic file does not exist: " + schematicFile);
            return null;
        }

        World world = location.getWorld();
        if (world == null) {
            plugin.getLogger().warning("World is null for location: " + location);
            return null;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            plugin.getLogger().warning("Unknown schematic format: " + schematicFile.getName());
            return null;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            Clipboard clipboard = reader.read();

            // Get schematic dimensions
            BlockVector3 dimensions = clipboard.getDimensions();
            int width = dimensions.getX();
            int height = dimensions.getY();
            int length = dimensions.getZ();

            // Calculate paste position (center the schematic on the X and Z axes)
            BlockVector3 clipboardOrigin = clipboard.getOrigin();
            int pasteX = location.getBlockX() - (width / 2);
            int pasteY = location.getBlockY();
            int pasteZ = location.getBlockZ() - (length / 2);

            // Create WorldEdit world and paste location
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
            BlockVector3 pasteLocation = BlockVector3.at(pasteX, pasteY, pasteZ);

            // Paste the schematic
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(pasteLocation)
                        .ignoreAirBlocks(false)
                        .build();

                Operations.complete(operation);

                plugin.getLogger().info("Pasted schematic '" + schematicFile.getName() +
                        "' at " + pasteX + ", " + pasteY + ", " + pasteZ +
                        " (size: " + width + "x" + height + "x" + length + ")");

                return new SchematicDimensions(width, height, length, pasteLocation);
            }

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to read schematic file: " + schematicFile.getName(), e);
            return null;
        } catch (WorldEditException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to paste schematic: " + schematicFile.getName(), e);
            return null;
        }
    }

    /**
     * Paste a random schematic at the specified location
     * @param location The location to paste at
     * @return The dimensions of the pasted schematic, or null on failure
     */
    public SchematicDimensions pasteRandomSchematic(Location location) {
        File schematic = getRandomSchematic();
        if (schematic == null) {
            plugin.getLogger().warning("No schematics available to paste");
            return null;
        }
        return pasteSchematic(schematic, location);
    }

    /**
     * Check if WorldEdit is available
     * @return true if WorldEdit is loaded
     */
    public boolean isWorldEditAvailable() {
        return plugin.getServer().getPluginManager().getPlugin("WorldEdit") != null;
    }

    /**
     * Get the number of available schematics
     * @return Number of loaded schematic files
     */
    public int getSchematicCount() {
        return arenaSchematicFiles.size();
    }

    /**
     * Get list of all schematic names
     * @return List of schematic file names
     */
    public List<String> getSchematicNames() {
        List<String> names = new ArrayList<>();
        for (File file : arenaSchematicFiles) {
            names.add(file.getName());
        }
        return names;
    }

    /**
     * Data class to hold schematic dimensions and paste location
     */
    public static class SchematicDimensions {
        private final int width;
        private final int height;
        private final int length;
        private final BlockVector3 pasteLocation;

        public SchematicDimensions(int width, int height, int length, BlockVector3 pasteLocation) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.pasteLocation = pasteLocation;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getLength() {
            return length;
        }

        public BlockVector3 getPasteLocation() {
            return pasteLocation;
        }

        /**
         * Get the center location of the schematic (useful for spawn points)
         * @return Center block vector
         */
        public BlockVector3 getCenter() {
            return pasteLocation.add(width / 2, 0, length / 2);
        }
    }
}