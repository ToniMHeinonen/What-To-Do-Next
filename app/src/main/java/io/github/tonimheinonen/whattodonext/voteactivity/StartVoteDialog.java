package io.github.tonimheinonen.whattodonext.voteactivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.Buddy;
import io.github.tonimheinonen.whattodonext.ClickListenerDialog;
import io.github.tonimheinonen.whattodonext.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.Debug;
import io.github.tonimheinonen.whattodonext.GlobalPrefs;
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
    private ArrayList<String> listNames = new ArrayList<>();
    private int startingListPosition;
    private ListOfItems selectedList;

    private ArrayList<Profile> profiles;
    private ArrayList<Profile> selectedProfiles = new ArrayList<>();

    private ListAndProfileAdapter profileListAdapter;
    private int profileTextDefaultColor = -1;

    public StartVoteDialog(VoteActivity a, ArrayList<ListOfItems> lists, ArrayList<Profile> profiles) {
        super(a);
        this.activity = a;
        this.lists = lists;
        this.profiles = profiles;

        String currentListId = GlobalPrefs.loadCurrentList();
        for (int i = 0; i < lists.size(); i++) {
            ListOfItems list = lists.get(i);
            listNames.add(list.getName());

            if (list.getDbID().equals(currentListId)) {
                startingListPosition = i;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.start_vote_dialog);

        // Setup saved lists spinner
        Spinner spinner = findViewById(R.id.listsSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, listNames);

        spinner.setAdapter(adapter);
        spinner.setSelection(startingListPosition);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedList = lists.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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
                startVoting();
                break;
            case R.id.addProfile:
                addNewProfile();
                break;
            case R.id.savedName:
                profileClicked(v);
                break;
            case R.id.savedDelete:
                deleteProfile(v);
                break;
            default:
                break;
        }
    }

    private void addNewProfile() {
        EditText profileTextView = findViewById(R.id.newProfile);
        String name = profileTextView.getText().toString();
        if (name.isEmpty()) {
            Buddy.showToast(Buddy.getString(R.string.toast_profile_empty));
            return;
        }

        Buddy.hideKeyboardAndClear(profileTextView);

        Profile profile = new Profile(name);
        DatabaseHandler.addProfile(profile);
        profiles.add(profile);
        profileListAdapter.notifyDataSetChanged();
    }

    private void profileClicked(View v) {
        Profile selected = profiles.get((int) v.getTag());

        Button btn = (Button) v;

        if (profileTextDefaultColor == -1) {
            profileTextDefaultColor = btn.getCurrentTextColor();
        }

        if (selectedProfiles.contains(selected)) {
            selectedProfiles.remove(selected);
            btn.setTextColor(profileTextDefaultColor);
        } else {
            selectedProfiles.add(selected);
            btn.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
        }
    }

    private void deleteProfile(View v) {
        Profile selected = profiles.get((int) v.getTag());

        if (selectedProfiles.contains(selected)) {
            Buddy.showToast(Buddy.getString(R.string.toast_cant_delete_selected));
            return;
        }

        DatabaseHandler.removeProfile(selected);

        profiles.remove(selected);

        profileListAdapter.notifyDataSetChanged();
    }

    private void startVoting() {
        if (selectedProfiles.isEmpty()) {
            Buddy.showToast(Buddy.getString(R.string.toast_selected_profiles_empty));
            return;
        }

        DatabaseHandler.getItems(activity, selectedList);
        dismiss();
    }
}
