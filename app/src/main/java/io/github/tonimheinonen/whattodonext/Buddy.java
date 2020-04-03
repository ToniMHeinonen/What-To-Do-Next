package io.github.tonimheinonen.whattodonext;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;

public abstract class Buddy {

    private static Context context;

    /**
     * Gets application context.
     * @param host context
     */
    public static void initialize(Context host) {
        context = host;
    }

    /**
     * Hides keyboard, clears focus on view and clears it's text.
     *
     * Notice that in order for this to work, you need to add android:focusable="true" &
     * android:focusableInTouchMode="true" to current view's parent layout.
     * @param editTextView
     */
    public static void hideKeyboardAndClear(EditText editTextView) {
        editTextView.clearFocus();
        editTextView.setText("");
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextView.getWindowToken(), 0);
    }

    /**
     * Shows long toast text.
     * @param text message to show
     */
    public static void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    /**
     * Return saved string from strings.xml.
     * @param stringID id of the string
     * @return saved string
     */
    public static String getString(int stringID) {
        return context.getResources().getString(stringID);
    }

    /**
     * Sorts ListItem items by name.
     * @param items items to sort
     * @param ascending sort ascending or descending
     */
    public static void sortItemsByName(ArrayList<ListItem> items, final boolean ascending) {
        Collections.sort(items, new Comparator<ListItem>() {
            public int compare(ListItem o1, ListItem o2) {
                return ascending ?
                        o1.getName().compareTo(o2.getName()) :
                        o2.getName().compareTo(o1.getName());
            }
        });
    }

    /**
     * Filters ListItem list by fallen status.
     * @param items items to filter
     * @param getFallen whether to get fallen or not fallen
     */
    public static void filterListByFallen(ArrayList<ListItem> items, boolean getFallen) {
        Iterator<ListItem> i = items.iterator();
        while (i.hasNext()) {
            ListItem item = i.next();

            // If item fallen is not same as parameter fallen, remove from list
            if (item.isFallen() != getFallen)
                i.remove();
        }
    }

    /**
     * Creates Alert Dialog when exiting voting.
     * @param activity
     */
    public static void exitVoting(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(getString(R.string.alert_exit_title))
                .setMessage(getString(R.string.alert_exit_message))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(getString(R.string.alert_exit_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(new Intent(activity, StartVoteActivity.class));
                        activity.finish();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(getString(R.string.alert_exit_no),  null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
