package io.github.tonimheinonen.whattodonext;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
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
public class SettingDialog extends Dialog {

    private SettingsActivity activity;

    public enum Setting {
        MAX_PERIL,
        VOTE_POINTS,
        IGNORE_UNSELECTED,
        HALVE_EXTRA,
        SHOW_EXTRA,
        SHOW_VOTES,
        RESET_TUTORIAL
    }

    private Setting setting;
    private EditText points, firstPoints, lastPoints;
    private SwitchCompat onOffSwitch;
    private LinearLayout inflateLayout;

    /**
     * Initializes necessary values.
     * @param a current activity
     * @param setting selected setting
     */
    public SettingDialog(SettingsActivity a, Setting setting) {
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

        // Fill the entire screen if the orientation is landscape
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Set dialog window size to 90% of the screen width and height
            int width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.90);
            int height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.90);
            getWindow().setLayout(width, height);
        }

        initializeViews();

        // Set listeners for confirm and cancel
        findViewById(R.id.minus).setOnClickListener(v -> plusMinusPressed(points, -1));
        findViewById(R.id.plus).setOnClickListener(v -> plusMinusPressed(points, 1));
        findViewById(R.id.cancel).setOnClickListener(v -> cancel());
        findViewById(R.id.confirm).setOnClickListener(v -> confirm());
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
        inflateLayout = findViewById(R.id.inflateLayout);

        switch (setting) {
            case MAX_PERIL:
                points.setText(String.valueOf(GlobalPrefs.loadMaxPerilPoints()));
                topic.setText(activity.getString(R.string.max_peril));
                text.setText(activity.getString(R.string.max_peril_text));
                adjustingPoints.setVisibility(View.VISIBLE);
                break;
            case VOTE_POINTS:
                points.setText(String.valueOf(GlobalPrefs.loadListVoteSizeFirst()));
                topic.setText(activity.getString(R.string.vote_points));
                text.setText(activity.getString(R.string.vote_points_text));
                // Inflate vote points layout
                View child = getLayoutInflater().inflate(R.layout.settings_vote_points, null);
                inflateLayout.addView(child);
                firstPoints = child.findViewById(R.id.firstPoints);
                firstPoints.setText(String.valueOf(GlobalPrefs.loadListVoteSizeFirst()));
                lastPoints = child.findViewById(R.id.lastPoints);
                lastPoints.setText(String.valueOf(GlobalPrefs.loadListVoteSizeSecond()));
                // Listen for plus and minus clicks
                child.findViewById(R.id.firstPlus).setOnClickListener(v -> plusMinusPressed(firstPoints, 1));
                child.findViewById(R.id.firstMinus).setOnClickListener(v -> plusMinusPressed(firstPoints, -1));
                child.findViewById(R.id.lastPlus).setOnClickListener(v -> plusMinusPressed(lastPoints, 1));
                child.findViewById(R.id.lastMinus).setOnClickListener(v -> plusMinusPressed(lastPoints, -1));
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

    private void plusMinusPressed(EditText points, int adjustment) {
        int p = Integer.parseInt(points.getText().toString());
        checkPoints(points, p + adjustment);
    }

    /**
     * Check whether the amount of points in EditText is allowed.
     * @param p amount of points
     * @return true if points are allowed
     */
    private boolean checkPoints(EditText pointsEdit, int p) {
        switch (setting) {
            case MAX_PERIL:
                if (p <= 0) {
                    Buddy.showToast(activity.getString(R.string.points_at_zero), Toast.LENGTH_LONG);
                    points.setText("1");
                    return false;
                }

                points.setText(String.valueOf(p));
                break;
            case VOTE_POINTS:
                if (pointsEdit == firstPoints) {
                    // If first vote is the same size as last vote
                    int lastP = Integer.parseInt(lastPoints.getText().toString());
                    if (p <= lastP) {
                        Buddy.showToast(activity.getString(R.string.first_vote_same_as_last), Toast.LENGTH_LONG);
                        firstPoints.setText(String.valueOf(lastP + 1));
                        return false;
                    }

                    firstPoints.setText(String.valueOf(p));
                } else if (pointsEdit == lastPoints) {
                    // If last vote is the same size as first vote
                    int firstP = Integer.parseInt(firstPoints.getText().toString());
                    if (p >= firstP) {
                        Buddy.showToast(activity.getString(R.string.last_vote_same_as_first), Toast.LENGTH_LONG);
                        lastPoints.setText(String.valueOf(firstP - 1));
                        return false;
                    }

                    if (p <= 0) {
                        Buddy.showToast(activity.getString(R.string.points_at_zero), Toast.LENGTH_LONG);
                        lastPoints.setText("1");
                        return false;
                    }

                    lastPoints.setText(String.valueOf(p));
                }
                break;
        }
        return true;
    }

    /**
     * Checks whether to dismiss dialog and save new values.
     */
    private void confirm() {
        switch (setting) {
            case MAX_PERIL:
                int p = Integer.parseInt(points.getText().toString());
                if (checkPoints(points, p)) {
                    GlobalPrefs.saveMaxPerilPoints(p);
                    dismiss();
                }
                break;
            case VOTE_POINTS:
                int firstP = Integer.parseInt(firstPoints.getText().toString());
                int lastP = Integer.parseInt(lastPoints.getText().toString());
                if (checkPoints(firstPoints, firstP) && checkPoints(lastPoints, lastP)) {
                    GlobalPrefs.saveListVoteSizeFirst(firstP);
                    GlobalPrefs.saveListVoteSizeSecond(lastP);
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
                // Start new MainActivity so the first part of tutorial is shown
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
                break;
        }
    }
}
