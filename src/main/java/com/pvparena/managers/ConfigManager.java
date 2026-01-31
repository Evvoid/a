package com.pvparena.managers;

import com.pvparena.PVPArenaPlugin;
import com.pvparena.models.GameMode;
import com.pvparena.models.Kit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ConfigManager {

    private final PVPArenaPlugin plugin;
    private final FileConfiguration config;
    private final Map<String, GameMode> gameModes;
    private final Map<String, Kit> kits;

    public ConfigManager(PVPArenaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.gameModes = new HashMap<>();
        this.kits = new HashMap<>();
        
        loadKits();
        loadGameModes();
    }

    private void loadGameModes() {
        gameModes.clear();
        ConfigurationSection modesSection = config.getConfigurationSection("game-modes");
        
        if (modesSection == null) {
            plugin.getLogger().warning("No game modes configured!");
            return;
        }
        
        for (String key : modesSection.getKeys(false)) {
            ConfigurationSection modeSection = modesSection.getConfigurationSection(key);
            if (modeSection == null) continue;
            
            String displayName = ChatColor.translateAlternateColorCodes('&', 
                modeSection.getString("display-name", key));
            Material material = Material.valueOf(modeSection.getString("material", "DIAMOND_SWORD"));
            int slot = modeSection.getInt("slot", 0);
            String kitName = modeSection.getString("kit", key);
            
            GameMode mode = new GameMode(key, displayName, material, slot, kitName);
            gameModes.put(key, mode);
        }
        
        plugin.getLogger().info("Loaded " + gameModes.size() + " game modes");
    }

    private void loadKits() {
        kits.clear();
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        
        if (kitsSection == null) {
            plugin.getLogger().warning("No kits configured!");
            return;
        }
        
        for (String key : kitsSection.getKeys(false)) {
            ConfigurationSection kitSection = kitsSection.getConfigurationSection(key);
            if (kitSection == null) continue;
            
            Kit kit = new Kit(key);
            
            // Load armor
            ConfigurationSection armorSection = kitSection.getConfigurationSection("armor");
            if (armorSection != null) {
                kit.setHelmet(getMaterial(armorSection.getString("helmet")));
                kit.setChestplate(getMaterial(armorSection.getString("chestplate")));
                kit.setLeggings(getMaterial(armorSection.getString("leggings")));
                kit.setBoots(getMaterial(armorSection.getString("boots")));
            }
            
            // Load items
            List<Map<?, ?>> items = kitSection.getMapList("items");
            for (Map<?, ?> itemMap : items) {
                String type = (String) itemMap.get("type");
                int amount = itemMap.containsKey("amount") ? (Integer) itemMap.get("amount") : 1;
                
                ItemStack item = new ItemStack(Material.valueOf(type), amount);
                
                // Handle potions
                if (type.contains("POTION")) {
                    String potionType = (String) itemMap.get("potion-type");
                    int potionLevel = itemMap.containsKey("potion-level") ? (Integer) itemMap.get("potion-level") : 1;
                    
                    if (potionType != null && item.getItemMeta() instanceof PotionMeta) {
                        PotionMeta meta = (PotionMeta) item.getItemMeta();
                        PotionEffectType effectType = PotionEffectType.getByName(potionType);
                        if (effectType != null) {
                            meta.addCustomEffect(new PotionEffect(effectType, 1, potionLevel - 1), true);
                            item.setItemMeta(meta);
                        }
                    }
                }
                
                kit.addItem(item);
            }
            
            kits.put(key, kit);
        }
        
        plugin.getLogger().info("Loaded " + kits.size() + " kits");
    }

    private Material getMaterial(String name) {
        if (name == null) return Material.AIR;
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Material.AIR;
        }
    }

    public int getArenaSpacing() {
        return config.getInt("arena-spacing", 1000);
    }

    public int getArenaCleanupDelay() {
        return config.getInt("arena-cleanup-delay", 30);
    }

    public int getCountdownDuration() {
        return config.getInt("countdown-duration", 3);
    }

    public String getPvpWorldName() {
        return config.getString("pvp-world-name", "pvp");
    }

    public Location getMainWorldSpawn() {
        String worldName = config.getString("main-world-spawn.world", "world");
        int x = config.getInt("main-world-spawn.x", 0);
        int y = config.getInt("main-world-spawn.y", 64);
        int z = config.getInt("main-world-spawn.z", 0);
        
        return new Location(plugin.getServer().getWorld(worldName), x, y, z);
    }

    public String getCompassName() {
        return ChatColor.translateAlternateColorCodes('&', 
            config.getString("compass-item.name", "&c&lPVP Arena"));
    }

    public List<String> getCompassLore() {
        List<String> lore = new ArrayList<>();
        List<String> configLore = config.getStringList("compass-item.lore");
        for (String line : configLore) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return lore;
    }

    public Map<String, GameMode> getGameModes() {
        return new HashMap<>(gameModes);
    }

    public GameMode getGameMode(String key) {
        return gameModes.get(key);
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public int getArenaSize() {
        return config.getInt("arena.size", 50);
    }

    public int getFloorY() {
        return config.getInt("arena.floor-y", 64);
    }

    public Location getSpawnPoint1(Location arenaCenter) {
        int x = config.getInt("arena.spawn-points.point1.x", 15);
        int y = config.getInt("arena.spawn-points.point1.y", 65);
        int z = config.getInt("arena.spawn-points.point1.z", 0);
        
        return arenaCenter.clone().add(x, y - arenaCenter.getY(), z);
    }

    public Location getSpawnPoint2(Location arenaCenter) {
        int x = config.getInt("arena.spawn-points.point2.x", -15);
        int y = config.getInt("arena.spawn-points.point2.y", 65);
        int z = config.getInt("arena.spawn-points.point2.z", 0);
        
        return arenaCenter.clone().add(x, y - arenaCenter.getY(), z);
    }

    public Material getFloorMaterial() {
        String material = config.getString("arena.blocks.floor.material", "STONE");
        return Material.valueOf(material);
    }

    public Material getBarrierMaterial() {
        String material = config.getString("arena.blocks.barrier.material", "BARRIER");
        return Material.valueOf(material);
    }

    public int getBarrierHeight() {
        return config.getInt("arena.blocks.barrier.height", 10);
    }

    public String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', 
            config.getString("messages." + key, ""));
    }

    public String getMessage(String key, String... replacements) {
        String message = getMessage(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return message;
    }
}
