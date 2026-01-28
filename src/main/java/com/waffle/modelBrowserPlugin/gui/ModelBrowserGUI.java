package com.waffle.modelBrowserPlugin.gui;

import com.waffle.modelBrowserPlugin.ModelBrowserPlugin;
import com.waffle.modelBrowserPlugin.manager.ModelManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ModelBrowserGUI extends BaseGUI {

    private final ModelManager modelManager;
    private final ModelBrowserPlugin plugin;
    private int currentPage = 0;
    private static final int MODELS_PER_PAGE = 45;
    private final Map<String, Material> modelMaterials = new HashMap<>();

    // Where to send players to import models (GitHub, docs, etc.)
    private static final String IMPORT_URL = "https://wafflecross97.github.io/PG-RP/";

    public ModelBrowserGUI(Player player) {
        super(player, ChatColor.DARK_GRAY + "Model Browser", 6);
        this.plugin = ModelBrowserPlugin.getInstance();
        this.modelManager = plugin.getModelManager();
        generateMappings();
    }

    private void generateMappings() {
        List<String> allModels = modelManager.getAvailableModels();
        List<String> itemsModels = getItemsFolderModels(allModels);
        modelMaterials.clear();
        for (String modelName : itemsModels) {
            modelMaterials.put(modelName, autoDetectMaterial(modelName));
        }
    }

    private Material autoDetectMaterial(String modelName) {
        String lower = modelName.toLowerCase();
        if (lower.contains("shield")) return Material.SHIELD;
        if (lower.contains("coin") || lower.contains("gold") || lower.contains("nugget")) return Material.GOLD_NUGGET;
        if (lower.contains("sword")) return Material.IRON_SWORD;
        if (lower.contains("axe")) return Material.IRON_AXE;
        if (lower.contains("pickaxe") || lower.contains("pick")) return Material.IRON_PICKAXE;
        return Material.PAPER;
    }

    @Override
    public void build() {
        clear();
        List<String> allModels = modelManager.getAvailableModels();
        List<String> itemsModels = getItemsFolderModels(allModels);
        int startIdx = currentPage * MODELS_PER_PAGE;
        int endIdx = Math.min(startIdx + MODELS_PER_PAGE, itemsModels.size());

        addHeaderItems(itemsModels.size());
        addModelItemsCleanGrid(itemsModels, startIdx, endIdx);
        addFooterItems(itemsModels.size());
    }

    protected List<String> getItemsFolderModels(List<String> allModels) {
        List<String> itemsModels = new ArrayList<>();
        for (String model : allModels) {
            if (!model.contains("/")) itemsModels.add(model);
        }
        Collections.sort(itemsModels);
        return itemsModels;
    }

    private void addModelItemsCleanGrid(List<String> models, int startIdx, int endIdx) {
        int slot = 9;
        for (int i = startIdx; i < endIdx; i++) {
            if (slot >= 54) break;
            String modelName = models.get(i);
            ItemStack modelItem = createModelApplierPreview(modelName);
            GUIItem guiItem = new GUIItem(modelItem, modelName, getModelLore(modelName),
                    (p, click) -> handleModelClick(modelName, click));
            setItem(slot, guiItem);
            slot++;
        }
    }

    /**
     * Create a preview item that already has the correct model + name.
     */
    private ItemStack createModelApplierPreview(String modelName) {
        Material material = modelMaterials.getOrDefault(modelName, autoDetectMaterial(modelName));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            applyModelToMeta(meta, modelName);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Apply the selected model + display name onto an existing ItemMeta.
     * Used for both preview items and "apply to held item".
     */
    private void applyModelToMeta(ItemMeta meta, String modelName) {
        // Technical link to the JSON file
        String cleanedKey = modelName.toLowerCase().replace("minecraft:", "");
        try {
            meta.setItemModel(NamespacedKey.minecraft(cleanedKey));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to apply item model: " + cleanedKey);
        }

        // Display name MUST match the raw string used in the
        // resource pack condition `"when": "Waffle Shield"`.
        // Any extra styling (color/italic) changes the JSON
        // component and breaks the equality check, so we keep
        // it as a plain text component.
        String formattedName = formatDisplayName(modelName);
        meta.displayName(Component.text(formattedName));

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
    }

    private String formatDisplayName(String name) {
        String cleaned = name.toLowerCase().replace("minecraft:", "").replace("_", " ");

        // Special shields should ONLY trigger when the model id also looks like a shield.
        boolean looksLikeShield = cleaned.contains("shield");
        if (looksLikeShield && cleaned.contains("lilypad")) return "LilyPad Shield";
        if (looksLikeShield && cleaned.contains("traptanium")) return "Traptanium Shield";
        if (looksLikeShield && cleaned.contains("waffle")) return "Waffle Shield";

        // Default: title‑case the cleaned id (e.g. "waffle sword" -> "Waffle Sword")
        StringBuilder sb = new StringBuilder();
        for (String word : cleaned.split(" ")) {
            if (!word.isEmpty()) sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private List<String> getModelLore(String modelName) {
        return Arrays.asList(ChatColor.DARK_GRAY + "1.21.4 Item Model", "",
                ChatColor.GRAY + "ID: " + ChatColor.WHITE + modelName,
                ChatColor.BLUE + "✔ Unbreakable", "",
                ChatColor.YELLOW + "Left-click" + ChatColor.GRAY + " to get item");
    }

    private void addHeaderItems(int totalModels) {
        // Title Slot
        ItemStack titleItem = new ItemStack(Material.BOOK);
        ItemMeta tm = titleItem.getItemMeta();
        tm.setDisplayName(ChatColor.GOLD + "Model Browser (1.21.4)");
        titleItem.setItemMeta(tm);
        setItem(4, new GUIItem(titleItem, "title", null, null));

        // IMPORT BUTTON Slot 7
        ItemStack importItem = new ItemStack(Material.ENDER_CHEST);
        ItemMeta im = importItem.getItemMeta();
        im.setDisplayName(ChatColor.AQUA + "Import Models (Web)");
        im.setLore(Arrays.asList(
                ChatColor.GRAY + "Opens the web importer",
                ChatColor.DARK_GRAY + "Use the site, then reload mappings"
        ));
        importItem.setItemMeta(im);
        setItem(7, new GUIItem(importItem, "import", null, (player, click) -> {
            // Always: send web importer link
            Component msg = Component.text("Click here to open the import page")
                    .clickEvent(ClickEvent.openUrl(IMPORT_URL));
            player.sendMessage(msg);
        }));

        // RELOAD BUTTON Slot 8
        ItemStack reloadItem = new ItemStack(Material.SUNFLOWER);
        ItemMeta rm = reloadItem.getItemMeta();
        rm.setDisplayName(ChatColor.YELLOW + "Reload Mappings");
        rm.setLore(Collections.singletonList(ChatColor.GRAY + "Refresh internal model list"));
        reloadItem.setItemMeta(rm);
        setItem(8, new GUIItem(reloadItem, "reload", null, (player, click) -> {
            generateMappings(); build(); refresh();
            player.sendMessage(ChatColor.AQUA + "GUI Mappings Reloaded!");
        }));
    }

    private void addFooterItems(int totalModels) {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalModels / MODELS_PER_PAGE));
        if (currentPage > 0) {
            setItem(48, new GUIItem(new ItemStack(Material.ARROW), "prev", null, (p, c) -> { currentPage--; build(); refresh(); }));
        }
        if (currentPage < totalPages - 1) {
            setItem(50, new GUIItem(new ItemStack(Material.ARROW), "next", null, (p, c) -> { currentPage++; build(); refresh(); }));
        }

        // EXIT BUTTON FIX
        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta em = exitItem.getItemMeta();
        em.setDisplayName(ChatColor.RED + "Exit Browser");
        exitItem.setItemMeta(em);
        setItem(53, new GUIItem(exitItem, "close", null, (p, c) -> close()));
    }

    private void handleModelClick(String modelName, ClickType click) {
        if (!click.toString().contains("LEFT")) {
            return;
        }

        // If the player is holding something, apply the model to that item.
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand != null && inHand.getType() != Material.AIR) {
            ItemMeta meta = inHand.getItemMeta();
            if (meta != null) {
                applyModelToMeta(meta, modelName);
                inHand.setItemMeta(meta);
                player.sendMessage(ChatColor.GREEN + "Applied model to held item: " +
                        ChatColor.WHITE + formatDisplayName(modelName));
                return;
            }
        }

        // Fallback: if hand is empty or meta is null, just give a new preview item,
        // but only if the player has the proper permission.
        if (!player.hasPermission("modelbrowser.give")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to receive items from the Model Browser.");
            return;
        }

        player.getInventory().addItem(createModelApplierPreview(modelName));
        player.sendMessage(ChatColor.GREEN + "Given: " + ChatColor.WHITE + formatDisplayName(modelName));
    }
}