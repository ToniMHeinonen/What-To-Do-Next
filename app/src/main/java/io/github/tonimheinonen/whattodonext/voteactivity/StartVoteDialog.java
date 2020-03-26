package io.github.tonimheinonen.whattodonext.voteactivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.Buddy;
import io.github.tonimheinonen.whattodonext.ClickListenerDialog;
import io.github.tonimheinonen.whattodonext.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.Debug;
import io.github.tonimheinonen.whattodonext.ListAndProfileAdapter;
import io.github.tonimheinonen.whattodonext.ListsActivity;
import io.github.tonimheinonen.whattodonext.MainActivity;
import io.github.tonimheinonen.whattodonext.Profile;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.VoteActivity;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

public class StartVoteDialog extends ClickListenerDialog implements
        View.OnClickListener {

    private VoteActivity activity;
    private ArrayList<ListOfItems> lists;
    private ArrayList<Profile> profiles;

    private ListAndProfileAdapter profileListAdapter;

    public StartVoteDialog(VoteActivity a, ArrayList<ListOfItems> lists, ArrayList<Profile> profiles) {
        super(a);
        this.activity = a;
        this.lists = lists;
        this.profiles = profiles;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.start_vote_dialog);

        // Set listeners for confirm and cancel
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.addProfile).setOnClickListener(this);

        // Add profiles to ListView
        final ListView profileListView = findViewById(R.id.savedProfiles);
        profileListAdapter = new ListAndProfileAdapter(activity, profiles, this);
        profileListView.setAdapter(profileListAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                activity.startActivity(new Intent(activity, MainActivity.class));
                dismiss();
                break;
            case R.id.start:
                break;
            case R.id.addProfile:
                addNewProfile();
                break;
            default:
                break;
        }
    }

    private void addNewProfile() {
        EditText profileTextView = findViewById(R.id.newProfile);
        String name = profileTextView.getText().toString();
        if (name.isEmpty())
            return;

        Buddy.hideKeyboardAndClear(profileTextView);

        Profile profile = new Profile(name);
        DatabaseHandler.addProfile(profile);
        profiles.add(profile);
        profileListAdapter.notifyDataSetChanged();
    }
}
