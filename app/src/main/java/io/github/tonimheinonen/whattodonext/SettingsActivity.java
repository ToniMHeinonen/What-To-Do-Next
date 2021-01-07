package io.github.tonimheinonen.whattodonext;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Handles controlling of global values.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.2
 * @since 1.0
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Initializes SettingsActivity.
     * @param savedInstanceState previous saved state
     */
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
                dialog = new SettingDialog(this, SettingDialog.Setting.MAX_PERIL);
                break;
            case R.id.firstVote:
                dialog = new SettingDialog(this, SettingDialog.Setting.FIRST_VOTE);
                break;
            case R.id.lastVote:
                dialog = new SettingDialog(this, SettingDialog.Setting.LAST_VOTE);
                break;
            case R.id.ignoreUnselected:
                dialog = new SettingDialog(this, SettingDialog.Setting.IGNORE_UNSELECTED);
                break;
            case R.id.halveExtra:
                dialog = new SettingDialog(this, SettingDialog.Setting.HALVE_EXTRA);
                break;
            case R.id.showExtra:
                dialog = new SettingDialog(this, SettingDialog.Setting.SHOW_EXTRA);
                break;
            case R.id.showVotes:
                dialog = new SettingDialog(this, SettingDialog.Setting.SHOW_VOTES);
                break;
            case R.id.resetTutorial:
                dialog = new SettingDialog(this, SettingDialog.Setting.RESET_TUTORIAL);
                break;
            default:
                // Add default to suppress error dialog not initialized
                dialog = new SettingDialog(this, null);
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
