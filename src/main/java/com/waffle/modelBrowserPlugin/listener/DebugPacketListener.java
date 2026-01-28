package com.waffle.modelBrowserPlugin.listener;

import com.waffle.modelBrowserPlugin.ModelBrowserPlugin;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DebugPacketListener extends PacketListenerCommon implements PacketListener {

    private ModelBrowserPlugin plugin = null;

    public DebugPacketListener() {
    }

    @Override
    public PacketListenerAbstract asAbstract(PacketListenerPriority priority) {
        return PacketListener.super.asAbstract(priority);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // Log ALL packets to see what's actually being received
        String packetName = event.getPacketType().toString();
        plugin.getLogger().info("[PACKET DEBUG RECEIVE] Packet: " + packetName);

        // Get player name if possible
        User user = event.getUser();
        String playerName = "Unknown";
        if (user != null && user.getUUID() != null) {
            Player bukkitPlayer = Bukkit.getPlayer(user.getUUID());
            if (bukkitPlayer != null) {
                playerName = bukkitPlayer.getName();
            }
        }

        plugin.getLogger().info("[PACKET DEBUG RECEIVE] Player: " + playerName);

        // Special handling for click packets
        if (packetName.contains("CLICK")) {
            plugin.getLogger().info("[PACKET DEBUG] >>> CLICK PACKET DETECTED! <<<");
            plugin.getLogger().info("[PACKET DEBUG] Full packet name: " + packetName);

            Player bukkitPlayer = Bukkit.getPlayer(playerName);
            if (bukkitPlayer != null) {
                Bukkit.broadcastMessage("&e[PACKET DEBUG] Click packet from " + playerName);
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        String packetName = event.getPacketType().toString();

        if (packetName.contains("WINDOW") || packetName.contains("INVENTORY")) {
            plugin.getLogger().info("[PACKET DEBUG SEND] " + packetName);
        }
    }
}