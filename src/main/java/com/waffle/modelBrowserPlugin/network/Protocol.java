package com.waffle.modelBrowserPlugin.network;

/**
 * Defines protocol constants for Model Browser plugin messaging.
 * <p>
 * This class contains string constants representing message types for
 * communication between the server and Model Browser clients. The protocol
 * is divided into client-to-server requests and server-to-client responses.
 * </p>
 * <p>
 * All message types are documented to indicate their purpose and direction.
 * </p>
 */
public class Protocol {
    /**
     * Constructs a new Protocol instance.
     * <p>
     * This constructor is private to prevent instantiation since this class
     * only contains static constants. This is a utility class that should
     * be used without creating instances.
     * </p>
     */
    private Protocol() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Client -> Server requests

    /** Request the list of all available models. */
    public static final String REQUEST_MODELS = "request_models";

    /** Request the JSON data for a specific model. */
    public static final String REQUEST_MODEL = "request_model";

    /** Upload a new model to the server. */
    public static final String UPLOAD_MODEL = "upload_model";

    /** Delete a model from the server. */
    public static final String DELETE_MODEL = "delete_model";

    /** Search for models by name. */
    public static final String SEARCH_MODELS = "search_models";

    /** Request the list of all model categories. */
    public static final String GET_CATEGORIES = "get_categories";

    /** Request models belonging to a specific category. */
    public static final String GET_MODELS_BY_CATEGORY = "get_models_by_category";

    /** Request detailed information about a specific model. */
    public static final String GET_MODEL_INFO = "get_model_info";

    /** Test connection to the server. */
    public static final String PING = "ping";

    // Server -> Client responses

    /** Response containing the list of all models. */
    public static final String MODEL_LIST = "model_list";

    /** Response containing a specific model's JSON data. */
    public static final String MODEL_DATA = "model_data";

    /** Response to an upload request. */
    public static final String UPLOAD_RESPONSE = "upload_response";

    /** Response to a delete request. */
    public static final String DELETE_RESPONSE = "delete_response";

    /** Response containing search results. */
    public static final String SEARCH_RESULTS = "search_results";

    /** Response containing the list of all categories. */
    public static final String CATEGORY_LIST = "category_list";

    /** Response containing models in a specific category. */
    public static final String CATEGORY_MODELS = "category_models";

    /** Response containing detailed model information. */
    public static final String MODEL_INFO = "model_info";

    /** Command to open the model browser interface. */
    public static final String OPEN_BROWSER = "open_browser";

    /** General notification message to display to the player. */
    public static final String NOTIFICATION = "notification";

    /** Response to a ping request. */
    public static final String PONG = "pong";

    // Error messages

    /** General error message type. */
    public static final String ERROR = "error";

    /** Error indicating the player lacks required permissions. */
    public static final String ERROR_NO_PERMISSION = "no_permission";

    /** Error indicating invalid JSON data. */
    public static final String ERROR_INVALID_JSON = "invalid_json";

    /** Error indicating the requested model was not found. */
    public static final String ERROR_MODEL_NOT_FOUND = "model_not_found";

    /** Error indicating the model data exceeds size limits. */
    public static final String ERROR_TOO_LARGE = "too_large";

    /** Error indicating an invalid model name. */
    public static final String ERROR_INVALID_NAME = "invalid_name";

    /** Error indicating the model already exists and overwrite was not specified. */
    public static final String ERROR_ALREADY_EXISTS = "already_exists";
}