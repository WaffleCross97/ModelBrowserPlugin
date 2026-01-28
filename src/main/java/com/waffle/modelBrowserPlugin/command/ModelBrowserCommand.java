package com.waffle.modelBrowserPlugin.command;

import com.waffle.modelBrowserPlugin.ModelBrowserPlugin;
import com.waffle.modelBrowserPlugin.gui.ModelBrowserGUI;
import com.waffle.modelBrowserPlugin.manager.ModelManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Command executor for the /modelbrowser command.
 * <p>
 * This class handles all subcommands and provides an interface for players
 * to interact with the Model Browser Plugin via chat commands.
 * </p>
 */
public class ModelBrowserCommand implements CommandExecutor {
    /** The main plugin instance for accessing global functionality. */
    private final ModelBrowserPlugin plugin;

    /** Manager responsible for model operations like loading, searching, and categorization. */
    private final ModelManager modelManager;

    /**
     * Constructs a new ModelBrowserCommand instance.
     */
    public ModelBrowserCommand() {
        this.plugin = ModelBrowserPlugin.getInstance();
        this.modelManager = plugin.getModelManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED));
            return true;
        }

        // If no arguments or "gui" command, open the GUI
        if (args.length == 0 || args[0].equalsIgnoreCase("gui")) {
            return handleOpenGUI(player);
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                return handleReload(player);

            case "list":
                return handleList(player, args);

            case "search":
                return handleSearch(player, args);

            case "categories":
                return handleCategories(player);

            case "info":
                return handleInfo(player, args);

            case "upload":
                return handleUpload(player, args);

            case "delete":
                return handleDelete(player, args);

            case "help":
                return handleHelp(player);

            case "test":
                return handleTest(player);

            default:
                player.sendMessage(Component.text("Unknown subcommand. Use /modelbrowser help").color(NamedTextColor.RED));
                return true;
        }
    }

    /**
     * Opens the Model Browser GUI
     */
    private boolean handleOpenGUI(Player player) {
        try {
            // Open the BaseGUI (Bukkit GUI)
            ModelBrowserGUI gui = new ModelBrowserGUI(player);
            plugin.getGUIManager().openGUI(player, gui);
            player.sendMessage(Component.text("Opening Model Browser GUI...").color(NamedTextColor.GREEN));
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to open GUI: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(Component.text("Failed to open GUI! Check console for errors.").color(NamedTextColor.RED));
            return true;
        }
    }

    private boolean handleTest(Player player) {
        plugin.testNetwork(player);
        player.sendMessage(Component.text("Test packet sent via PacketEvents!").color(NamedTextColor.GREEN));
        player.sendMessage(Component.text("Check if the client mod received it.").color(NamedTextColor.GRAY));
        return true;
    }

    private boolean handleReload(Player player) {
        if (!player.hasPermission("modelbrowser.admin")) {
            player.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return true;
        }

        plugin.reloadPlugin();
        player.sendMessage(Component.text("Plugin reloaded!").color(NamedTextColor.GREEN));
        return true;
    }

    private boolean handleList(Player player, String[] args) {
        // Instead of chat output, send model list via PacketEvents to the GUI
        List<String> models = modelManager.getAvailableModels();
        plugin.getPacketCommunicator().sendModelList(player, models);

        player.sendMessage(Component.text("Sending model list to GUI...").color(NamedTextColor.GREEN));
        player.sendMessage(Component.text(models.size() + " models available").color(NamedTextColor.GRAY));
        return true;
    }

    private boolean handleSearch(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /modelbrowser search <query>").color(NamedTextColor.RED));
            return true;
        }

        String query = args[1];
        List<String> results = modelManager.searchModels(query);
        plugin.getPacketCommunicator().sendSearchResults(player, query, results);

        player.sendMessage(Component.text("Search results sent to GUI!").color(NamedTextColor.GREEN));
        return true;
    }

    private boolean handleCategories(Player player) {
        Map<String, List<String>> categories = modelManager.getCategories();
        plugin.getPacketCommunicator().sendCategories(player, categories);

        player.sendMessage(Component.text("Categories sent to GUI!").color(NamedTextColor.GREEN));
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /modelbrowser info <model>").color(NamedTextColor.RED));
            return true;
        }

        String modelId = args[1];
        Map<String, Object> info = modelManager.getModelInfo(modelId);

        if (info == null || !info.containsKey("exists") || !(Boolean) info.get("exists")) {
            player.sendMessage(Component.text("Model not found: " + modelId).color(NamedTextColor.RED));
            return true;
        }

        plugin.getPacketCommunicator().sendModelInfo(player, modelId, info);
        player.sendMessage(Component.text("Model info sent to GUI!").color(NamedTextColor.GREEN));
        return true;
    }

    private boolean handleUpload(Player player, String[] args) {
        // The client mod handles uploads and sends packets to the server
        player.sendMessage(Component.text("Use the Model Browser mod GUI to upload models.").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("The upload will be sent to the server via PacketEvents.").color(NamedTextColor.GRAY));
        return true;
    }

    private boolean handleDelete(Player player, String[] args) {
        if (!player.hasPermission("modelbrowser.delete")) {
            player.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /modelbrowser delete <model>").color(NamedTextColor.RED));
            return true;
        }

        String modelId = args[1];
        boolean success = modelManager.deleteModel(modelId);

        if (success) {
            player.sendMessage(Component.text("Model deleted: " + modelId).color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Model not found: " + modelId).color(NamedTextColor.RED));
        }

        return true;
    }

    private boolean handleHelp(Player player) {
        player.sendMessage(Component.text("=== ModelBrowser Help ===").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("/modelbrowser - Open the Model Browser GUI"));
        player.sendMessage(Component.text("/modelbrowser gui - Open the Model Browser GUI"));
        player.sendMessage(Component.text("/modelbrowser list - Send model list to client mod"));
        player.sendMessage(Component.text("/modelbrowser search <query> - Search models and send to client mod"));
        player.sendMessage(Component.text("/modelbrowser categories - Send categories to client mod"));
        player.sendMessage(Component.text("/modelbrowser info <model> - Get model info in client mod"));
        player.sendMessage(Component.text("/modelbrowser test - Test PacketEvents connection"));

        if (player.hasPermission("modelbrowser.admin")) {
            player.sendMessage(Component.text("/modelbrowser reload - Reload the plugin"));
        }

        if (player.hasPermission("modelbrowser.delete")) {
            player.sendMessage(Component.text("/modelbrowser delete <model> - Delete a model"));
        }

        player.sendMessage(Component.text(" "));
        player.sendMessage(Component.text("Note: Some features require the Model Browser client mod").color(NamedTextColor.GRAY));
        return true;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private String formatTimestamp(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000;

        if (seconds < 60) return seconds + " seconds ago";
        if (seconds < 3600) return (seconds / 60) + " minutes ago";
        if (seconds < 86400) return (seconds / 3600) + " hours ago";
        return (seconds / 86400) + " days ago";
    }
}