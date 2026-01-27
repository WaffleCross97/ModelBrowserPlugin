package com.debornmc.modelBrowserPlugin.gui;

import com.debornmc.modelBrowserPlugin.ModelBrowserPlugin;
import com.debornmc.modelBrowserPlugin.util.ResourcePackScanner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Base class for all Bukkit-based GUIs
 */
public class BaseGUI {

    protected final Player player;
    protected final String title;
    protected final int size;
    protected Inventory inventory;
    protected final Map<Integer, GUIItem> items = new HashMap<>();

    public BaseGUI(Player player, String title, int rows) {
        this.player = player;
        this.title = title;
        this.size = rows * 9;
        this.inventory = Bukkit.createInventory(null, size, title);
    }

    /**
     * AUTOMATIC FILTER: Get ONLY models from items/ folder
     * Uses the file structure to detect, not manual lists
     */
    protected List<String> getItemsFolderModels(List<String> allModels) {
        List<String> itemsModels = new ArrayList<>();

        // Simple detection: Items/ folder models don't have slashes
        // Examples:
        // - items/ folder: "minecraft:coin" (NO slash)
        // - models/ folder: "minecraft:item/coin" (has slash)
        for (String model : allModels) {
            if (!model.contains("/")) {
                itemsModels.add(model);
            }
        }

        ModelBrowserPlugin.getInstance().getLogger().info(
                "Auto-filter: " + allModels.size() + " -> " + itemsModels.size() + " items/ models"
        );

        return itemsModels;
    }

    /**
     * Smart filter using ResourcePackScanner for 100% accuracy
     */
    protected List<String> getItemsFolderModelsSmart(List<String> allModels) {
        try {
            ModelBrowserPlugin plugin = ModelBrowserPlugin.getInstance();

            // Get resource pack path from config
            String resourcePackPath = plugin.getConfig().getString("resource-pack.path", "resourcepack");
            File resourcePackDir = new File(plugin.getDataFolder(), resourcePackPath);
            Path packPath = resourcePackDir.toPath();

            // Use scanner to get ACTUAL models from items/ folder
            List<String> actualItemsModels = ResourcePackScanner.scanResourcePack(packPath);
            Set<String> itemsSet = new HashSet<>(actualItemsModels);

            // Filter allModels to match what's actually in items/
            List<String> filtered = new ArrayList<>();
            for (String model : allModels) {
                if (itemsSet.contains(model)) {
                    filtered.add(model);
                }
            }

            plugin.getLogger().info(
                    "Smart filter: " + allModels.size() + " total, " +
                            actualItemsModels.size() + " in items/, " +
                            filtered.size() + " matched"
            );

            return filtered;

        } catch (IOException e) {
            ModelBrowserPlugin.getInstance().getLogger().warning(
                    "Error in smart filter, using simple filter: " + e.getMessage()
            );
            return getItemsFolderModels(allModels); // Fallback
        }
    }

    /**
     * Get models sorted for consistent display
     */
    protected List<String> getSortedModels(List<String> models) {
        List<String> sorted = new ArrayList<>(models);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Debug: Show what models are in the list
     */
    protected void debugModelList(String label, List<String> models) {
        ModelBrowserPlugin plugin = ModelBrowserPlugin.getInstance();
        plugin.getLogger().info("=== " + label + " (" + models.size() + ") ===");
        for (int i = 0; i < models.size(); i++) {
            plugin.getLogger().info(i + ": " + models.get(i));
        }
    }

    // ===== ORIGINAL METHODS (unchanged) =====

    /**
     * Open the GUI for the player
     */
    public void open() {
        build();
        player.openInventory(inventory);
        ModelBrowserPlugin.getInstance().getGUIManager().registerGUI(this);
    }

    /**
     * Close the GUI
     */
    public void close() {
        player.closeInventory();
        ModelBrowserPlugin.getInstance().getGUIManager().unregisterGUI(this);
    }

    /**
     * Build the GUI layout - override in subclasses
     */
    public void build() {
        // Override this in subclasses
    }

    /**
     * Set an item in a specific slot
     */
    public void setItem(int slot, GUIItem item) {
        items.put(slot, item);
        if (item != null && item.getItem() != null) {
            inventory.setItem(slot, item.getItem());
        } else {
            inventory.setItem(slot, null);
        }
    }

    /**
     * Clear all items from the GUI
     */
    public void clear() {
        items.clear();
        inventory.clear();
    }

    /**
     * Refresh/update the GUI display
     */
    public void refresh() {
        inventory.clear();
        for (Map.Entry<Integer, GUIItem> entry : items.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getItem() != null) {
                inventory.setItem(entry.getKey(), entry.getValue().getItem());
            }
        }
    }

    /**
     * Get the player viewing this GUI
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the Bukkit inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Get all GUI items
     */
    public Map<Integer, GUIItem> getItems() {
        return items;
    }

    /**
     * Handle inventory click events
     */
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true); // Prevent moving items

        int slot = event.getSlot();
        GUIItem item = items.get(slot);

        if (item != null && item.getClickAction() != null) {
            ClickType clickType = convertBukkitClick(event.getClick());
            item.getClickAction().onClick(player, clickType);
        }
    }

    /**
     * Convert Bukkit click type to our ClickType enum
     */
    private ClickType convertBukkitClick(org.bukkit.event.inventory.ClickType bukkitClick) {
        switch (bukkitClick) {
            case LEFT:
                return ClickType.LEFT_CLICK;
            case RIGHT:
                return ClickType.RIGHT_CLICK;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                return ClickType.SHIFT_CLICK;
            case MIDDLE:
                return ClickType.MIDDLE_CLICK;
            case DROP:
            case CONTROL_DROP:
                return ClickType.DROP;
            case NUMBER_KEY:
                return ClickType.NUMBER_KEY;
            case DOUBLE_CLICK:
                return ClickType.DOUBLE_CLICK;
            default:
                return ClickType.UNKNOWN;
        }
    }
}
