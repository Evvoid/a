package com.pvparena;

import com.pvparena.gui.GameModeGUI;
import com.pvparena.listeners.ChatListener;
import com.pvparena.listeners.CompassClickListener;
import com.pvparena.listeners.PlayerDeathListener;
import com.pvparena.listeners.PlayerQuitListener;
import com.pvparena.managers.ArenaManager;
import com.pvparena.managers.ConfigManager;
import com.pvparena.managers.MatchManager;
import com.pvparena.managers.QueueManager;
import com.pvparena.managers.SchematicManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class PVPArenaPlugin extends JavaPlugin {

    private static PVPArenaPlugin instance;
    private ConfigManager configManager;
    private SchematicManager schematicManager;
    private QueueManager queueManager;
    private ArenaManager arenaManager;
    private MatchManager matchManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Check if WorldEdit is available
        if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
            getLogger().severe("==================================================");
            getLogger().severe("WorldEdit is required for this plugin to work!");
            getLogger().severe("Please install WorldEdit and restart the server.");
            getLogger().severe("Download: https://dev.bukkit.org/projects/worldedit");
            getLogger().severe("==================================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize config manager first
        configManager = new ConfigManager(this);

        // Get PVP world
        World pvpWorld = Bukkit.getWorld(configManager.getPvpWorldName());
        if (pvpWorld == null) {
            getLogger().warning("PVP world '" + configManager.getPvpWorldName() + "' does not exist! Please create it.");
        }

        // Initialize managers
        schematicManager = new SchematicManager(this);
        arenaManager = new ArenaManager(this, schematicManager, pvpWorld);
        queueManager = new QueueManager(this);
        matchManager = new MatchManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new CompassClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        getLogger().info("PVP Arena Plugin has been enabled with WorldEdit schematic support!");
        getLogger().info("Loaded " + schematicManager.getSchematicCount() + " arena schematic(s)");

        // Warn if no schematics found
        if (schematicManager.getSchematicCount() == 0) {
            getLogger().warning("==================================================");
            getLogger().warning("No arena schematics found!");
            getLogger().warning("Please add .schem or .schematic files to:");
            getLogger().warning(getDataFolder().getAbsolutePath() + "/arenas/");
            getLogger().warning("==================================================");
        }
    }

    @Override
    public void onDisable() {
        // Cleanup active matches
        if (matchManager != null) {
            matchManager.cleanup();
        }

        getLogger().info("PVP Arena Plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("pvparena")) {
            if (!sender.hasPermission("pvparena.admin")) {
                sender.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage("§e=== PVP Arena Commands ===");
                sender.sendMessage("§e/pvparena reload §7- Reload configuration and schematics");
                sender.sendMessage("§e/pvparena debug §7- Show debug information");
                if (sender instanceof Player) {
                    sender.sendMessage("§e/pvparena give §7- Get arena compass");
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                configManager = new ConfigManager(this);
                schematicManager.reload();
                sender.sendMessage("§aConfiguration reloaded!");
                sender.sendMessage("§aReloaded " + schematicManager.getSchematicCount() + " schematic(s)");
                return true;
            }

            if (args[0].equalsIgnoreCase("debug")) {
                sender.sendMessage("§e=== PVP Arena Debug ===");
                sender.sendMessage("§7WorldEdit Available: §a" + schematicManager.isWorldEditAvailable());
                sender.sendMessage("§7Loaded Schematics: §a" + schematicManager.getSchematicCount());

                if (schematicManager.getSchematicCount() > 0) {
                    sender.sendMessage("§7Schematic Files:");
                    for (String name : schematicManager.getSchematicNames()) {
                        sender.sendMessage("  §7- §f" + name);
                    }
                }

                sender.sendMessage("§7Queued Players: §a" + queueManager.getTotalInQueue());
                sender.sendMessage("§7Active Matches: §a" + matchManager.getActiveMatches());
                sender.sendMessage("§7Active Arenas: §a" + arenaManager.getActiveArenaCount());
                return true;
            }

            if (args[0].equalsIgnoreCase("give")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.getInventory().addItem(getCompassItem());
                    player.sendMessage("§aYou received the arena compass!");
                } else {
                    sender.sendMessage("§cOnly players can receive items!");
                }
                return true;
            }
        }

        return false;
    }

    public ItemStack getCompassItem() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        String name = configManager.getCompassName();
        meta.setDisplayName(name);

        meta.setLore(configManager.getCompassLore());
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        compass.setItemMeta(meta);
        return compass;
    }

    public static PVPArenaPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SchematicManager getSchematicManager() {
        return schematicManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public MatchManager getMatchManager() {
        return matchManager;
    }
}