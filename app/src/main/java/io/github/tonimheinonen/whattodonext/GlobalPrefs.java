package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class GlobalPrefs {
    private static SharedPreferences prefs;

    private static String keyPrefs;
    private static String keyCurrentList = "current_list";
    private static String keyListVoteSize = "list_vote_size";

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

    public static int loadListVoteSize() {
        return prefs.getInt(keyListVoteSize, 7);
    }

    public static void saveListVoteSize(int listVoteSize) {
        prefs.edit().putInt(keyListVoteSize, listVoteSize).commit();
    }
}
