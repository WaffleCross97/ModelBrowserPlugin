package com.debornmc.modelBrowserPlugin.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashSet;
import java.util.Set;

/**
 * Validates Minecraft 3D model JSON files for structural correctness and best practices.
 * <p>
 * This class provides validation methods to check if a JSON string represents a valid
 * Minecraft model according to Minecraft's model specification. It validates parent
 * references, element structures, textures, and common issues that might cause problems
 * in-game.
 * </p>
 * <p>
 * The validator includes both error checks (which make the model invalid) and warning
 * checks (which indicate potential issues but don't prevent model usage).
 * </p>
 */
public class ModelValidator {
    /** Set of known valid Minecraft model parent identifiers. */
    private static final Set<String> VALID_PARENTS = new HashSet<>();

    static {
        // Common Minecraft model parents
        VALID_PARENTS.add("block/block");
        VALID_PARENTS.add("block/cube");
        VALID_PARENTS.add("block/cube_all");
        VALID_PARENTS.add("block/cube_column");
        VALID_PARENTS.add("item/generated");
        VALID_PARENTS.add("item/handheld");
        VALID_PARENTS.add("builtin/entity");
    }

    /**
     * Constructs a new ModelValidator instance.
     * <p>
     * This constructor is private to prevent instantiation since all methods
     * in this class are static. This is a utility class that should be used
     * without creating instances.
     * </p>
     */
    private ModelValidator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Validates a Minecraft model JSON string.
     * <p>
     * This method performs comprehensive validation including:
     * - Basic JSON structure validation
     * - Parent reference validation against known Minecraft parents
     * - Element array structure validation
     * - Texture object validation
     * - Common issue detection (missing textures, unusual scales)
     * </p>
     *
     * @param json the JSON string to validate
     * @return a {@link ValidationResult} containing validation errors and warnings
     * @throws NullPointerException if the json parameter is null
     */
    public static ValidationResult validate(String json) {
        ValidationResult result = new ValidationResult();

        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            // Check for required fields
            if (!obj.has("parent") && !obj.has("elements") && !obj.has("textures")) {
                result.addError("Model must have at least one of: parent, elements, or textures");
            }

            // Validate parent if present
            if (obj.has("parent")) {
                String parent = obj.get("parent").getAsString();
                if (!isValidParent(parent)) {
                    result.addWarning("Unknown parent: " + parent);
                }
            }

            // Validate elements if present
            if (obj.has("elements")) {
                if (!obj.get("elements").isJsonArray()) {
                    result.addError("Elements must be an array");
                } else {
                    JsonArray elements = obj.get("elements").getAsJsonArray();
                    if (elements.size() > 100) {
                        result.addWarning("Large number of elements: " + elements.size());
                    }
                }
            }

            // Validate textures if present
            if (obj.has("textures")) {
                if (!obj.get("textures").isJsonObject()) {
                    result.addError("Textures must be an object");
                }
            }

            // Check for common issues
            checkCommonIssues(obj, result);

        } catch (Exception e) {
            result.addError("Invalid JSON: " + e.getMessage());
        }

        return result;
    }

    /**
     * Checks if a parent identifier is valid.
     * <p>
     * Valid parents include known Minecraft model parents (like "block/block") and
     * any parent starting with "minecraft:". Custom parents (not starting with
     * "minecraft:") are allowed but may generate warnings.
     * </p>
     *
     * @param parent the parent identifier to validate
     * @return true if the parent is valid, false otherwise
     */
    private static boolean isValidParent(String parent) {
        // Check if parent is a known Minecraft parent or starts with minecraft:
        if (parent.startsWith("minecraft:")) {
            String shortParent = parent.substring(10); // Remove "minecraft:"
            return VALID_PARENTS.contains(shortParent) || shortParent.contains("/");
        }
        return true; // Allow custom parents
    }

    /**
     * Checks for common model issues that might cause problems in-game.
     * <p>
     * This method detects issues such as:
     * - Elements defined without corresponding textures
     * - Unusually large or small scale transformations in display settings
     * </p>
     *
     * @param obj the parsed JSON object representing the model
     * @param result the ValidationResult to add warnings to
     */
    private static void checkCommonIssues(JsonObject obj, ValidationResult result) {
        // Check for missing textures in elements
        if (obj.has("elements") && !obj.has("textures")) {
            result.addWarning("Elements defined but no textures specified");
        }

        // Check for display transforms that might be too large
        if (obj.has("display")) {
            JsonObject display = obj.getAsJsonObject("display");
            for (String key : display.keySet()) {
                if (display.get(key).isJsonObject()) {
                    JsonObject transform = display.get(key).getAsJsonObject();
                    if (transform.has("scale")) {
                        // Check for extremely large scales
                        try {
                            float scale = transform.get("scale").getAsFloat();
                            if (scale > 10.0f || scale < 0.1f) {
                                result.addWarning("Unusual scale in display." + key + ": " + scale);
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                }
            }
        }
    }

    /**
     * Container class for model validation results.
     * <p>
     * This class stores both errors (which prevent model usage) and warnings
     * (which indicate potential issues but don't prevent usage). It provides
     * methods to check validity and retrieve formatted summaries.
     * </p>
     */
    public static class ValidationResult {
        private final Set<String> errors = new HashSet<>();
        private final Set<String> warnings = new HashSet<>();

        /**
         * Constructs a new ValidationResult with empty error and warning sets.
         * <p>
         * This constructor initializes a validation result container that can
         * be used to collect errors and warnings during model validation.
         * </p>
         */
        public ValidationResult() {
        }

        /**
         * Adds an error to the validation result.
         * <p>
         * Errors indicate problems that make the model invalid and unusable.
         * </p>
         *
         * @param error the error message to add
         */
        public void addError(String error) {
            errors.add(error);
        }

        /**
         * Adds a warning to the validation result.
         * <p>
         * Warnings indicate potential issues that don't prevent model usage
         * but might cause unexpected behavior.
         * </p>
         *
         * @param warning the warning message to add
         */
        public void addWarning(String warning) {
            warnings.add(warning);
        }

        /**
         * Checks if the model is valid (has no errors).
         *
         * @return true if there are no errors, false otherwise
         */
        public boolean isValid() {
            return errors.isEmpty();
        }

        /**
         * Checks if the model has any warnings.
         *
         * @return true if there are warnings, false otherwise
         */
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        /**
         * Returns a copy of all error messages.
         *
         * @return a Set containing all error messages
         */
        public Set<String> getErrors() {
            return new HashSet<>(errors);
        }

        /**
         * Returns a copy of all warning messages.
         *
         * @return a Set containing all warning messages
         */
        public Set<String> getWarnings() {
            return new HashSet<>(warnings);
        }

        /**
         * Generates a human-readable summary of validation results.
         * <p>
         * The summary includes counts and lists of all errors and warnings.
         * </p>
         *
         * @return a formatted string summarizing validation results
         */
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            if (!isValid()) {
                sb.append("Errors: ").append(errors.size());
                for (String error : errors) {
                    sb.append("\n- ").append(error);
                }
            }
            if (hasWarnings()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("Warnings: ").append(warnings.size());
                for (String warning : warnings) {
                    sb.append("\n- ").append(warning);
                }
            }
            return sb.toString();
        }
    }
}