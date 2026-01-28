package com.waffle.modelBrowserPlugin.network;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.waffle.modelBrowserPlugin.ModelBrowserPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketEventsCommunicator {

    private static final String CHANNEL = "modelbrowser:main";
    private int nextWindowId = 100;

    // Container Type IDs for 1.14+
    private static final int CONTAINER_GENERIC_9X1 = 0;
    private static final int CONTAINER_GENERIC_9X2 = 1;
    private static final int CONTAINER_GENERIC_9X3 = 2;
    private static final int CONTAINER_GENERIC_9X4 = 3;
    private static final int CONTAINER_GENERIC_9X5 = 4;
    private static final int CONTAINER_GENERIC_9X6 = 5; // 54 slots - double chest
    private static final int CONTAINER_GENERIC_3X3 = 6;
    private static final int CONTAINER_ANVIL = 7;
    private static final int CONTAINER_BEACON = 8;
    private static final int CONTAINER_BLAST_FURNACE = 9;
    private static final int CONTAINER_BREWING_STAND = 10;
    private static final int CONTAINER_CRAFTING = 11;
    private static final int CONTAINER_ENCHANTMENT = 12;
    private static final int CONTAINER_FURNACE = 13;
    private static final int CONTAINER_GRINDSTONE = 14;
    private static final int CONTAINER_HOPPER = 15;
    private static final int CONTAINER_LECTERN = 16;
    private static final int CONTAINER_LOOM = 17;
    private static final int CONTAINER_MERCHANT = 18;
    private static final int CONTAINER_SHULKER_BOX = 19;
    private static final int CONTAINER_SMOKER = 20;
    private static final int CONTAINER_CARTOGRAPHY_TABLE = 21;
    private static final int CONTAINER_STONECUTTER = 22;

    public void initialize() {
        // Setup any packet listeners
    }

    public void shutdown() {
        // Clean up
    }

    // =============== GUI METHODS ===============

    public void openGUI(Player player, String title, int rows) {
        try {
            PlayerManager playerManager = PacketEvents.getAPI().getPlayerManager();
            ClientVersion clientVersion = playerManager.getClientVersion(player);

            if (clientVersion == null) {
                clientVersion = ClientVersion.V_1_21_7;
            }

            int windowId = nextWindowId++;
            int slots = rows * 9;

            // Create proper Component without legacy formatting
            Component componentTitle = createComponentFromLegacy(title);

            // Open window with integer container type
            WrapperPlayServerOpenWindow openWindow = new WrapperPlayServerOpenWindow(
                    windowId,
                    getContainerTypeForRows(rows),
                    componentTitle // Use Component
            );

            playerManager.sendPacket(player, openWindow);

            // Send empty items
            sendEmptyGUIItems(player, windowId, slots);

            // Use proper logger instead of System.out
            ModelBrowserPlugin.getInstance().getLogger().info(
                    "[PacketEvents] Opened GUI for " + player.getName()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to convert legacy formatted string to Component
    private Component createComponentFromLegacy(String legacyText) {
        // Convert & codes to proper TextColor
        // Remove all & codes and create a simple gray text
        String cleanText = legacyText
                .replace("&0", "").replace("&1", "").replace("&2", "")
                .replace("&3", "").replace("&4", "").replace("&5", "")
                .replace("&6", "").replace("&7", "").replace("&8", "")
                .replace("&9", "").replace("&a", "").replace("&b", "")
                .replace("&c", "").replace("&d", "").replace("&e", "")
                .replace("&f", "").replace("&l", "").replace("&o", "")
                .replace("&n", "").replace("&m", "").replace("&k", "")
                .replace("&r", "").trim();

        // If text starts with &8 (dark gray), use gray color
        if (legacyText.startsWith("&8")) {
            return Component.text(cleanText)
                    .color(TextColor.color(0x555555)) // Dark gray
                    .decoration(TextDecoration.ITALIC, false);
        }

        // Default to white text
        return Component.text(cleanText)
                .color(TextColor.color(0xFFFFFF))
                .decoration(TextDecoration.ITALIC, false);
    }

    public void openGUIWithId(Player player, String title, int containerTypeId, int slots) {
        try {
            PlayerManager playerManager = PacketEvents.getAPI().getPlayerManager();

            int windowId = nextWindowId++;

            WrapperPlayServerOpenWindow openWindow = new WrapperPlayServerOpenWindow(
                    windowId,
                    containerTypeId,
                    createComponentFromLegacy(title)
            );

            playerManager.sendPacket(player, openWindow);
            sendEmptyGUIItems(player, windowId, slots);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmptyGUIItems(Player player, int windowId, int slots) {
        try {
            PlayerManager playerManager = PacketEvents.getAPI().getPlayerManager();

            List<ItemStack> emptyItems = new ArrayList<>();
            for (int i = 0; i < slots; i++) {
                emptyItems.add(ItemStack.EMPTY);
            }

            WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(
                    windowId,
                    0,
                    emptyItems,
                    ItemStack.EMPTY  // Carried item (empty)
            );

            playerManager.sendPacket(player, windowItems);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendGUIItems(Player player, int windowId, List<org.bukkit.inventory.ItemStack> bukkitItems) {
        try {
            PlayerManager playerManager = PacketEvents.getAPI().getPlayerManager();

            List<ItemStack> packetItems = new ArrayList<>();

            for (org.bukkit.inventory.ItemStack bukkitItem : bukkitItems) {
                if (bukkitItem != null && bukkitItem.getType() != Material.AIR) {
                    packetItems.add(convertBukkitItem(bukkitItem));
                } else {
                    packetItems.add(ItemStack.EMPTY);
                }
            }

            WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(
                    windowId,
                    0,
                    packetItems,
                    ItemStack.EMPTY  // Carried item (empty)
            );

            playerManager.sendPacket(player, windowItems);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeGUI(Player player, int windowId) {
        try {
            PlayerManager playerManager = PacketEvents.getAPI().getPlayerManager();

            WrapperPlayServerCloseWindow closeWindow = new WrapperPlayServerCloseWindow(windowId);
            playerManager.sendPacket(player, closeWindow);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ItemStack convertBukkitItem(org.bukkit.inventory.ItemStack bukkitItem) {
        if (bukkitItem == null || bukkitItem.getType() == Material.AIR) {
            return ItemStack.EMPTY;
        }

        try {
            String materialName = bukkitItem.getType().name().toLowerCase();
            com.github.retrooper.packetevents.protocol.item.type.ItemType itemType =
                    ItemTypes.getByName(materialName);

            if (itemType == null) {
                itemType = ItemTypes.STONE; // Fallback
            }

            return ItemStack.builder()
                    .type(itemType)
                    .amount(bukkitItem.getAmount())
                    .build();

        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    private int getContainerTypeForRows(int rows) {
        switch (rows) {
            case 1: return CONTAINER_GENERIC_9X1;
            case 2: return CONTAINER_GENERIC_9X2;
            case 3: return CONTAINER_GENERIC_9X3;
            case 4: return CONTAINER_GENERIC_9X4;
            case 5: return CONTAINER_GENERIC_9X5;
            case 6: return CONTAINER_GENERIC_9X6; // Double chest - 54 slots
            default: return CONTAINER_GENERIC_9X3; // Default to 3 rows
        }
    }

    // =============== ORIGINAL METHODS ===============

    public void sendOpenBrowser(Player player) {
        sendPacket(player, "open_browser", new byte[0]);
    }

    public void sendModelList(Player player, List<String> models) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            out.writeUTF("model_list");
            out.writeInt(models.size());
            for (String model : models) {
                out.writeUTF(model);
            }

            sendPacket(player, baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendSearchResults(Player player, String query, List<String> results) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            out.writeUTF("search_results");
            out.writeUTF(query);
            out.writeInt(results.size());
            for (String result : results) {
                out.writeUTF(result);
            }

            sendPacket(player, baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCategories(Player player, Map<String, List<String>> categories) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            out.writeUTF("categories");
            out.writeInt(categories.size());
            for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeInt(entry.getValue().size());
                for (String model : entry.getValue()) {
                    out.writeUTF(model);
                }
            }

            sendPacket(player, baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendModelInfo(Player player, String modelId, Map<String, Object> info) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            out.writeUTF("model_info");
            out.writeUTF(modelId);
            out.writeLong((Long) info.get("size"));
            out.writeUTF((String) info.get("category"));
            out.writeLong((Long) info.get("lastModified"));

            sendPacket(player, baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNotification(Player player, String message) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            out.writeUTF("notification");
            out.writeUTF(message);

            sendPacket(player, baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPacket(Player player, byte[] data) {
        try {
            PlayerManager playerManager = PacketEvents.getAPI().getPlayerManager();
            ClientVersion clientVersion = playerManager.getClientVersion(player);

            if (clientVersion == null) {
                clientVersion = ClientVersion.V_1_21_7;
            }

            WrapperPlayServerPluginMessage packet = new WrapperPlayServerPluginMessage(
                    CHANNEL,
                    data
            );

            playerManager.sendPacket(player, packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPacket(Player player, String subChannel, byte[] data) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            out.writeUTF(subChannel);
            out.write(data);

            sendPacket(player, baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =============== SIMPLE GUI METHOD ===============

    public void openModelBrowserGUI(Player player) {
        // Open 9x6 GUI (54 slots)
        openGUI(player, "Model Browser", 6);

        // Create items for 54 slots
        List<org.bukkit.inventory.ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            items.add(new org.bukkit.inventory.ItemStack(Material.AIR));
        }

        // Add navigation items
        org.bukkit.inventory.ItemStack infoItem = new org.bukkit.inventory.ItemStack(Material.BOOK);
        ItemMeta meta = infoItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("&6Model Browser");
            List<String> lore = new ArrayList<>();
            lore.add("&7Server-side GUI");
            lore.add("&7Click items to interact");
            meta.setLore(lore);
            infoItem.setItemMeta(meta);
        }
        items.set(4, infoItem); // Center of first row

        // Add close button
        org.bukkit.inventory.ItemStack closeItem = new org.bukkit.inventory.ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("&cClose");
            closeItem.setItemMeta(closeMeta);
        }
        items.set(53, closeItem); // Last slot

        // Send items
        sendGUIItems(player, 100, items);
    }

    // Helper method to get current window ID
    public int getCurrentWindowId() {
        return nextWindowId - 1;
    }

    // Open specific GUI types
    public void openDoubleChestGUI(Player player, String title) {
        openGUIWithId(player, title, CONTAINER_GENERIC_9X6, 54);
    }

    public void openShulkerBoxGUI(Player player, String title) {
        openGUIWithId(player, title, CONTAINER_SHULKER_BOX, 27);
    }

    public void openHopperGUI(Player player, String title) {
        openGUIWithId(player, title, CONTAINER_HOPPER, 5);
    }
}