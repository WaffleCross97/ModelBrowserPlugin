package com.debornmc.modelBrowserPlugin.gui;

import com.debornmc.modelBrowserPlugin.ModelBrowserPlugin;
import com.debornmc.modelBrowserPlugin.manager.ModelManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Main Model Browser GUI with automatic ModelApplier-style preview items
 */
public class ModelBrowserGUI extends BaseGUI {

    private final ModelManager modelManager;
    private final ModelBrowserPlugin plugin;
    private int currentPage = 0;
    private static final int MODELS_PER_PAGE = 45;

    // Auto-generated maps (no hardcoding!)
    //private final Map<String, Material> modelMaterials = new HashMap<>();
    //private final Map<String, Integer> modelDataIds = new HashMap<>();

    public ModelBrowserGUI(Player player) {
        super(player, ChatColor.DARK_GRAY + "Model Browser", 6);
        this.plugin = ModelBrowserPlugin.getInstance();
        this.modelManager = plugin.getModelManager();

        // Auto-generate material and CMD mappings
        generateMappings();
    }

    /**
     * AUTO-GENERATE material and CMD mappings from model names
     */
    private void generateMappings() {
        List<String> allModels = modelManager.getAvailableModels();
        List<String> itemsModels = getItemsFolderModels(allModels);

        int cmdCounter = 1000;

        for (String modelName : itemsModels) {
            // Auto-determine material based on model name
            Material material = autoDetectMaterial(modelName);
            modelMaterials.put(modelName, material);

            // Auto-generate CMD (optional, for better previews)
            modelDataIds.put(modelName, cmdCounter);
            cmdCounter++;
        }

        plugin.getLogger().info("Auto-generated mappings for " + itemsModels.size() + " models");
    }

    /**
     * AUTO-DETECT material from model name
     */
    private Material autoDetectMaterial(String modelName) {
        String lower = modelName.toLowerCase();

        if (lower.contains("coin") || lower.contains("gold") || lower.contains("nugget")) {
            return Material.GOLD_NUGGET;
        } else if (lower.contains("shield")) {
            return Material.SHIELD;
        } else if (lower.contains("sword")) {
            return Material.IRON_SWORD;
        } else if (lower.contains("axe")) {
            return Material.IRON_AXE;
        } else if (lower.contains("pickaxe") || lower.contains("pick")) {
            return Material.IRON_PICKAXE;
        } else if (lower.contains("hat") || lower.contains("helmet") || lower.contains("cap")) {
            return Material.LEATHER_HELMET;
        } else if (lower.contains("chestplate") || lower.contains("armor")) {
            return Material.IRON_CHESTPLATE;
        } else if (lower.contains("boot") || lower.contains("shoe")) {
            return Material.LEATHER_BOOTS;
        } else if (lower.contains("bow") || lower.contains("arrow")) {
            return Material.BOW;
        } else if (lower.contains("potion") || lower.contains("bottle")) {
            return Material.POTION;
        } else if (lower.contains("food") || lower.contains("apple") || lower.contains("bread")) {
            return Material.APPLE;
        } else {
            return Material.PAPER; // Default fallback
        }
    }

    @Override
    public void build() {
        clear();

        // Get ALL models then filter to only items/ folder
        List<String> allModels = modelManager.getAvailableModels();
        List<String> itemsModels = getItemsFolderModels(allModels);

        plugin.getLogger().info(
                "GUI: " + allModels.size() + " total -> " + itemsModels.size() + " items/"
        );

        int startIdx = currentPage * MODELS_PER_PAGE;
        int endIdx = Math.min(startIdx + MODELS_PER_PAGE, itemsModels.size());

        // Add header
        addHeaderItems(itemsModels.size());

        // Add model items - CLEAN GRID LAYOUT
        addModelItemsCleanGrid(itemsModels, startIdx, endIdx);

        // Add footer with pagination
        addFooterItems(itemsModels.size());
    }

