package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

/**
 * Handles controlling of global values.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    /**
     * Listens for setting button presses.
     * @param v selected setting
     */
    public void settingSelected(View v) {
        SettingDialog dialog;

        switch (v.getId()) {
            case R.id.maxPeril:
                dialog = new SettingDialog(this, SettingDialog.MAX_PERIL);
                break;
            case R.id.firstVote:
                dialog = new SettingDialog(this, SettingDialog.FIRST_VOTE);
                break;
            case R.id.lastVote:
                dialog = new SettingDialog(this, SettingDialog.LAST_VOTE);
                break;
            case R.id.ignoreUnselected:
                dialog = new SettingDialog(this, SettingDialog.IGNORE_UNSELECTED);
                break;
            case R.id.halveExtra:
                dialog = new SettingDialog(this, SettingDialog.HALVE_EXTRA);
                break;
            case R.id.showExtra:
                dialog = new SettingDialog(this, SettingDialog.SHOW_EXTRA);
                break;
            case R.id.showVotes:
                dialog = new SettingDialog(this, SettingDialog.SHOW_VOTES);
                break;
            default:
                // Add default to suppress error dialog not initialized
                dialog = new SettingDialog(this, -1);
                break;
        }

        dialog.show();
    }

    /**
     * Listens for back button presses.
     * @param v back button
     */
    public void backSelected(View v) {
        super.onBackPressed();
    }
}
