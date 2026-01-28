package com.waffle.modelBrowserPlugin.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Utility class providing JSON manipulation and validation methods.
 * <p>
 * This class offers functionality for pretty-printing, minifying, validating,
 * and categorizing JSON strings. It uses Google's Gson library for JSON processing.
 * </p>
 * <p>
 * Note: This is a utility class and should not be instantiated.
 * </p>
 *
 * @author Model Browser Plugin Team
 * @since 1.0
 */
public class JsonUtil {
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson COMPACT_GSON = new Gson();

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * This constructor is private to enforce the static nature of the utility methods.
     * All methods in this class are static and should be called without creating an instance.
     * </p>
     */
    private JsonUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a JSON string into a human-readable, formatted version.
     * <p>
     * This method takes a JSON string and returns a formatted version with proper
     * indentation and line breaks. If the input is not valid JSON, the original
     * string is returned unchanged.
     * </p>
     *
     * @param json the JSON string to format (may be minified or already formatted)
     * @return a pretty-printed JSON string, or the original string if formatting fails
     * @see #minify(String) for the inverse operation
     */
    public static String prettyPrint(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            return PRETTY_GSON.toJson(jsonObject);
        } catch (Exception e) {
            return json;
        }
    }

    /**
     * Converts a JSON string into a minified/compact version.
     * <p>
     * This method removes all unnecessary whitespace from a JSON string to
     * reduce its size. If the input is not valid JSON, the original string
     * is returned unchanged.
     * </p>
     *
     * @param json the JSON string to minify (may be pretty-printed or already compact)
     * @return a compact JSON string with no extra whitespace, or the original string if minification fails
     * @see #prettyPrint(String) for the inverse operation
     */
    public static String minify(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            return COMPACT_GSON.toJson(jsonObject);
        } catch (Exception e) {
            return json;
        }
    }

    /**
     * Validates whether a string contains valid JSON syntax.
     * <p>
     * This method attempts to parse the input string as JSON. If parsing succeeds,
     * the string is considered valid JSON. The method does not validate against
     * any specific JSON schema, only basic syntax.
     * </p>
     *
     * @param json the string to validate as JSON
     * @return {@code true} if the string is valid JSON, {@code false} otherwise
     */
    public static boolean isValidJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Determines the type of JSON structure based on its content.
     * <p>
     * This method analyzes the JSON object's structure to categorize it into
     * specific types used within the Model Browser Plugin. It checks for
     * specific key patterns to determine the JSON type.
     * </p>
     *
     * @param json the JSON string to analyze
     * @return a string representing the JSON type:
     *         <ul>
     *           <li>"parented" - contains a "parent" field</li>
     *           <li>"custom" - contains an "elements" field</li>
     *           <li>"textured" - contains a "textures" field</li>
     *           <li>"display" - contains a "display" field</li>
     *           <li>"unknown" - valid JSON but doesn't match known patterns</li>
     *           <li>"invalid" - not valid JSON</li>
     *         </ul>
     */
    public static String getJsonType(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            if (obj.has("parent")) {
                return "parented";
            } else if (obj.has("elements")) {
                return "custom";
            } else if (obj.has("textures")) {
                return "textured";
            } else if (obj.has("display")) {
                return "display";
            }

            return "unknown";
        } catch (Exception e) {
            return "invalid";
        }
    }
}