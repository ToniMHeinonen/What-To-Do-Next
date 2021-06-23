package io.github.tonimheinonen.whattodonext;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.SavedResult;
import io.github.tonimheinonen.whattodonext.database.VoteSettings;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.HTMLDialog;

/**
 * Handles controlling of global values.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.2
 * @since 1.0
 */
public class SettingsActivity extends AppCompatActivity {

    private VoteSettings voteSettings;
    private boolean addNewVoteSettings;

    /**
     * Initializes SettingsActivity.
     * @param savedInstanceState previous saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Retrieve saved results
        Buddy.showLoadingBar(this, R.id.settingsLayout);
        DatabaseHandler.getVoteSettings(this::voteSettingsLoaded, null);
    }

    /**
     * Initializes values after vote settings are loaded from database.
     * @param voteSettings loaded vote settings
     */
    private void voteSettingsLoaded(VoteSettings voteSettings) {
        if (voteSettings == null) {
            voteSettings = new VoteSettings();
            addNewVoteSettings = true;
        }

        this.voteSettings = voteSettings;
        Buddy.hideLoadingBar(this, R.id.settingsLayout);
    }

    /**
     * Listens for setting button presses.
     * @param v selected setting
     */
    public void settingSelected(View v) {
        SettingDialog dialog = null;

        switch (v.getId()) {
            case R.id.maxPeril:
                dialog = new SettingDialog(this, SettingDialog.Setting.MAX_PERIL, voteSettings);
                break;
            case R.id.votePoints:
                dialog = new SettingDialog(this, SettingDialog.Setting.VOTE_POINTS, voteSettings);
                break;
            case R.id.ignoreUnselected:
                dialog = new SettingDialog(this, SettingDialog.Setting.IGNORE_UNSELECTED, voteSettings);
                break;
            case R.id.halveExtra:
                dialog = new SettingDialog(this, SettingDialog.Setting.HALVE_EXTRA, voteSettings);
                break;
            case R.id.showExtra:
                dialog = new SettingDialog(this, SettingDialog.Setting.SHOW_EXTRA, voteSettings);
                break;
            case R.id.showVotes:
                dialog = new SettingDialog(this, SettingDialog.Setting.SHOW_VOTES, voteSettings);
                break;
            case R.id.resetTutorial:
                dialog = new SettingDialog(this, SettingDialog.Setting.RESET_TUTORIAL, voteSettings);
                break;
            case R.id.patchNotes:
                new HTMLDialog(this, HTMLDialog.HTMLText.PATCH_NOTES).show();
                break;
        }

        if (dialog != null)
            dialog.show();
    }

    /**
     * Listens for back button presses.
     * @param v back button
     */
    public void backSelected(View v) {
        if (addNewVoteSettings)
            DatabaseHandler.addVoteSettings(null, voteSettings);
        else
            DatabaseHandler.modifyVoteSettings(voteSettings);

        super.onBackPressed();
    }
}
