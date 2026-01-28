package com.waffle.modelBrowserPlugin.util;

import com.waffle.modelBrowserPlugin.ModelBrowserPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Copies resource-pack files from a local "imports" folder into the plugin's resource pack,
 * and auto-creates missing items/ JSON wrappers for models/item/*.json so ModelManager can see them.
 *
 * Expected import structure (recommended):
 * - plugins/ModelBrowser/imports/assets/minecraft/models/item/<id>.json
 * - plugins/ModelBrowser/imports/assets/minecraft/textures/item/<id>.png
 *
 * You can also place items/ json directly; it will be copied as-is.
 */
public final class ResourcePackImporter {

    private ResourcePackImporter() {}

    public record ImportResult(
            Path importRoot,
            Path packRoot,
            int copiedFiles,
            int createdItemWrappers,
            List<String> createdIds
    ) {}

    public static ImportResult importFromLocalFolder(ModelBrowserPlugin plugin) throws IOException {
        FileConfiguration config = plugin.getConfig();
        String resourcePackPath = config.getString("resource-pack.path", "resourcepack");

        Path packRoot = plugin.getDataFolder().toPath().resolve(resourcePackPath);
        Path importRoot = plugin.getDataFolder().toPath().resolve("imports");

        if (!Files.exists(importRoot)) {
            Files.createDirectories(importRoot);
            // Nothing to import yet.
            return new ImportResult(importRoot, packRoot, 0, 0, List.of());
        }

        // Ensure common pack directories exist.
        Files.createDirectories(packRoot.resolve(Paths.get("assets", "minecraft", "items")));
        Files.createDirectories(packRoot.resolve(Paths.get("assets", "minecraft", "models", "item")));
        Files.createDirectories(packRoot.resolve(Paths.get("assets", "minecraft", "textures", "item")));

        int copied = 0;

        // Copy the entire assets/ tree if present, preserving structure.
        Path importAssets = importRoot.resolve("assets");
        if (Files.exists(importAssets)) {
            copied += copyRecursive(importAssets, packRoot.resolve("assets"));
        }

        // Also support dropping files directly into imports/ (rare but convenient).
        copied += copyLooseFiles(importRoot, packRoot);

        // Auto-create items wrappers for imported model jsons
        Path packModelsItem = packRoot.resolve(Paths.get("assets", "minecraft", "models", "item"));
        Path packItems = packRoot.resolve(Paths.get("assets", "minecraft", "items"));

        int createdWrappers = 0;
        List<String> createdIds = new ArrayList<>();

        if (Files.exists(packModelsItem)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(packModelsItem, "*.json")) {
                for (Path modelJson : stream) {
                    String fileName = modelJson.getFileName().toString();
                    String id = fileName.substring(0, fileName.length() - 5); // strip .json

                    // Skip blocking variants; those are referenced by the base item.
                    if (id.endsWith("_blocking")) continue;

                    Path itemJson = packItems.resolve(id + ".json");
                    if (Files.exists(itemJson)) continue;

                    String wrapper = """
                            {
                              "model": {
                                "type": "minecraft:model",
                                "model": "minecraft:item/%s"
                              }
                            }
                            """.formatted(id);

                    Files.writeString(itemJson, wrapper, StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    createdWrappers++;
                    createdIds.add("minecraft:" + id);
                }
            }
        }

        return new ImportResult(importRoot, packRoot, copied, createdWrappers, createdIds);
    }

    private static int copyLooseFiles(Path importRoot, Path packRoot) throws IOException {
        int copied = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(importRoot)) {
            for (Path child : stream) {
                if (Files.isDirectory(child)) continue;
                // Don't overwrite pack.mcmeta by accident unless user explicitly supplies assets tree.
                Path target = packRoot.resolve(child.getFileName().toString());
                Files.copy(child, target, StandardCopyOption.REPLACE_EXISTING);
                copied++;
            }
        }
        return copied;
    }

    private static int copyRecursive(Path sourceRoot, Path targetRoot) throws IOException {
        final int[] copied = {0};
        Files.walk(sourceRoot).forEach(source -> {
            try {
                Path rel = sourceRoot.relativize(source);
                Path target = targetRoot.resolve(rel);
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    copied[0]++;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return copied[0];
    }
}

