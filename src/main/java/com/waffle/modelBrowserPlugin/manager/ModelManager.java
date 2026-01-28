package com.waffle.modelBrowserPlugin.manager;

import com.waffle.modelBrowserPlugin.ModelBrowserPlugin;
import com.waffle.modelBrowserPlugin.util.ResourcePackScanner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelManager {

    private final ModelBrowserPlugin plugin;
    private final List<String> availableModels = new ArrayList<>();
    private final Map<String, Long> modelSizes = new HashMap<>();
    private final Map<String, String> modelCategories = new HashMap<>();
    private final Map<String, Long> modelTimestamps = new HashMap<>();
    private final Gson gson = new Gson();

    public ModelManager(ModelBrowserPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reload all models from resource pack
     */
    public void reload() {
        availableModels.clear();
        modelSizes.clear();
        modelCategories.clear();
        modelTimestamps.clear();

        // Get resource pack folder from config
        FileConfiguration config = plugin.getConfig();
        String resourcePackPath = config.getString("resource-pack.path", "resourcepack");

        File resourcePackDir = new File(plugin.getDataFolder(), resourcePackPath);

        if (!resourcePackDir.exists()) {
            plugin.getLogger().warning("Resource pack directory not found: " + resourcePackDir.getPath());
            plugin.getLogger().warning("Creating example directory structure...");
            createExampleStructure(resourcePackDir);
            return;
        }

        // Scan for models using the CORRECT scanner (items/ folder)
        scanForModels(resourcePackDir);

        plugin.getLogger().info("Loaded " + availableModels.size() + " models from resource pack");
    }

    /**
     * Scan resource pack directory for models - FIXED VERSION
     * Now scans items/ folder instead of models/ folder
     */
    private void scanForModels(File resourcePackDir) {
        try {
            // Use the ResourcePackScanner to get ONLY items/ folder models
            Path packPath = resourcePackDir.toPath();
            List<String> itemsModels = ResourcePackScanner.scanResourcePack(packPath);

            // Process each model found in items/ folder
            for (String modelId : itemsModels) {
                // Extract the model name (remove "minecraft:")
                String modelName = modelId.replace("minecraft:", "");

                // Build the file path
                Path modelFile = packPath
                        .resolve("assets")
                        .resolve("minecraft")
                        .resolve("items")
                        .resolve(modelName + ".json");

                if (Files.exists(modelFile)) {
                    processModelFile(modelFile, modelId);
                }
            }

        } catch (IOException e) {
            plugin.getLogger().severe("Error scanning for models: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Process a single model JSON file from items/ folder
     */
    private void processModelFile(Path modelFile, String modelId) {
        try {
            // Read file size and timestamp
            long fileSize = Files.size(modelFile);
            long lastModified = Files.getLastModifiedTime(modelFile).toMillis();

            // Parse JSON to get more info
            JsonObject json = gson.fromJson(new FileReader(modelFile.toFile()), JsonObject.class);

            // Determine category based on model type
            String category = determineCategoryFromJson(json);

            // Store model info
            availableModels.add(modelId);
            modelSizes.put(modelId, fileSize);
            modelCategories.put(modelId, category);
            modelTimestamps.put(modelId, lastModified);

            plugin.getLogger().fine("Found item model: " + modelId + " (" + category + ")");

        } catch (Exception e) {
            plugin.getLogger().warning("Error processing model file " + modelFile + ": " + e.getMessage());
        }
    }

    /**
     * Determine category based on JSON content (for items/ folder)
     */
    private String determineCategoryFromJson(JsonObject json) {
        if (json == null || !json.has("model")) {
            return "unknown";
        }

        JsonObject modelObj = json.getAsJsonObject("model");
        if (modelObj.has("type")) {
            String type = modelObj.get("type").getAsString();

            if (type.contains("condition")) {
                return "shields"; // Conditional models are usually shields
            }

            if (type.contains("select")) {
                return "selectable";
            }
        }

        // Check referenced models for clues
        String jsonString = json.toString().toLowerCase();
        if (jsonString.contains("sword") || jsonString.contains("axe")) {
            return "weapons";
        }
        if (jsonString.contains("shield")) {
            return "shields";
        }
        if (jsonString.contains("pickaxe")) {
            return "tools";
        }
        if (jsonString.contains("hat") || jsonString.contains("helmet")) {
            return "armor";
        }

        return "items";
    }

    /**
     * Create example directory structure for items/ folder
     */
    private void createExampleStructure(File resourcePackDir) {
        try {
            // Create items folder structure
            File itemsDir = new File(resourcePackDir, "assets/minecraft/items");
            if (itemsDir.mkdirs()) {
                plugin.getLogger().info("Created items directory: " + itemsDir.getPath());

                // Create example item model definitions
                createExampleItemModel(itemsDir, "example_sword.json", "sword");
                createExampleItemModel(itemsDir, "example_shield.json", "shield");
                createExampleItemModel(itemsDir, "example_tool.json", "tool");

                // Create pack.mcmeta
                createPackMcmeta(resourcePackDir);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create example structure: " + e.getMessage());
        }
    }

    /**
     * Create an example item model file (for items/ folder)
     */
    private void createExampleItemModel(File itemsDir, String fileName, String type) throws IOException {
        File modelFile = new File(itemsDir, fileName);

        String json;
        if (type.equals("shield")) {
            json = """
            {
              "model": {
                "type": "minecraft:condition",
                "property": "minecraft:using_item",
                "on_true": {
                  "type": "minecraft:model",
                  "model": "minecraft:item/example_shield_blocking"
                },
                "on_false": {
                  "type": "minecraft:model",
                  "model": "minecraft:item/example_shield"
                }
              }
            }
            """;
        } else if (type.equals("sword")) {
            json = """
            {
              "model": {
                "type": "minecraft:model",
                "model": "minecraft:item/example_sword"
              }
            }
            """;
        } else {
            json = """
            {
              "model": {
                "type": "minecraft:model",
                "model": "minecraft:item/example_tool"
              }
            }
            """;
        }

        Files.write(modelFile.toPath(), json.getBytes());
        plugin.getLogger().info("Created example item model: " + fileName);
    }

    /**
     * Create pack.mcmeta file
     */
    private void createPackMcmeta(File resourcePackDir) throws IOException {
        File mcmetaFile = new File(resourcePackDir, "pack.mcmeta");
        String mcmeta = """
        {
          "pack": {
            "pack_format": 22,
            "description": "Example Resource Pack for Model Browser"
          }
        }
        """;

        Files.write(mcmetaFile.toPath(), mcmeta.getBytes());
    }

    // =============== PUBLIC API ===============

    /**
     * Get all available models
     */
    public List<String> getAvailableModels() {
        return new ArrayList<>(availableModels);
    }

    /**
     * Get model count
     */
    public int getModelCount() {
        return availableModels.size();
    }

    /**
     * Get model file size in bytes
     */
    public long getModelSize(String modelName) {
        return modelSizes.getOrDefault(modelName, 0L);
    }

    /**
     * Get model category
     */
    public String getModelCategory(String modelName) {
        return modelCategories.getOrDefault(modelName, "unknown");
    }

    /**
     * Search for models
     */
    public List<String> searchModels(String query) {
        List<String> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (String model : availableModels) {
            if (model.toLowerCase().contains(lowerQuery)) {
                results.add(model);
            }
        }

        return results;
    }

    /**
     * Get model info
     */
    public Map<String, Object> getModelInfo(String modelName) {
        Map<String, Object> info = new HashMap<>();

        if (availableModels.contains(modelName)) {
            info.put("exists", true);
            info.put("name", modelName);
            info.put("size", modelSizes.get(modelName));
            info.put("category", modelCategories.get(modelName));
            info.put("lastModified", modelTimestamps.get(modelName));
        } else {
            info.put("exists", false);
        }

        return info;
    }

    /**
     * Get all categories
     */
    public Map<String, List<String>> getCategories() {
        Map<String, List<String>> categories = new HashMap<>();

        for (String model : availableModels) {
            String category = modelCategories.get(model);
            categories.computeIfAbsent(category, k -> new ArrayList<>()).add(model);
        }

        return categories;
    }

    /**
     * Delete a model
     */
    public boolean deleteModel(String modelName) {
        // TODO: Implement model deletion
        plugin.getLogger().warning("Model deletion not implemented yet for: " + modelName);
        return false;
    }

    /**
     * Save all data
     */
    public void saveAll() {
        // Save any cached data if needed
    }

    /**
     * DEBUG: Show what models are loaded and from where
     */
    public void debugLoadedModels() {
        plugin.getLogger().info("=== DEBUG: Loaded Models ===");
        plugin.getLogger().info("Total models: " + availableModels.size());

        for (String model : availableModels) {
            plugin.getLogger().info("- " + model + " [" + modelCategories.get(model) + "]");
        }

        // Also show what the ResourcePackScanner finds
        try {
            FileConfiguration config = plugin.getConfig();
            String resourcePackPath = config.getString("resource-pack.path", "resourcepack");
            File resourcePackDir = new File(plugin.getDataFolder(), resourcePackPath);

            List<String> scannedModels = ResourcePackScanner.scanResourcePack(resourcePackDir.toPath());
            plugin.getLogger().info("=== DEBUG: ResourcePackScanner found ===");
            plugin.getLogger().info("Scanned models: " + scannedModels.size());
            for (String model : scannedModels) {
                plugin.getLogger().info("- " + model);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Debug error: " + e.getMessage());
        }
    }
}