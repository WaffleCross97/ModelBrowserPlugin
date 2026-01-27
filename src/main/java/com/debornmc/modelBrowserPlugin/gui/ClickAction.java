package com.debornmc.modelBrowserPlugin.gui;

import org.bukkit.entity.Player;

public interface ClickAction {
    void onClick(Player player, ClickType clickType);
}