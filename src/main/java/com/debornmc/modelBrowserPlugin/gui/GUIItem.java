package com.debornmc.modelBrowserPlugin.gui;

import org.bukkit.inventory.ItemStack;
import java.util.List;

public class GUIItem {
    private final ItemStack item;
    private final String id;
    private final List<String> lore;
    private final ClickAction clickAction;

    public GUIItem(ItemStack item, String id, List<String> lore, ClickAction clickAction) {
        this.item = item;
        this.id = id;
        this.lore = lore;
        this.clickAction = clickAction;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getId() {
        return id;
    }

    public List<String> getLore() {
        return lore;
    }

    public ClickAction getClickAction() {
        return clickAction;
    }
}