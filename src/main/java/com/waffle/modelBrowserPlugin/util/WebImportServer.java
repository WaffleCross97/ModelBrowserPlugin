package com.waffle.modelBrowserPlugin.util;

import com.waffle.modelBrowserPlugin.ModelBrowserPlugin;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Very small HTTP server that accepts model imports from the web UI.
 *
 * Endpoint: POST /model-import
 * Payload (JSON):
 * {
 *   "id": "waffle_shield",
 *   "modelJson": "{ ... }",
 *   "texturePngBase64": "iVBORw0KGgoAAA..."   // raw base64, no data: prefix
 * }
 *
 * Files are written into the plugin's resource pack folder:
 * - assets/minecraft/models/item/<id>.json
 * - assets/minecraft/textures/item/<id>.png
 * - assets/minecraft/items/<id>.json (wrapper, if missing)
 *
 * CORS is enabled so you can host the web UI separately (e.g. GitHub Pages).
 */
public class WebImportServer {

    private final ModelBrowserPlugin plugin;
    private final Gson gson = new Gson();
    private HttpServer server;

    public WebImportServer(ModelBrowserPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        try {
            FileConfiguration cfg = plugin.getPluginConfig();
            int port = cfg.getInt("web-import.port", 8123);

            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.createContext("/model-import", new ImportHandler());
            server.setExecutor(null); // default executor
            server.start();

            plugin.getLogger().info("WebImportServer listening on port " + port + " at /model-import");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start WebImportServer: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            plugin.getLogger().info("WebImportServer stopped.");
        }
    }

    private class ImportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Basic CORS handling
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "OPTIONS, POST");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendPlain(exchange, 405, "Method Not Allowed");
                return;
            }

            try (InputStream is = exchange.getRequestBody()) {
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = gson.fromJson(body, JsonObject.class);

                if (json == null || !json.has("id") || !json.has("modelJson") || !json.has("texturePngBase64")) {
                    sendPlain(exchange, 400, "Missing required fields");
                    return;
                }

                String id = json.get("id").getAsString().trim();
                String modelJson = json.get("modelJson").getAsString();
                String textureBase64 = json.get("texturePngBase64").getAsString();

                if (id.isEmpty()) {
                    sendPlain(exchange, 400, "Empty id");
                    return;
                }

                byte[] textureBytes;
                try {
                    textureBytes = Base64.getDecoder().decode(textureBase64);
                } catch (IllegalArgumentException e) {
                    sendPlain(exchange, 400, "Invalid base64 for texture");
                    return;
                }

                // Write files into the resource pack folder
                FileConfiguration cfg = plugin.getPluginConfig();
                String resourcePackPath = cfg.getString("resource-pack.path", "resourcepack");
                Path packRoot = plugin.getDataFolder().toPath().resolve(resourcePackPath);

                Path modelsItem = packRoot.resolve(Paths.get("assets", "minecraft", "models", "item"));
                Path texturesItem = packRoot.resolve(Paths.get("assets", "minecraft", "textures", "item"));
                Path items = packRoot.resolve(Paths.get("assets", "minecraft", "items"));

                Files.createDirectories(modelsItem);
                Files.createDirectories(texturesItem);
                Files.createDirectories(items);

                Path modelFile = modelsItem.resolve(id + ".json");
                Path textureFile = texturesItem.resolve(id + ".png");
                Path itemFile = items.resolve(id + ".json");

                Files.writeString(modelFile, modelJson, StandardCharsets.UTF_8);
                Files.write(textureFile, textureBytes);

                if (!Files.exists(itemFile)) {
                    String wrapper = """
                            {
                              "model": {
                                "type": "minecraft:model",
                                "model": "minecraft:item/%s"
                              }
                            }
                            """.formatted(id);
                    Files.writeString(itemFile, wrapper, StandardCharsets.UTF_8);
                }

                plugin.getLogger().info("Imported model via web: " + id);

                // Respond with JSON
                JsonObject resp = new JsonObject();
                resp.addProperty("status", "ok");
                resp.addProperty("id", id);
                sendJson(exchange, 200, resp.toString());
            } catch (Exception e) {
                plugin.getLogger().warning("Web import failed: " + e.getMessage());
                sendPlain(exchange, 500, "Internal server error");
            }
        }

        private void sendPlain(HttpExchange exchange, int code, String msg) throws IOException {
            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private void sendJson(HttpExchange exchange, int code, String json) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "application/json");
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}