    /**
     * Get ONLY models from items/ folder
     */
    protected List<String> getItemsFolderModels(List<String> allModels) {
        List<String> itemsModels = new ArrayList<>();

        // Simple filter: Items/ folder models don't have slashes
        for (String model : allModels) {
            if (!model.contains("/")) {
                itemsModels.add(model);
            }
        }

        // Sort alphabetically for consistent display
        Collections.sort(itemsModels);

        return itemsModels;
    }

    /**
     * CLEAN GRID: Add model items in simple grid layout
     */
    private void addModelItemsCleanGrid(List<String> models, int startIdx, int endIdx) {
        int slot = 9; // Start at row 2, column 1 (slot 9)

        for (int i = startIdx; i < endIdx; i++) {
            if (slot >= 54) break;

            String modelName = models.get(i);
            ItemStack modelItem = createModelApplierPreview(modelName);

            GUIItem guiItem = new GUIItem(modelItem, modelName,
                    getModelLore(modelName),
                    (player, click) -> handleModelClick(modelName, click)
            );

            setItem(slot, guiItem);
            slot++;
        }
    }

    /**
     * Create ModelApplier-style preview item (AUTOMATIC)
     */
    private ItemStack createModelApplierPreview(String modelName) {
        // AUTO: Get material from generated map (or detect on the fly)
        Material material = modelMaterials.getOrDefault(modelName,
                autoDetectMaterial(modelName));

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // AUTO: Use the exact model name from items/ folder
            meta.setDisplayName(ChatColor.RESET + modelName);

            // AUTO: Apply generated CMD if available
            Integer customModelData = modelDataIds.get(modelName);
            if (customModelData != null && customModelData > 0) {
                meta.setCustomModelData(customModelData);
                item.addUnsafeEnchantment(Enchantment.LURE, 1);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            // Store the model ID
            NamespacedKey modelKey = new NamespacedKey(plugin, "modelapplier_id");
            meta.getPersistentDataContainer().set(modelKey,
                    PersistentDataType.STRING, modelName);

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Get lore for model items (AUTOMATIC)
     */
    private List<String> getModelLore(String modelName) {
        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.DARK_GRAY + "ModelApplier Model");
        lore.add("");
        lore.add(ChatColor.GRAY + "ID: " + ChatColor.WHITE + modelName);

        // AUTO: Show material type
        Material material = modelMaterials.getOrDefault(modelName,
                autoDetectMaterial(modelName));
        lore.add(ChatColor.GRAY + "Type: " + ChatColor.WHITE +
                material.toString().toLowerCase().replace("_", " "));

        // AUTO: Show CMD if available
        Integer cmd = modelDataIds.get(modelName);
        if (cmd != null && cmd > 0) {
            lore.add(ChatColor.GRAY + "CustomModelData: " + ChatColor.YELLOW + cmd);
        }

        lore.add("");
        lore.add(ChatColor.YELLOW + "Left-click" + ChatColor.GRAY + " to get item");
        lore.add(ChatColor.YELLOW + "Right-click" + ChatColor.GRAY + " for info");
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Hold to see model preview");

        return lore;
    }

    /**
     * Add header items (row 1)
     */
    private void addHeaderItems(int totalModels) {
        ItemStack titleItem = new ItemStack(Material.BOOK);
        ItemMeta meta = titleItem.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Model Browser");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Items/ folder models: " + ChatColor.YELLOW + totalModels);
        lore.add(ChatColor.GRAY + "Page: " + ChatColor.YELLOW + (currentPage + 1));
        lore.add("");
        lore.add(ChatColor.GRAY + "Auto-detected " + modelMaterials.size() + " models");
        lore.add(ChatColor.YELLOW + "Click to refresh");

        meta.setLore(lore);
        titleItem.setItemMeta(meta);

        setItem(4, new GUIItem(titleItem, "title", lore, (player, click) -> {
            player.sendMessage(ChatColor.GREEN + "Refreshing GUI...");
            currentPage = 0;
            generateMappings(); // Regenerate on refresh
            build();
            refresh();
        }));
    }

