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

    // Settings
    private static String keyMaxPerilPoints = "max_peril_points";
    private static String keyListVoteSizeFirst = "list_vote_size_first";
    private static String keyListVoteSizeSecond = "list_vote_size_second";
    private static String keyIgnoreUnselected = "ignore_unselected";
    private static String keyHalveExtra = "halve_extra";
    private static String keyShowExtra = "show_extra";
    private static String keyShowVoted = "show_voted";

    // Popup information box
    private static String keyPopup = "popup_";
    public static final String BETA_ONLINE = "beta_online12537311";
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
     * Loads the first voting size of items.
     * @return voting size of items
     */
    public static int loadListVoteSizeFirst() {
        return prefs.getInt(keyListVoteSizeFirst, 7);
    }

    /**
     * Saves the first voting size of items.
     * @param listVoteSize voting size first top list
     */
    public static void saveListVoteSizeFirst(int listVoteSize) {
        prefs.edit().putInt(keyListVoteSizeFirst, listVoteSize).apply();
    }

    /**
     * Loads the second voting size of items.
     * @return voting size of items
     */
    public static int loadListVoteSizeSecond() {
        return prefs.getInt(keyListVoteSizeSecond, 5);
    }

    /**
     * Saves the second voting size of items.
     * @param listVoteSize voting size second top list
     */
    public static void saveListVoteSizeSecond(int listVoteSize) {
        prefs.edit().putInt(keyListVoteSizeSecond, listVoteSize).apply();
    }

    /**
     * Loads max peril points before item falls out from list.
     * @return max peril points
     */
    public static int loadMaxPerilPoints() {
        return prefs.getInt(keyMaxPerilPoints, 5);
    }

    /**
     * Saves the max peril points before item falls out from list.
     * @param maxPerilPoints max peril points
     */
    public static void saveMaxPerilPoints(int maxPerilPoints) {
        prefs.edit().putInt(keyMaxPerilPoints, maxPerilPoints).apply();
    }

    /**
     * Loads whether to ignore unselected when voting or not.
     * @return true if to ignore
     */
    public static boolean loadIgnoreUnselected() {
        return prefs.getBoolean(keyIgnoreUnselected, true);
    }

    /**
     * Saves whether to ignore unselected when voting or not.
     * @param ignore true if to ignore
     */
    public static void saveIgnoreUnselected(boolean ignore) {
        prefs.edit().putBoolean(keyIgnoreUnselected, ignore).apply();
    }

    /**
     * Loads whether to halve extra points in last vote or not.
     * @return true if to halve
     */
    public static boolean loadHalveExtra() {
        return prefs.getBoolean(keyHalveExtra, true);
    }

    /**
     * Saves whether to halve extra points in last vote or not.
     * @param halve true if to halve
     */
    public static void saveHalveExtra(boolean halve) {
        prefs.edit().putBoolean(keyHalveExtra, halve).apply();
    }

    /**
     * Loads whether to show extra points when voting or not.
     * @return true if to show
     */
    public static boolean loadShowExtra() {
        return prefs.getBoolean(keyShowExtra, true);
    }

    /**
     * Saves whether to show extra points when voting or not.
     * @param show true if to show
     */
    public static void saveShowExtra(boolean show) {
        prefs.edit().putBoolean(keyShowExtra, show).apply();
    }

    /**
     * Loads whether to show voted points in vote results or not.
     * @return true if to show
     */
    public static boolean loadShowVoted() {
        return prefs.getBoolean(keyShowVoted, true);
    }

    /**
     * Saves whether to show voted points in vote results or not.
     * @param show true if to show
     */
    public static void saveShowVoted(boolean show) {
        prefs.edit().putBoolean(keyShowVoted, show).apply();
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
}
