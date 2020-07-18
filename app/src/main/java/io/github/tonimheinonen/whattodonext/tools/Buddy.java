package io.github.tonimheinonen.whattodonext.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import io.github.tonimheinonen.whattodonext.InitializeActivity;
import io.github.tonimheinonen.whattodonext.ListItemFragment;
import io.github.tonimheinonen.whattodonext.MainActivity;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;

/**
 * Handles often used helpful methods.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public abstract class Buddy {

    public static final String FIREBASE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:sss'Z'";

    /**
     * Hides keyboard, clears focus on view and clears it's text.
     *
     * Notice that in order for this to work, you need to add android:focusable="true" &
     * android:focusableInTouchMode="true" to current view's parent layout.
     * @param editTextView view to clear focus
     * @param clearText true if to reset text field to ""
     */
    public static void hideKeyboardAndClear(EditText editTextView, boolean clearText) {
        Context context = InitializeActivity.getAppContext();
        editTextView.clearFocus();
        if (clearText)
            editTextView.setText("");

        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextView.getWindowToken(), 0);
    }

    /**
     * Shows long toast text.
     * @param text message to show
     * @param duration either Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     */
    public static void showToast(String text, int duration) {
        Context context = InitializeActivity.getAppContext();
        Toast toast = Toast.makeText(context, text, duration);
        View view = toast.getView();

        // Gets the actual oval background of the Toast then sets the colour filter
        view.setBackground(context.getResources().getDrawable(R.drawable.custom_bg));

        // Gets the TextView from the Toast so it can be editted
        TextView textView = view.findViewById(android.R.id.message);
        textView.setTextColor(context.getResources().getColor(R.color.textColorPrimary));

        toast.show();
    }

    /**
     * Return saved string from strings.xml.
     * @param stringID id of the string
     * @return saved string
     */
    public static String getString(int stringID) {
        Context context = InitializeActivity.getAppContext();
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
                        o1.getName().compareToIgnoreCase(o2.getName()) :
                        o2.getName().compareToIgnoreCase(o1.getName());
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
        showAlert(activity, getString(R.string.alert_exit_title),
                getString(R.string.alert_exit_message),
                getString(R.string.alert_exit_yes), null,
                () -> { resetToMenuScreen(activity); }, null);
    }

    /**
     * Clears all other activities and moves to menu screen.
     * @param activity current activity
     */
    public static void resetToMenuScreen(final Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    /**
     * Creates custom alert dialog.
     * @param context application context
     * @param topicText topic text, null = "Alert!"
     * @param messageText message text, null = "Are you sure?"
     * @param positiveText positive button text, null = "Ok"
     * @param negativeText negative button text, null = "Cancel"
     * @param positiveListener listener for positive button
     * @param negativeListener listener for negative button
     */
    public static void showAlert(Context context, String topicText, String messageText,
                                 String positiveText, String negativeText,
                                 AlertPositiveButtonListener positiveListener,
                                 AlertNegativeButtonListener negativeListener) {

        // Check for null values, if null set to default text
        String topic = topicText != null ? topicText : context.getString(R.string.alertTopic);
        String message = messageText != null ? messageText : context.getString(R.string.alertMessage);
        String posText = positiveText != null ? positiveText : context.getString(R.string.alertPositiveButton);
        String negText = negativeText != null ? negativeText : context.getString(R.string.alertNegativeButton);

        // Initialize alert dialog
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.custom_alert_dialog, null);
        final AlertDialog alertD = new AlertDialog.Builder(context).create();

        // Set topic text
        ((TextView)(view.findViewById(R.id.topic))).setText(topic);
        // Set message text
        ((TextView)(view.findViewById(R.id.text))).setText(message);
        // Set positive button text and listener
        Button positive = view.findViewById(R.id.positiveButton);
        positive.setText(posText);
        positive.setOnClickListener((e) -> {
            if (positiveListener != null)
                positiveListener.onClick();
            alertD.dismiss();
        });
        // Set negative button text and listener
        Button negative = view.findViewById(R.id.negativeButton);
        negative.setText(negText);
        negative.setOnClickListener((e) -> {
            if (negativeListener != null)
                negativeListener.onClick();
            alertD.dismiss();
        });

        // Show alert
        alertD.setView(view);
        alertD.show();
    }

    /**
     * Shows loading bar.
     * @param activity current activity
     */
    public static void showLoadingBar(Activity activity) {
        activity.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }

    /**
     * Hides loading bar.
     * @param activity current activity
     */
    public static void hideLoadingBar(Activity activity) {
        activity.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    /**
     * Shows loading bar and hides necessary view.
     * @param activity current activity
     * @param viewIDToHide view to hide
     */
    public static void showLoadingBar(Activity activity, int viewIDToHide) {
        activity.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        activity.findViewById(viewIDToHide).setVisibility(View.GONE);
    }

    /**
     * Hides loading bar and shows necessary view.
     * @param activity current activity
     * @param viewIDToShow view to show
     */
    public static void hideLoadingBar(Activity activity, int viewIDToShow) {
        activity.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        activity.findViewById(viewIDToShow).setVisibility(View.VISIBLE);
    }

    /**
     * Creates a fragment for displaying list items.
     * @param activity current activity
     * @param type database type of list
     * @param curList list to display
     * @return created list fragment
     */
    public static ListItemFragment createListItemFragment(AppCompatActivity activity, DatabaseType type,
                                                   ListOfItems curList) {
        // Retrieve FragmentManager and begin transaction
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Create Fragment and put database type and list as intent
        ListItemFragment itemsFragment = new ListItemFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type.name());
        bundle.putParcelable("curList", curList);
        itemsFragment.setArguments(bundle);

        // Add fragment to the listFragment FrameLayout
        fragmentTransaction.add(R.id.listFragment, itemsFragment);
        fragmentTransaction.commit();   // Add fragment to queue

        return itemsFragment;
    }

    /**
     * Formats date of result to show in a desired format.
     * @param date date of the result
     * @return formatted date
     */
    public static String formatResultDate(String date) {
        Date dateFormat = stringToDate(date);
        return android.text.format.DateFormat.format("yyyy-MM-dd hh:mm", dateFormat).toString();
    }

    /**
     * Converts firebase string to date.
     * @param string string to convert
     * @return converted date
     */
    public static Date stringToDate(String string) {
        SimpleDateFormat format = new SimpleDateFormat(FIREBASE_DATE_FORMAT);
        Date date = null;
        try {
            date = format.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }
}
