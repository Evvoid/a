package com.pvparena.listeners;

import com.pvparena.PVPArenaPlugin;
import com.pvparena.gui.GameModeGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CompassClickListener implements Listener {

    private final PVPArenaPlugin plugin;
    private final GameModeGUI gui;

    public CompassClickListener(PVPArenaPlugin plugin) {
        this.plugin = plugin;
        this.gui = new GameModeGUI(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.COMPASS) {
            return;
        }

        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        String compassName = plugin.getConfigManager().getCompassName();
        if (item.getItemMeta().getDisplayName().equals(compassName)) {
            event.setCancelled(true);
            gui.openGUI(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getView().getTitle().equals("§c§lSelect Game Mode")) {
            event.setCancelled(true);
            
            ItemStack clickedItem = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();
            
            gui.handleClick(player, clickedItem);
        }
    }
}
