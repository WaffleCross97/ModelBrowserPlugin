package com.waffle.modelBrowserPlugin;

import com.waffle.modelBrowserPlugin.command.ModelBrowserCommand;
import com.waffle.modelBrowserPlugin.command.TabCompleter;
import com.waffle.modelBrowserPlugin.gui.GUIManager;
import com.waffle.modelBrowserPlugin.listener.BukkitInventoryListener;
import com.waffle.modelBrowserPlugin.manager.ModelManager;
import com.waffle.modelBrowserPlugin.network.PacketEventsCommunicator;
import com.waffle.modelBrowserPlugin.util.WebImportServer;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ModelBrowserPlugin extends JavaPlugin {

    private static ModelBrowserPlugin instance;
    private ModelManager modelManager;
    private GUIManager guiManager;
    private PacketEventsCommunicator packetCommunicator;
    private WebImportServer webImportServer;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;

        // Load configuration
        saveDefaultConfig();
        config = getConfig();

        // Initialize PacketEvents
        setupPacketEvents();

        // Initialize managers
        this.modelManager = new ModelManager(this);
        this.guiManager = new GUIManager(this);
        this.webImportServer = new WebImportServer(this);

        // Initialize PacketEvents communicator
        this.packetCommunicator = new PacketEventsCommunicator();
        this.packetCommunicator.initialize();

        // Register listeners
        registerListeners();

        getLogger().info("PacketEvents communicator initialized!");

        // Start lightweight HTTP server for web imports
        if (config.getBoolean("web-import.enabled", true)) {
            webImportServer.start();
        }

        // Register commands
        getCommand("modelbrowser").setExecutor(new ModelBrowserCommand());
        getCommand("modelbrowser").setTabCompleter(new TabCompleter());

        // Check for updates
        checkUpdates();

        // Delay final initialization to ensure everything is loaded
        Bukkit.getScheduler().runTaskLater(this, this::finishInitialization, 20L);
    }

    /**
     * Setup PacketEvents API
     */
    private void setupPacketEvents() {
        getLogger().info("=== Setting up PacketEvents ===");

        // Check if PacketEvents plugin exists
        if (getServer().getPluginManager().getPlugin("PacketEvents") == null) {
            getLogger().severe("❌ PacketEvents plugin not found! GUI features will not work.");
            return;
        }

        getLogger().info("✅ PacketEvents plugin detected: v" +
                getServer().getPluginManager().getPlugin("PacketEvents").getDescription().getVersion());

        // Initialize PacketEvents API
        try {
            if (!PacketEvents.getAPI().isInitialized()) {
                getLogger().info("Initializing PacketEvents API...");
                PacketEvents.getAPI().load();
                PacketEvents.getAPI().init();
                getLogger().info("✅ PacketEvents API initialized!");
            } else {
                getLogger().info("✅ PacketEvents API already initialized");
            }

            // Test if API is working
            getLogger().info("PacketEvents API status:");
            getLogger().info("- Is initialized: " + PacketEvents.getAPI().isInitialized());
            getLogger().info("- Has server manager: " + (PacketEvents.getAPI().getServerManager() != null));
            getLogger().info("- Has player manager: " + (PacketEvents.getAPI().getPlayerManager() != null));

        } catch (Exception e) {
            getLogger().severe("❌ Failed to initialize PacketEvents: " + e.getMessage());
            e.printStackTrace();
        }

        getLogger().info("=== PacketEvents Setup Complete ===");
    }

    /**
     * Register all listeners
     */
    private void registerListeners() {
        try {
            getLogger().info("=== Registering Listeners ===");

            // Register Bukkit inventory listener
            getServer().getPluginManager().registerEvents(new BukkitInventoryListener(this), this);

            getLogger().info("✅ Registered BukkitInventoryListener");
            getLogger().info("=== Listeners Registered ===");

        } catch (Exception e) {
            getLogger().severe("❌ Failed to register listeners: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Finish plugin initialization after a delay
     */
    private void finishInitialization() {
        int modelCount = modelManager.getModelCount();

        getLogger().info("=======================================");
        getLogger().info("ModelBrowser Plugin v" + getDescription().getVersion() + " Enabled!");
        getLogger().info("Loaded " + modelCount + " models");
        getLogger().info("Network: PacketEvents");
        getLogger().info("=======================================");
    }

    @Override
    public void onDisable() {
        // Stop web import server
        if (webImportServer != null) {
            webImportServer.stop();
        }

        // Shutdown PacketEvents communicator
        if (packetCommunicator != null) {
            packetCommunicator.shutdown();
        }

        // Clean up PacketEvents API
        if (PacketEvents.getAPI().isInitialized()) {
            PacketEvents.getAPI().terminate();
        }

        // Save any pending data
        if (modelManager != null) {
            modelManager.saveAll();
        }

        getLogger().info("ModelBrowser Plugin disabled!");
    }

    /**
     * Checks for plugin updates and logs version information.
     */
    private void checkUpdates() {
        if (config.getBoolean("logging.enabled", true)) {
            String version = getDescription().getVersion();
            getLogger().info("Running ModelBrowser v" + version);

            // Add update checker logic here if needed
            if (version.contains("SNAPSHOT")) {
                getLogger().warning("You are running a development build!");
            }
        }
    }

    /**
     * Reloads the plugin configuration and model data.
     */
    public void reloadPlugin() {
        reloadConfig();
        config = getConfig();

        if (modelManager != null) {
            modelManager.reload();
        }

        getLogger().info("Plugin reloaded!");
    }

    /**
     * Returns the singleton instance of the ModelBrowserPlugin.
     */
    public static ModelBrowserPlugin getInstance() {
        return instance;
    }

    /**
     * Returns the ModelManager instance for this plugin.
     */
    public ModelManager getModelManager() {
        return modelManager;
    }

    /**
     * Returns the GUIManager instance for this plugin.
     */
    public GUIManager getGUIManager() {
        return guiManager;
    }

    /**
     * Returns the PacketEventsCommunicator for network communication.
     */
    public PacketEventsCommunicator getPacketCommunicator() {
        return packetCommunicator;
    }

    /**
     * Returns the plugin configuration.
     */
    public FileConfiguration getPluginConfig() {
        return config;
    }

    /**
     * Send a test packet to verify PacketEvents communication
     */
    public void testNetwork(org.bukkit.entity.Player player) {
        if (packetCommunicator != null) {
            packetCommunicator.sendNotification(player, "Testing PacketEvents connection!");
            getLogger().info("Sent test packet via PacketEvents to " + player.getName());
        }
    }
}