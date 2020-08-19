package io.github.tonimheinonen.whattodonext.tools;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import io.github.tonimheinonen.whattodonext.R;

/**
 * Shows info texts in a popup.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.3
 * @since 1.3
 */
public class HTMLDialog extends Dialog implements
        View.OnClickListener {

    public enum HTMLText {
        NO_REGISTRATION,
        FIRST,
        BETA_ONLINE
    }

    private Activity activity;
    private HTMLText htmlText;
    private Button cancelButton;

    /**
     * Initializes necessary values.
     * @param a current activity
     * @param htmlText selected text to show
     */
    public HTMLDialog(Activity a, HTMLText htmlText) {
        super(a);
        this.activity = a;
        this.htmlText = htmlText;
    }

    /**
     * Initializes views.
     * @param savedInstanceState previous Activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.html_dialog);

        // Set dialog window size to 90% of the screen width and height
        int width = (int)(activity.getResources().getDisplayMetrics().widthPixels);
        //int height = (int)(activity.getResources().getDisplayMetrics().heightPixels);
        getWindow().setLayout(width, getWindow().getAttributes().height);

        // Set listeners for buttons
        findViewById(R.id.confirm).setOnClickListener(this);
        cancelButton = findViewById(R.id.cancel);
        cancelButton.setOnClickListener(this);

        initializeViews();
    }

    /**
     * Changes texts of views and shows necessary views.
     */
    private void initializeViews() {
        // Hide usually hidden buttons
        cancelButton.setVisibility(View.GONE);

        switch (htmlText) {
            case NO_REGISTRATION:
                setTutorialText(activity.getString(R.string.no_registration_text));
                cancelButton.setVisibility(View.VISIBLE);
                break;
            case FIRST:
                setTutorialText(activity.getString(R.string.first_tutorial_text));
                break;
            case BETA_ONLINE:
                setTutorialText(activity.getString(R.string.beta_online));
                break;
        }
    }

    /**
     * Formats html text correctly to TextView.
     * @param text html text to set on TextView
     */
    private void setTutorialText(String text) {
        TextView textView = findViewById(R.id.tutorialText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            textView.setText(Html.fromHtml(text));
        }
    }

    /**
     * Listens for view clicks.
     * @param v clicked view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                confirm();
                break;
            case R.id.cancel:
                cancel();
                break;
            default:
                break;
        }
    }

    /**
     * Does necessary stuff when clicking the ok button.
     */
    private void confirm() {
        switch (htmlText) {
            case NO_REGISTRATION:
                Buddy.isRegistered = false;
                Buddy.moveToVoteSetup(activity, true);
                break;
            case FIRST:
                GlobalPrefs.savePopupInfo(GlobalPrefs.FIRST_TUTORIAL, false);
                break;
            case BETA_ONLINE:
                GlobalPrefs.savePopupInfo(GlobalPrefs.BETA_ONLINE, false);
                break;
        }

        dismiss();
    }
}