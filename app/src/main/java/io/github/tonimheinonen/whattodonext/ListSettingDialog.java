package io.github.tonimheinonen.whattodonext;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.tonimheinonen.whattodonext.component.BoolValueLayout;
import io.github.tonimheinonen.whattodonext.component.IntValueLayout;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.database.VoteSettings;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.voteactivity.VoteSetupActivity;

public class ListSettingDialog extends Dialog {

    private VoteSetupActivity a;
    private ListOfItems list;
    private VoteSettings voteSettings;
    private LinearLayout inflateLayout;

    private IntValueLayout firstVote, lastVote, maxPeril;
    private BoolValueLayout ignoreUnselected, halveExtra, showExtra, showVoted;

    /**
     * Initializes necessary values.
     * @param a current activity
     * @param list current list
     */
    public ListSettingDialog(VoteSetupActivity a, ListOfItems list) {
        super(a);
        this.a = a;
        this.list = list;
    }

    /**
     * Initializes views.
     * @param savedInstanceState previous Activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.list_setting_dialog);

        // Fill the entire screen
        int width = (int) (a.getResources().getDisplayMetrics().widthPixels);
        int height = (int) (a.getResources().getDisplayMetrics().heightPixels);
        getWindow().setLayout(width, height);

        Buddy.showLoadingBar(a);

        // Load list settings
        DatabaseHandler.getVoteSettings((settings) -> {
            // Check if list settings are null
            if (settings == null) {
                // If null, load default settings
                DatabaseHandler.getVoteSettings((defaultSettings) -> {
                    if (defaultSettings == null) {
                        // If default settings are null, create new default settings
                        defaultSettings = new VoteSettings();
                        DatabaseHandler.addVoteSettings(null, defaultSettings);
                    }
                    // Set default settings as base values for list
                    voteSettings = defaultSettings;
                    DatabaseHandler.addVoteSettings(list, voteSettings);
                    initializeViews();
                }, null);
            } else {
                // Else set reference to loaded settings
                voteSettings = settings;
                initializeViews();
            }
        }, list);
    }

    /**
     * Changes texts of views and shows necessary views.
     */
    private void initializeViews() {
        Buddy.hideLoadingBar(a);

        TextView topic = findViewById(R.id.settingTopic);
        topic.setText(a.getString(R.string.list_settings_title, list.getName()));
        inflateLayout = findViewById(R.id.inflateLayout);

        // Add vote settings
        firstVote = new IntValueLayout(a, this, inflateLayout,
                a.getString(R.string.first_vote), voteSettings.getFirstVote());

        lastVote = new IntValueLayout(a, this, inflateLayout,
                a.getString(R.string.last_vote), voteSettings.getLastVote());

        firstVote.setMin(lastVote);
        firstVote.setMax(list.getItems().size());
        firstVote.setUnderMinToastTextID(R.string.first_vote_same_as_last);
        firstVote.setOverMaxToastTextID(R.string.points_over_list_size);
        lastVote.setMax(firstVote);
        lastVote.setMin(1);
        lastVote.setOverMaxToastTextID(R.string.last_vote_same_as_first);

        // Add max peril
        maxPeril = new IntValueLayout(a, this, inflateLayout,
                a.getString(R.string.max_peril), voteSettings.getMaxPeril());

        maxPeril.setMin(1);

        // Add boolean values
        ignoreUnselected = new BoolValueLayout(this, inflateLayout,
                a.getString(R.string.ignore_unselected), voteSettings.isIgnoreUnselected());

        halveExtra = new BoolValueLayout(this, inflateLayout,
                a.getString(R.string.halve_extra), voteSettings.isHalveExtra());

        showExtra = new BoolValueLayout(this, inflateLayout,
                a.getString(R.string.show_extra), voteSettings.isShowExtra());

        showVoted = new BoolValueLayout(this, inflateLayout,
                a.getString(R.string.show_votes), voteSettings.isShowVoted());

        // Set listeners for confirm and cancel
        findViewById(R.id.cancel).setOnClickListener(v -> cancel());
        findViewById(R.id.confirm).setOnClickListener(v -> confirm());
    }

    /**
     * Checks whether to dismiss dialog and save new values.
     */
    private void confirm() {
        // Return, if any value is illegal
        if (!firstVote.valueIsLegal() ||
            !lastVote.valueIsLegal() ||
            !maxPeril.valueIsLegal())
            return;

        // Save all settings
        voteSettings.setFirstVote(firstVote.getValue());
        voteSettings.setLastVote(lastVote.getValue());
        voteSettings.setMaxPeril(maxPeril.getValue());
        voteSettings.setIgnoreUnselected(ignoreUnselected.getValue());
        voteSettings.setHalveExtra(halveExtra.getValue());
        voteSettings.setShowExtra(showExtra.getValue());
        voteSettings.setShowVoted(showVoted.getValue());
        DatabaseHandler.modifyVoteSettings(voteSettings);

        // Inform activity that dialog was confirmed
        a.listSettingDialogConfirmed(voteSettings);
        dismiss();
    }
}
