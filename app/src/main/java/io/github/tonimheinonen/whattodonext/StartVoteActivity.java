package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;
import io.github.tonimheinonen.whattodonext.voteactivity.VoteTopActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Handles setting up voting.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class StartVoteActivity extends AppCompatActivity implements OnGetDataListener,
        View.OnClickListener {

    private StartVoteActivity _this = this;

    private ArrayList<ListOfItems> lists = new ArrayList<>();
    private ArrayList<Profile> profiles = new ArrayList<>();

    // Setup voting options
    private ListOfItems selectedList;
    private ListAndProfileAdapter profileListAdapter;
    private ArrayList<Profile> selectedProfiles = new ArrayList<>();

    /**
     * Initializes StartVoteActivity.
     * @param savedInstanceState previous activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_vote);
    }

    //////////////////////// LOAD DATA FROM FIREBASE ////////////////////////

    /**
     * Shows loading bar and starts loading lists.
     */
    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE); // Show loading bar
        DatabaseHandler.getLists(this);
    }

    /**
     * Checks if lists are empty.
     * @param lists loaded lists from database
     */
    @Override
    public void onDataGetLists(ArrayList<ListOfItems> lists) {
        Debug.print(this, "onDataGetLists", "", 1);
        this.lists = lists;

        if (lists.isEmpty()) {
            // If there are no lists, show alert message
            Debug.print(this, "onDataGetLists", "isEmpty", 1);
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.alert_no_lists_title))
                    .setMessage(getString(R.string.alert_no_lists_message))

                    .setPositiveButton(getString(R.string.alert_no_lists_move), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(_this, ListsActivity.class));
                        }
                    })

                    .setNegativeButton(getString(R.string.alert_no_lists_back),  new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(_this, MainActivity.class));
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            // Else start loading profiles
            DatabaseHandler.getProfiles(this);
        }
    }

    /**
     * Initialize views after profiles have been retrieved.
     * @param profiles loaded lists from database
     */
    @Override
    public void onDataGetProfiles(ArrayList<Profile> profiles) {
        this.profiles = profiles;

        findViewById(R.id.loadingPanel).setVisibility(View.GONE); // Hide loading bar
        initializeVotingSetup();
    }

    /**
     * Loads items for selected list and starts voting.
     * @param items loaded lists from database
     */
    @Override
    public void onDataGetItems(ArrayList<ListItem> items) {
        int firstTopAmount = GlobalPrefs.loadListVoteSizeFirst();

        if (items.size() < firstTopAmount) {
            Buddy.showToast(Buddy.getString(R.string.toast_not_enough_activities), Toast.LENGTH_LONG);
            return;
        }

        Buddy.filterListByFallen(items, false);
        selectedList.setItems(items); // Put loaded items to selected list

        // Move to voting top list
        Intent intent = new Intent(this, VoteTopActivity.class);
        intent.putExtra("topAmount", firstTopAmount);
        intent.putExtra("selectedList", selectedList);
        intent.putParcelableArrayListExtra("selectedProfiles", selectedProfiles);
        startActivity(intent);
    }

    //////////////////////// INITIALIZE VIEWS ////////////////////////

    /**
     *
     */
    private void initializeVotingSetup() {
        setupListsSpinner();

        // Set listeners for confirm and cancel
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.addProfile).setOnClickListener(this);

        // Add profiles to ListView
        final ListView profileListView = findViewById(R.id.savedProfiles);
        profileListAdapter = new ListAndProfileAdapter(this, profiles, this);
        profileListView.setAdapter(profileListAdapter);
    }

    private void setupListsSpinner() {
        ArrayList<String> listNames = new ArrayList<>();
        int startingListPosition = -1;

        // Load current starting position depending on latest used list.
        String currentListId = GlobalPrefs.loadCurrentList();
        for (int i = 0; i < lists.size(); i++) {
            ListOfItems list = lists.get(i);
            listNames.add(list.getName());

            if (list.getDbID().equals(currentListId)) {
                startingListPosition = i;
            }
        }

        // Set adapter and position
        Spinner spinner = findViewById(R.id.listsSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, listNames);
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
    }

    /**
     * Listens clicks of views.
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
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

    /**
     * Moves back to MainActivity.
     */
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Adds new profile.
     */
    private void addNewProfile() {
        // Get name of the new profile
        EditText profileTextView = findViewById(R.id.newProfile);
        String name = profileTextView.getText().toString();
        if (name.isEmpty()) {
            Buddy.showToast(Buddy.getString(R.string.toast_profile_empty), Toast.LENGTH_LONG);
            return;
        }

        // Hides keyboard and clears profile edit text
        Buddy.hideKeyboardAndClear(profileTextView, true);

        // Add new profile to list and database
        Profile profile = new Profile(name);
        DatabaseHandler.addProfile(profile);
        profiles.add(profile);
        profileListAdapter.notifyDataSetChanged();
    }

    /**
     * Selects and deselects profiles.
     * @param v profile view
     */
    private void profileClicked(View v) {
        Profile selected = profiles.get((int) v.getTag());

        if (selectedProfiles.contains(selected)) {
            selectedProfiles.remove(selected);
            selected.setSelected(false);
        } else {
            selectedProfiles.add(selected);
            selected.setSelected(true);
        }

        profileListAdapter.notifyDataSetChanged();
    }

    /**
     * Deletes clicked profile.
     * @param v profile view
     */
    private void deleteProfile(View v) {
        Profile selected = profiles.get((int) v.getTag());

        if (selectedProfiles.contains(selected)) {
            Buddy.showToast(Buddy.getString(R.string.toast_cant_delete_selected), Toast.LENGTH_LONG);
            return;
        }

        DatabaseHandler.removeProfile(selected);

        profiles.remove(selected);

        profileListAdapter.notifyDataSetChanged();
    }

    /**
     * Checks whether to start voting or not.
     */
    private void startVoting() {
        if (selectedProfiles.isEmpty()) {
            Buddy.showToast(Buddy.getString(R.string.toast_selected_profiles_empty), Toast.LENGTH_LONG);
            return;
        }

        DatabaseHandler.getItems(this, selectedList);
    }
}
