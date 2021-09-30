package io.github.tonimheinonen.whattodonext.tools;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Handles saving and loading values to local storage.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public abstract class GlobalPrefs {
    private static Context context;
    private static SharedPreferences prefs;

    private static String keyPrefs;

    // Lists
    private static String keyCurrentList = "current_list";

    // Popup information box
    private static String keyPopup = "popup_";
    public static final String PATCH_NOTES = "patch_notes";
    // Tutorial
    public static final String TUTORIAL_WELCOME = "welcome_tutorial";
    public static final String TUTORIAL_CREATE_LIST = "create_list_tutorial";
    public static final String TUTORIAL_ADD_ITEM = "add_item_tutorial";
    public static final String TUTORIAL_ITEM_INFO = "item_info_tutorial";
    public static final String TUTORIAL_VOTE_TYPE = "vote_type_tutorial";
    public static final String TUTORIAL_ONLINE_VOTE = "online_vote_tutorial";
    public static final String TUTORIAL_OFFLINE_VOTE = "offline_vote_tutorial";
    public static final String TUTORIAL_VOTE_TOP = "vote_top_tutorial";
    public static final String TUTORIAL_LAST_RESULTS = "last_results_tutorial";
    public static final String TUTORIAL_VOTE_COMPLETE = "vote_complete_tutorial";
    public static final String[] TUTORIAL_TEXTS = new String[] {
            TUTORIAL_WELCOME, TUTORIAL_CREATE_LIST, TUTORIAL_ADD_ITEM, TUTORIAL_ITEM_INFO,
            TUTORIAL_VOTE_TYPE, TUTORIAL_ONLINE_VOTE, TUTORIAL_OFFLINE_VOTE,
            TUTORIAL_VOTE_TOP, TUTORIAL_LAST_RESULTS, TUTORIAL_VOTE_COMPLETE};

    // Online
    private static String keyOnlineNickname = "online_nick_name";
    private static String keyOnlineRoomCode = "online_room_code";

    /**
     * Gets access to the correct prefs depending on the user.
     * @param aContext activity context
     */
    public static void initialize(Context aContext, String user) {
        context = aContext;
        keyPrefs = "WTDN_prefs_" + user;
        prefs = context.getSharedPreferences(keyPrefs, 0);
    }

    /**
     * Loads previously used list from memory.
     * @return id of the list
     */
    public static String loadCurrentList() {
        return prefs.getString(keyCurrentList, "");
    }

    /**
     * Saves current list as previously used list.
     * @param listID id of the list
     */
    public static void saveCurrentList(String listID) {
        prefs.edit().putString(keyCurrentList, listID).commit();
    }

    /**
     * Loads whether to show provided popup info type.
     * @return true if to show
     */
    public static boolean loadPopupInfo(String type) {
        return prefs.getBoolean(keyPopup + type, true);
    }

    /**
     * Saves whether to show provided popup info type.
     * @param show true if to show
     */
    public static void savePopupInfo(String type, boolean show) {
        prefs.edit().putBoolean(keyPopup + type, show).apply();
    }

    /**
     * Loads previously used nickname from memory.
     * @return nickname
     */
    public static String loadOnlineNickname() {
        return prefs.getString(keyOnlineNickname, "");
    }

    /**
     * Saves current nickname as previously used nickname.
     * @param nickname nickname
     */
    public static void saveOnlineNickname(String nickname) {
        prefs.edit().putString(keyOnlineNickname, nickname).apply();
    }

    /**
     * Loads previously used room code from memory.
     * @return room code
     */
    public static String loadOnlineRoomCode() {
        return prefs.getString(keyOnlineRoomCode, "");
    }

    /**
     * Saves current room code as previously used room code.
     * @param roomCode room code
     */
    public static void saveOnlineRoomCode(String roomCode) {
        prefs.edit().putString(keyOnlineRoomCode, roomCode).apply();
    }

    /**
     * Loads integer from prefs.
     * @param key key to load
     * @param defValue value if not found
     * @return loaded int or default value
     */
    public static int loadPreference(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    /**
     * Saves integer to prefs.
     * @param key key to save
     * @param value value to save
     */
    public static void savePreference(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }
}
