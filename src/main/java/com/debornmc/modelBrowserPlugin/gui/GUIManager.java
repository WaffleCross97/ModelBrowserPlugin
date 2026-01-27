package com.debornmc.modelBrowserPlugin.gui;

import com.debornmc.modelBrowserPlugin.ModelBrowserPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager implements Listener {

    private final ModelBrowserPlugin plugin;
    private final Map<UUID, BaseGUI> openGUIs = new HashMap<>();

    public GUIManager(ModelBrowserPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Open a GUI for a player
     */
    public void openGUI(Player player, BaseGUI gui) {
        closeGUI(player);
        openGUIs.put(player.getUniqueId(), gui);
        gui.open();
        plugin.getLogger().info("Opened GUI for " + player.getName());
    }

    /**
     * Register a GUI (called from BaseGUI.open())
     */
    public void registerGUI(BaseGUI gui) {
        openGUIs.put(gui.getPlayer().getUniqueId(), gui);
    }

    /**
     * Unregister a GUI (called from BaseGUI.close())
     */
    public void unregisterGUI(BaseGUI gui) {
        openGUIs.remove(gui.getPlayer().getUniqueId());
    }

    /**
     * Close a player's GUI
     */
    public void closeGUI(Player player) {
        BaseGUI gui = openGUIs.remove(player.getUniqueId());
        if (gui != null) {
            gui.close();
        }
    }

    /**
     * Check if a player has a GUI open
     */
    public boolean hasOpenGUI(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }

    /**
     * Get a player's open GUI
     */
    public BaseGUI getOpenGUI(Player player) {
        return openGUIs.get(player.getUniqueId());
    }

    /**
     * Handle inventory click events
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        BaseGUI gui = openGUIs.get(player.getUniqueId());

        if (gui != null && gui.getInventory().equals(event.getInventory())) {
            gui.handleClick(event);
            plugin.getLogger().info("GUI click handled for " + player.getName());
        }
    }

    /**
     * Handle inventory close events
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        openGUIs.remove(player.getUniqueId());
    }

    /**
     * Handle player quit events
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        closeGUI(event.getPlayer());
    }
}