package io.github.tonimheinonen.whattodonext;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import io.github.tonimheinonen.whattodonext.database.GlobalSettings;
import io.github.tonimheinonen.whattodonext.database.VoteSettings;
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
        RESET_TUTORIAL,
        RESULT_STYLE
    }

    private VoteSettings voteSettings;
    private GlobalSettings globalSettings;
    private Setting setting;
    private EditText points, firstPoints, lastPoints;
    private Spinner spinner;
    private SwitchCompat onOffSwitch;
    private LinearLayout inflateLayout;

    /**
     * Initializes necessary values.
     * @param a current activity
     * @param setting selected setting
     */
    public SettingDialog(SettingsActivity a, Setting setting, VoteSettings voteSettings, GlobalSettings globalSettings) {
        super(a);
        this.activity = a;
        this.setting = setting;
        this.voteSettings = voteSettings;
        this.globalSettings = globalSettings;
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
        TextView note = findViewById(R.id.note);
        LinearLayout adjustingPoints = findViewById(R.id.adjustingPoints);
        points = findViewById(R.id.points);
        onOffSwitch = findViewById(R.id.onOffSwitch);
        spinner = findViewById(R.id.spinner);
        inflateLayout = findViewById(R.id.inflateLayout);

        switch (setting) {
            case MAX_PERIL:
                points.setText(String.valueOf(voteSettings.getMaxPeril()));
                topic.setText(activity.getString(R.string.max_peril));
                text.setText(activity.getString(R.string.max_peril_text));
                adjustingPoints.setVisibility(View.VISIBLE);
                break;
            case VOTE_POINTS:
                points.setText(String.valueOf(voteSettings.getFirstVote()));
                topic.setText(activity.getString(R.string.vote_points));
                text.setText(activity.getString(R.string.vote_points_text));
                // Inflate vote points layout
                View child = getLayoutInflater().inflate(R.layout.settings_vote_points, null);
                inflateLayout.addView(child);
                firstPoints = child.findViewById(R.id.firstPoints);
                firstPoints.setText(String.valueOf(voteSettings.getFirstVote()));
                lastPoints = child.findViewById(R.id.lastPoints);
                lastPoints.setText(String.valueOf(voteSettings.getLastVote()));
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
                onOffSwitch.setChecked(voteSettings.isIgnoreUnselected());
                break;
            case HALVE_EXTRA:
                topic.setText(activity.getString(R.string.halve_extra));
                text.setText(activity.getString(R.string.halve_extra_text));
                onOffSwitch.setVisibility(View.VISIBLE);
                onOffSwitch.setChecked(voteSettings.isHalveExtra());
                break;
            case SHOW_EXTRA:
                topic.setText(activity.getString(R.string.show_extra));
                text.setText(  activity.getString(R.string.show_extra_text));
                onOffSwitch.setVisibility(View.VISIBLE);
                onOffSwitch.setChecked(voteSettings.isShowExtra());
                break;
            case SHOW_VOTES:
                topic.setText(activity.getString(R.string.show_votes));
                text.setText(activity.getString(R.string.show_votes_text));
                onOffSwitch.setVisibility(View.VISIBLE);
                onOffSwitch.setChecked(voteSettings.isShowVoted());
                break;
            case RESULT_STYLE:
                topic.setText(activity.getString(R.string.result_style));
                text.setText(activity.getString(R.string.result_style_text));
                note.setVisibility(View.VISIBLE);
                note.setText(R.string.result_style_note);
                int spinnerPosition = globalSettings.getResultStyle();
                setupListsSpinner(activity.getResources().getStringArray(R.array.resultStyles), spinnerPosition);
                break;
            case RESET_TUTORIAL:
                topic.setText(activity.getString(R.string.reset_tutorial));
                text.setText(activity.getString(R.string.reset_tutorial_text));
                break;
        }
    }

    private void setupListsSpinner(String[] spinnerList, int startingPosition) {
        spinner.setVisibility(View.VISIBLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                R.layout.custom_spinner, spinnerList);
        spinner.setAdapter(adapter);
        spinner.setSelection(startingPosition);
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
                    voteSettings.setMaxPeril(p);
                    dismiss();
                }
                break;
            case VOTE_POINTS:
                int firstP = Integer.parseInt(firstPoints.getText().toString());
                int lastP = Integer.parseInt(lastPoints.getText().toString());
                if (checkPoints(firstPoints, firstP) && checkPoints(lastPoints, lastP)) {
                    voteSettings.setFirstVote(firstP);
                    voteSettings.setLastVote(lastP);
                    dismiss();
                }
                break;
            case IGNORE_UNSELECTED:
                voteSettings.setIgnoreUnselected(onOffSwitch.isChecked());
                dismiss();
                break;
            case HALVE_EXTRA:
                voteSettings.setHalveExtra(onOffSwitch.isChecked());
                dismiss();
                break;
            case SHOW_EXTRA:
                voteSettings.setShowExtra(onOffSwitch.isChecked());
                dismiss();
                break;
            case SHOW_VOTES:
                voteSettings.setShowVoted(onOffSwitch.isChecked());
                dismiss();
                break;
            case RESULT_STYLE:
                int spinnerPos = spinner.getSelectedItemPosition();
                globalSettings.setResultStyle(spinnerPos);
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
