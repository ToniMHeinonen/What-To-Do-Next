package io.github.tonimheinonen.whattodonext;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

/**
 * Handles user changing app settings.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0.2
 * @since 1.0
 */
public class TutorialDialog extends Dialog implements
        View.OnClickListener {

    private Activity activity;

    public static final int FIRST_TUTORIAL = 0;
    private int tutorial;

    /**
     * Initializes necessary values.
     * @param a current activity
     * @param tutorial selected tutorial
     */
    public TutorialDialog(Activity a, int tutorial) {
        super(a);
        this.activity = a;
        this.tutorial = tutorial;
    }

    /**
     * Initializes views.
     * @param savedInstanceState previous Activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tutorial_dialog);

        // Set dialog window size to 90% of the screen width and height
        int width = (int)(activity.getResources().getDisplayMetrics().widthPixels);
        int height = (int)(activity.getResources().getDisplayMetrics().heightPixels);
        getWindow().setLayout(width, height);

        initializeViews();

        // Set listeners for confirm and cancel
        findViewById(R.id.showLater).setOnClickListener(this);
        findViewById(R.id.confirm).setOnClickListener(this);
    }

    /**
     * Changes texts of views and shows necessary views.
     */
    private void initializeViews() {
        TextView topic = findViewById(R.id.settingTopic);
        TextView text = findViewById(R.id.settingText);

        switch (tutorial) {
            case FIRST_TUTORIAL:
                topic.setText(activity.getString(R.string.first_tutorial));
                text.setText(activity.getString(R.string.first_tutorial_text));
                break;
        }
    }

    /**
     * Listens for view clicks.
     * @param v clicked view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showLater:
                cancel();
                break;
            case R.id.confirm:
                confirm();
                break;
            default:
                break;
        }
    }

    /**
     * Checks tutorial as read.
     */
    private void confirm() {
        switch (tutorial) {
            case FIRST_TUTORIAL:
                GlobalPrefs.saveFirstTutorial(false);
                dismiss();
                break;
        }
    }
}
