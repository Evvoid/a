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
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    private QueueManager queueManager;
    private ArenaManager arenaManager;
    private MatchManager matchManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        configManager = new ConfigManager(this);
        arenaManager = new ArenaManager(this);
        queueManager = new QueueManager(this);
        matchManager = new MatchManager(this);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new CompassClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        
        // Ensure PVP world exists
        if (Bukkit.getWorld(configManager.getPvpWorldName()) == null) {
            getLogger().warning("PVP world '" + configManager.getPvpWorldName() + "' does not exist! Please create it.");
        }
        
        getLogger().info("PVP Arena Plugin has been enabled!");
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
                sender.sendMessage("§e/pvparena reload §7- Reload configuration");
                sender.sendMessage("§e/pvparena debug §7- Show debug information");
                if (sender instanceof Player) {
                    sender.sendMessage("§e/pvparena give §7- Get arena compass");
                }
                return true;
            }
            
            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                configManager = new ConfigManager(this);
                sender.sendMessage("§aConfiguration reloaded!");
                return true;
            }
            
            if (args[0].equalsIgnoreCase("debug")) {
                sender.sendMessage("§e=== PVP Arena Debug ===");
                sender.sendMessage("§7Queues: " + queueManager.getTotalInQueue());
                sender.sendMessage("§7Active matches: " + matchManager.getActiveMatches());
                sender.sendMessage("§7Available arena coords: " + arenaManager.getAvailableCoordinates());
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
