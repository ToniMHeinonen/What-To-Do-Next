package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class GlobalPrefs {
    private static SharedPreferences prefs;

    private static String keyPrefs;
    private static String keyCurrentList = "current_list";

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
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(keyCurrentList, listID);
        editor.commit();
    }
}
