package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class GlobalPrefs {
    private static SharedPreferences prefs;

    private static String keyPrefs;
    private static String keyCurrentList = "current_list";
    private static String keyMaxPerilPoints = "max_peril_points";
    private static String keyListVoteSizeFirst = "list_vote_size_first";
    private static String keyListVoteSizeSecond = "list_vote_size_second";

    /**
     * Gets access to the correct prefs depending on the user.
     * @param context activity context
     */
    public static void initialize(Context context, String user) {
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
}
