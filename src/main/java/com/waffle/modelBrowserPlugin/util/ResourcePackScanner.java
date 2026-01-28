package com.waffle.modelBrowserPlugin.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * EXTREME STRICT scanner - ONLY items/ folder, NO models/ EVER
 */
public class ResourcePackScanner {

    public static List<String> scanResourcePack(Path resourcePackPath) throws IOException {
        List<String> modelIds = new ArrayList<>();

        // 1. Build EXACT path to items/ folder
        Path itemsDir = resourcePackPath
                .resolve("assets")
                .resolve("minecraft")
                .resolve("items");

        System.out.println("SCANNER: Targeting ONLY: " + itemsDir);

        // 2. If items/ doesn't exist, STOP HERE
        if (!Files.exists(itemsDir)) {
            System.out.println("SCANNER: Items folder not found - STOPPING");
            return modelIds; // EMPTY
        }

        // 3. List ONLY .json files in THAT EXACT FOLDER
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(itemsDir, "*.json")) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                String modelName = fileName.replace(".json", "");
                modelIds.add("minecraft:" + modelName);
                System.out.println("SCANNER: Found in items/: " + modelName);
            }
        }

        System.out.println("SCANNER: Total found: " + modelIds.size());

        return modelIds;
    }

    /**
     * NUCLEAR OPTION: Manual file listing - no walk, no recursion
     */
    public static List<String> scanItemsFolderNuclear(Path resourcePackPath) {
        List<String> modelIds = new ArrayList<>();

        try {
            // Build path like: C:/resourcepacks/myPack/assets/minecraft/items/
            Path itemsPath = Paths.get(
                    resourcePackPath.toString(),
                    "assets", "minecraft", "items"
            );

            System.out.println("NUCLEAR SCAN: Looking at: " + itemsPath);

            if (!Files.exists(itemsPath)) {
                return modelIds;
            }

            // Get ALL files in that folder
            String[] files = itemsPath.toFile().list();
            if (files == null) return modelIds;

            for (String file : files) {
                if (file.endsWith(".json")) {
                    String modelName = file.substring(0, file.length() - 5);
                    modelIds.add("minecraft:" + modelName);
                    System.out.println("NUCLEAR: Found: " + modelName);
                }
            }

        } catch (Exception e) {
            System.err.println("Nuclear scan error: " + e.getMessage());
        }

        return modelIds;
    }

    /**
     * PRIVATE: Internal scan without logging for filter method
     */
    private static List<String> scanItemsSilently(Path resourcePackPath) throws IOException {
        List<String> modelIds = new ArrayList<>();
        Path itemsDir = resourcePackPath
                .resolve("assets")
                .resolve("minecraft")
                .resolve("items");

        if (!Files.exists(itemsDir)) {
            return modelIds;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(itemsDir, "*.json")) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                String modelName = fileName.replace(".json", "");
                modelIds.add("minecraft:" + modelName);
            }
        }
        return modelIds;
    }

    /**
     * FILTER: Remove all models that aren't from the items/ folder
     * Use this when your UI shows too many models
     */
    public static List<String> filterToItemsOnly(List<String> allModels, Path resourcePackPath) {
        List<String> filtered = new ArrayList<>();

        try {
            // Get the ACTUAL models from items/ folder (silent version)
            List<String> itemsModels = scanItemsSilently(resourcePackPath);
            Set<String> itemsSet = new HashSet<>(itemsModels);

            System.out.println("=== FILTERING MODELS ===");
            System.out.println("Total models before filter: " + allModels.size());
            System.out.println("Models in items/ folder: " + itemsModels.size());

            // Keep ONLY models that exist in items/ folder
            for (String model : allModels) {
                if (itemsSet.contains(model)) {
                    filtered.add(model);
                    System.out.println("✓ Keeping (from items/): " + model);
                } else {
                    System.out.println("✗ Removing (not in items/): " + model);
                }
            }

            System.out.println("Total models after filter: " + filtered.size());
            System.out.println("=========================");
        } catch (IOException e) {
            System.err.println("Filter error: " + e.getMessage());
        }

        return filtered;
    }

    /**
     * Quick check: Is this model from items/ folder?
     */
    public static boolean isFromItemsFolder(String modelId, Path resourcePackPath) {
        if (modelId == null || !modelId.startsWith("minecraft:")) {
            return false;
        }

        String modelName = modelId.replace("minecraft:", "");
        Path expectedFile = resourcePackPath
                .resolve("assets")
                .resolve("minecraft")
                .resolve("items")
                .resolve(modelName + ".json");

        return Files.exists(expectedFile);
    }

    /**
     * Debug method to see what's being loaded vs what's in items/
     */
    public static void debugModelSources(List<String> loadedModels, Path resourcePackPath) {
        System.out.println("\n=== DEBUG MODEL SOURCES ===");
        System.out.println("Currently loaded models: " + loadedModels.size());

        try {
            List<String> itemsModels = scanItemsSilently(resourcePackPath);
            System.out.println("Models in items/ folder: " + itemsModels.size());

            System.out.println("\n--- Items/ folder models ---");
            for (String model : itemsModels) {
                System.out.println("  " + model);
            }
        } catch (IOException e) {
            System.out.println("Error scanning items folder: " + e.getMessage());
        }

        System.out.println("\n--- All loaded models ---");
        for (int i = 0; i < loadedModels.size(); i++) {
            String model = loadedModels.get(i);
            boolean isFromItems = isFromItemsFolder(model, resourcePackPath);
            System.out.printf("%2d. %s %s%n",
                    i + 1,
                    model,
                    isFromItems ? "[FROM ITEMS/]" : "[OTHER SOURCE]"
            );
        }
        System.out.println("===========================\n");
    }
}