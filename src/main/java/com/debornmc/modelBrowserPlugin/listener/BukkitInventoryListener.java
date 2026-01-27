package com.debornmc.modelBrowserPlugin.listener;

import com.debornmc.modelBrowserPlugin.ModelBrowserPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BukkitInventoryListener implements Listener {

    private final ModelBrowserPlugin plugin;

    public BukkitInventoryListener(ModelBrowserPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (plugin.getGUIManager().hasOpenGUI(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getGUIManager().closeGUI(event.getPlayer());
    }
}