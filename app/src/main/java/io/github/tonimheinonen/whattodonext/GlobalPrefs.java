package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class GlobalPrefs {
    private static SharedPreferences prefs;

    private static String keyPrefs;
    private static String keyCurrentList = "current_list";
    private static String keyListVoteSizeFirst = "list_vote_size_first";
    private static String keyListVoteSizeSecond = "list_vote_size_second";

    /**
     * Loads the saved values from file.
     * @param context activity context
     */
    public static void initialize(Context context, String user) {
        keyPrefs = "WTDN_prefs_" + user;
        prefs = context.getSharedPreferences(keyPrefs, 0);
    }

    public static String loadCurrentList() {
        return prefs.getString(keyCurrentList, "");
    }

    public static void saveCurrentList(String listID) {
        prefs.edit().putString(keyCurrentList, listID).commit();
    }

    public static int loadListVoteSizeFirst() {
        return prefs.getInt(keyListVoteSizeFirst, 7);
    }

    public static void saveListVoteSizeFirst(int listVoteSize) {
        prefs.edit().putInt(keyListVoteSizeFirst, listVoteSize).apply();
    }

    public static int loadListVoteSizeSecond() {
        return prefs.getInt(keyListVoteSizeSecond, 5);
    }

    public static void saveListVoteSizeSecond(int listVoteSize) {
        prefs.edit().putInt(keyListVoteSizeSecond, listVoteSize).apply();
    }
}
