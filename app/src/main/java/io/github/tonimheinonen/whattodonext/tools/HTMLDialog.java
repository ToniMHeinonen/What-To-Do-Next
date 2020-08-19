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
        TUTORIAL_WELCOME,
        TUTORIAL_CREATE_LIST,
        TUTORIAL_ADD_ITEM,
        TUTORIAL_ITEM_INFO,
        TUTORIAL_VOTE_TYPE,
        TUTORIAL_ONLINE_VOTE,
        TUTORIAL_OFFLINE_VOTE,
        TUTORIAL_VOTE_TOP,
        TUTORIAL_LAST_RESULTS,
        TUTORIAL_VOTE_COMPLETE,
        BETA_ONLINE
    }

    private Activity activity;
    private HTMLText htmlText;
    private Button cancelButton, skipTutorialButton;

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
        skipTutorialButton = findViewById(R.id.skipTutorial);
        skipTutorialButton.setOnClickListener(this);

        initializeViews();
    }

    /**
     * Changes texts of views and shows necessary views.
     */
    private void initializeViews() {
        // Hide usually hidden buttons
        cancelButton.setVisibility(View.GONE);
        skipTutorialButton.setVisibility(View.GONE);

        switch (htmlText) {
            case NO_REGISTRATION:
                setTutorialText(activity.getString(R.string.no_registration_text));
                cancelButton.setVisibility(View.VISIBLE);
                break;
                // Tutorials
            case TUTORIAL_WELCOME:
                skipTutorialButton.setVisibility(View.VISIBLE);
                setTutorialText(activity.getString(R.string.welcome_tutorial_text));
                break;
            case TUTORIAL_CREATE_LIST:
                skipTutorialButton.setVisibility(View.VISIBLE);
                setTutorialText(activity.getString(R.string.add_list_tutorial_text));
                break;
            case TUTORIAL_ADD_ITEM:
                skipTutorialButton.setVisibility(View.VISIBLE);
                setTutorialText(activity.getString(R.string.add_item_tutorial_text));
                break;
            case TUTORIAL_ITEM_INFO:
                skipTutorialButton.setVisibility(View.VISIBLE);
                setTutorialText(activity.getString(R.string.item_info_tutorial_text));
                break;
            case TUTORIAL_VOTE_TYPE:
                skipTutorialButton.setVisibility(View.VISIBLE);
                setTutorialText(activity.getString(R.string.vote_type_tutorial_text));
                break;
            case TUTORIAL_OFFLINE_VOTE:
                skipTutorialButton.setVisibility(View.VISIBLE);
                setTutorialText(activity.getString(R.string.offline_vote_tutorial_text));
                break;
            case TUTORIAL_ONLINE_VOTE:
                skipTutorialButton.setVisibility(View.VISIBLE);
                setTutorialText(activity.getString(R.string.online_vote_tutorial_text));
                break;
            case TUTORIAL_VOTE_TOP:
                skipTutorialButton.setVisibility(View.VISIBLE);
                setTutorialText(activity.getString(R.string.vote_top_tutorial_text));
                break;
            case TUTORIAL_LAST_RESULTS:
                skipTutorialButton.setVisibility(View.VISIBLE);
                setTutorialText(activity.getString(R.string.last_results_tutorial_text));
                break;
            case TUTORIAL_VOTE_COMPLETE:
                setTutorialText(activity.getString(R.string.vote_complete_tutorial_text));
                break;
                // Beta
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
            case R.id.skipTutorial:
                skipTutorial();
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
            case TUTORIAL_WELCOME:
                GlobalPrefs.savePopupInfo(GlobalPrefs.TUTORIAL_WELCOME, false);
                break;
            case TUTORIAL_CREATE_LIST:
                GlobalPrefs.savePopupInfo(GlobalPrefs.TUTORIAL_CREATE_LIST, false);
                break;
            case TUTORIAL_ADD_ITEM:
                GlobalPrefs.savePopupInfo(GlobalPrefs.TUTORIAL_ADD_ITEM, false);
                break;
            case TUTORIAL_ITEM_INFO:
                GlobalPrefs.savePopupInfo(GlobalPrefs.TUTORIAL_ITEM_INFO, false);
                break;
            case TUTORIAL_VOTE_TYPE:
                GlobalPrefs.savePopupInfo(GlobalPrefs.TUTORIAL_VOTE_TYPE, false);
                break;
            case TUTORIAL_OFFLINE_VOTE:
                GlobalPrefs.savePopupInfo(GlobalPrefs.TUTORIAL_OFFLINE_VOTE, false);
                break;
            case TUTORIAL_ONLINE_VOTE:
                GlobalPrefs.savePopupInfo(GlobalPrefs.TUTORIAL_ONLINE_VOTE, false);
                break;
            case TUTORIAL_VOTE_TOP:
                GlobalPrefs.savePopupInfo(GlobalPrefs.TUTORIAL_VOTE_TOP, false);
                break;
            case TUTORIAL_LAST_RESULTS:
                GlobalPrefs.savePopupInfo(GlobalPrefs.TUTORIAL_LAST_RESULTS, false);
                break;
            case TUTORIAL_VOTE_COMPLETE:
                GlobalPrefs.savePopupInfo(GlobalPrefs.TUTORIAL_VOTE_COMPLETE, false);
                break;
            case BETA_ONLINE:
                GlobalPrefs.savePopupInfo(GlobalPrefs.BETA_ONLINE, false);
                break;
        }

        dismiss();
    }

    private void skipTutorial() {
        // Mark all tutorials read
        for (String tutorial : GlobalPrefs.TUTORIAL_TEXTS) {
            GlobalPrefs.savePopupInfo(tutorial, false);
        }

        dismiss();
    }
}
