package io.github.tonimheinonen.whattodonext;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

/**
 * Handles user changing app settings.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0.2
 * @since 1.0
 */
public class SettingDialog extends Dialog implements
        View.OnClickListener {

    private Activity activity;

    public enum Setting {
        MAX_PERIL,
        FIRST_VOTE,
        LAST_VOTE,
        IGNORE_UNSELECTED,
        HALVE_EXTRA,
        SHOW_EXTRA,
        SHOW_VOTES,
        RESET_TUTORIAL
    }

    private Setting setting;
    private EditText points;
    private SwitchCompat onOffSwitch;

    /**
     * Initializes necessary values.
     * @param a current activity
     * @param setting selected setting
     */
    public SettingDialog(Activity a, Setting setting) {
        super(a);
        this.activity = a;
        this.setting = setting;
    }

    /**
     * Initializes views.
     * @param savedInstanceState previous Activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting_dialog);

        // Set dialog window size to 90% of the screen width and height
        int width = (int)(activity.getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(activity.getResources().getDisplayMetrics().heightPixels*0.90);
        getWindow().setLayout(width, height);

        initializeViews();

        // Set listeners for confirm and cancel
        findViewById(R.id.minus).setOnClickListener(this);
        findViewById(R.id.plus).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.confirm).setOnClickListener(this);
    }

    /**
     * Changes texts of views and shows necessary views.
     */
    private void initializeViews() {
        TextView topic = findViewById(R.id.settingTopic);
        TextView text = findViewById(R.id.settingText);
        LinearLayout adjustingPoints = findViewById(R.id.adjustingPoints);
        points = findViewById(R.id.points);
        onOffSwitch = findViewById(R.id.onOffSwitch);

        switch (setting) {
            case MAX_PERIL:
                points.setText(String.valueOf(GlobalPrefs.loadMaxPerilPoints()));
                topic.setText(activity.getString(R.string.max_peril));
                text.setText(activity.getString(R.string.max_peril_text));
                adjustingPoints.setVisibility(View.VISIBLE);
                break;
            case FIRST_VOTE:
                points.setText(String.valueOf(GlobalPrefs.loadListVoteSizeFirst()));
                topic.setText(activity.getString(R.string.first_vote));
                text.setText(activity.getString(R.string.first_vote_text));
                adjustingPoints.setVisibility(View.VISIBLE);
                break;
            case LAST_VOTE:
                points.setText(String.valueOf(GlobalPrefs.loadListVoteSizeSecond()));
                topic.setText(activity.getString(R.string.last_vote));
                text.setText(activity.getString(R.string.last_vote_text));
                adjustingPoints.setVisibility(View.VISIBLE);
                break;
            case IGNORE_UNSELECTED:
                topic.setText(activity.getString(R.string.ignore_unselected));
                text.setText(activity.getString(R.string.ignore_unselected_text));
                onOffSwitch.setVisibility(View.VISIBLE);
                onOffSwitch.setChecked(GlobalPrefs.loadIgnoreUnselected());
                break;
            case HALVE_EXTRA:
                topic.setText(activity.getString(R.string.halve_extra));
                text.setText(activity.getString(R.string.halve_extra_text));
                onOffSwitch.setVisibility(View.VISIBLE);
                onOffSwitch.setChecked(GlobalPrefs.loadHalveExtra());
                break;
            case SHOW_EXTRA:
                topic.setText(activity.getString(R.string.show_extra));
                text.setText(  activity.getString(R.string.show_extra_text));
                onOffSwitch.setVisibility(View.VISIBLE);
                onOffSwitch.setChecked(GlobalPrefs.loadShowExtra());
                break;
            case SHOW_VOTES:
                topic.setText(activity.getString(R.string.show_votes));
                text.setText(activity.getString(R.string.show_votes_text));
                onOffSwitch.setVisibility(View.VISIBLE);
                onOffSwitch.setChecked(GlobalPrefs.loadShowVoted());
                break;
            case RESET_TUTORIAL:
                topic.setText(activity.getString(R.string.reset_tutorial));
                text.setText(activity.getString(R.string.reset_tutorial_text));
                break;
        }
    }

    /**
     * Listens for view clicks.
     * @param v clicked view
     */
    @Override
    public void onClick(View v) {
        int p;

        switch (v.getId()) {
            case R.id.plus:
                p = Integer.parseInt(points.getText().toString());
                checkPoints(p + 1);
                break;
            case R.id.minus:
                p = Integer.parseInt(points.getText().toString());
                checkPoints(p - 1);
                break;
            case R.id.cancel:
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
     * Check whether the amount of points in EditText is allowed.
     * @param p amount of points
     * @return true if points are allowed
     */
    private boolean checkPoints(int p) {
        switch (setting) {
            case MAX_PERIL:
                if (p <= 0) {
                    Buddy.showToast(activity.getString(R.string.points_at_zero), Toast.LENGTH_LONG);
                    points.setText("1");
                    return false;
                }
                break;
            case LAST_VOTE:
                // If last vote is the same size as first vote
                if (p >= GlobalPrefs.loadListVoteSizeFirst()) {
                    Buddy.showToast(activity.getString(R.string.last_vote_same_as_first), Toast.LENGTH_LONG);
                    points.setText(String.valueOf(GlobalPrefs.loadListVoteSizeFirst() - 1));
                    return false;
                }

                if (p <= 0) {
                    Buddy.showToast(activity.getString(R.string.points_at_zero), Toast.LENGTH_LONG);
                    points.setText("1");
                    return false;
                }
                break;
            case FIRST_VOTE:
                // If first vote is the same size as last vote
                if (p <= GlobalPrefs.loadListVoteSizeSecond()) {
                    Buddy.showToast(activity.getString(R.string.first_vote_same_as_last), Toast.LENGTH_LONG);
                    points.setText(String.valueOf(GlobalPrefs.loadListVoteSizeSecond() + 1));
                    return false;
                }
                break;
        }

        points.setText(String.valueOf(p));
        return true;
    }

    /**
     * Checks whether to dismiss dialog and save new values.
     */
    private void confirm() {
        int p = Integer.parseInt(points.getText().toString());

        switch (setting) {
            case MAX_PERIL:
                if (checkPoints(p)) {
                    GlobalPrefs.saveMaxPerilPoints(p);
                    dismiss();
                }
                break;
            case FIRST_VOTE:
                if (checkPoints(p)) {
                    GlobalPrefs.saveListVoteSizeFirst(p);
                    dismiss();
                }
                break;
            case LAST_VOTE:
                if (checkPoints(p)) {
                    GlobalPrefs.saveListVoteSizeSecond(p);
                    dismiss();
                }
                break;
            case IGNORE_UNSELECTED:
                GlobalPrefs.saveIgnoreUnselected(onOffSwitch.isChecked());
                dismiss();
                break;
            case HALVE_EXTRA:
                GlobalPrefs.saveHalveExtra(onOffSwitch.isChecked());
                dismiss();
                break;
            case SHOW_EXTRA:
                GlobalPrefs.saveShowExtra(onOffSwitch.isChecked());
                dismiss();
                break;
            case SHOW_VOTES:
                GlobalPrefs.saveShowVoted(onOffSwitch.isChecked());
                dismiss();
                break;
            case RESET_TUTORIAL:
                // Mark all tutorials unread
                for (String tutorial : GlobalPrefs.TUTORIAL_TEXTS) {
                    GlobalPrefs.savePopupInfo(tutorial, true);
                }
                dismiss();
                break;
        }
    }
}