    /**
     * Add footer items (row 6)
     */
    private void addFooterItems(int totalModels) {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalModels / MODELS_PER_PAGE));

        // Previous page button
        if (currentPage > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta meta = prevPage.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Previous Page");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Page " + (currentPage) + " of " + totalPages);
            meta.setLore(lore);
            prevPage.setItemMeta(meta);

            setItem(48, new GUIItem(prevPage, "prev-page", lore, (player, click) -> {
                currentPage--;
                build();
                refresh();
            }));
        }

        // Current page indicator
        ItemStack pageInfo = new ItemStack(Material.MAP);
        ItemMeta meta = pageInfo.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Page " + (currentPage + 1) + "/" + totalPages);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Models: " + ChatColor.YELLOW + totalModels);
        lore.add(ChatColor.GRAY + "Showing: " + ChatColor.YELLOW +
                Math.min(MODELS_PER_PAGE, totalModels - (currentPage * MODELS_PER_PAGE)));
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Auto-detected from items/ folder");
        meta.setLore(lore);
        pageInfo.setItemMeta(meta);

        setItem(49, new GUIItem(pageInfo, "page-info", lore, null));

        // Next page button
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            meta = nextPage.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Next Page");
            lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Page " + (currentPage + 2) + " of " + totalPages);
            meta.setLore(lore);
            nextPage.setItemMeta(meta);

            setItem(50, new GUIItem(nextPage, "next-page", lore, (player, click) -> {
                currentPage++;
                build();
                refresh();
            }));
        }

        // Refresh button
        ItemStack refreshItem = new ItemStack(Material.ENDER_EYE);
        meta = refreshItem.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Refresh");
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to refresh model list");
        meta.setLore(lore);
        refreshItem.setItemMeta(meta);

        setItem(45, new GUIItem(refreshItem, "refresh", lore, (player, click) -> {
            modelManager.reload();
            currentPage = 0;
            generateMappings();
            build();
            refresh();
            player.sendMessage(ChatColor.GREEN + "Model list refreshed!");
        }));

        // Close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        meta = closeItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Close");
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to close the GUI");
        meta.setLore(lore);
        closeItem.setItemMeta(meta);

        setItem(53, new GUIItem(closeItem, "close", lore, (player, click) -> {
            close();
        }));
    }

    /**
     * Handle model clicks
     */
    private void handleModelClick(String modelName, ClickType clickType) {
        switch (clickType) {
            case LEFT_CLICK:
                ItemStack modelItem = createModelApplierPreview(modelName);
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(modelItem);

                if (leftover.isEmpty()) {
                    player.sendMessage(ChatColor.GREEN + "✓ Preview item given!");
                    player.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.WHITE + modelName);
                    player.sendMessage(ChatColor.GRAY + "For ModelApplier: rename to this exact name");
                } else {
                    player.sendMessage(ChatColor.RED + "✗ Inventory full!");
                }
                break;

            case RIGHT_CLICK:
                player.sendMessage(ChatColor.GOLD + "=== Model Info ===");
                player.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.WHITE + modelName);

                Material material = modelMaterials.getOrDefault(modelName,
                        autoDetectMaterial(modelName));
                player.sendMessage(ChatColor.GRAY + "Material: " +
                        ChatColor.WHITE + material.toString().toLowerCase().replace("_", " "));

                Integer cmd = modelDataIds.get(modelName);
                if (cmd != null && cmd > 0) {
                    player.sendMessage(ChatColor.GRAY + "CustomModelData: " +
                            ChatColor.YELLOW + cmd);
                }

                player.sendMessage("");
                player.sendMessage(ChatColor.YELLOW + "ModelApplier Instructions:");
                player.sendMessage(ChatColor.WHITE + "1. Rename any item to:");
                player.sendMessage(ChatColor.GREEN + "   \"" + modelName + "\"");
                player.sendMessage(ChatColor.WHITE + "2. Use ModelApplier menu");
                break;

            default:
                player.sendMessage(ChatColor.YELLOW + "Model: " + modelName);
                break;
        }
    }
}