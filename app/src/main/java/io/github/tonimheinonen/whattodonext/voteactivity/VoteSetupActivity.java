package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.ListsActivity;
import io.github.tonimheinonen.whattodonext.MainActivity;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.database.OnlineProfile;
import io.github.tonimheinonen.whattodonext.database.Profile;
import io.github.tonimheinonen.whattodonext.database.VoteRoom;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;
import io.github.tonimheinonen.whattodonext.tools.HTMLDialog;

/**
 * Handles setting up voting.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class VoteSetupActivity extends VotingParentActivity implements
        View.OnClickListener {

    private VoteSetupActivity _this = this;

    private ArrayList<ListOfItems> lists = new ArrayList<>();
    private ArrayList<Profile> profiles = new ArrayList<>();

    private boolean listBigEnough = false;
    private int firstVoteSize;

    // Setup voting options
    private ListOfItems selectedList;
    private DatabaseValueListAdapter profileListAdapter;
    private ArrayList<Profile> selectedProfiles = new ArrayList<>();

    private boolean isOnlineVote;

    // Online values
    private String roomCode, nickname;
    private EditText roomCodeView, nicknameView;

    /**
     * Initializes StartVoteActivity.
     * @param savedInstanceState previous activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get if vote is online
        Intent intent = getIntent();
        isOnlineVote = intent.getBooleanExtra("isOnline", false);

        if (isOnlineVote) {
            setContentView(R.layout.activity_vote_online);
            // Remove old vote rooms
            DatabaseHandler.removeExpiredVoteRooms();
            // Show beta info if not yet confirmed
            if (GlobalPrefs.loadPopupInfo(GlobalPrefs.BETA_ONLINE))
                new HTMLDialog(this, HTMLDialog.HTMLText.BETA_ONLINE).show();
        } else {
            setContentView(R.layout.activity_vote_local);
        }

        firstVoteSize = GlobalPrefs.loadListVoteSizeFirst();
    }

    //////////////////////// LOAD DATA FROM FIREBASE ////////////////////////

    /**
     * Shows loading bar and starts loading lists.
     */
    @Override
    protected void onStart() {
        super.onStart();
        Buddy.showLoadingBar(this);
        DatabaseHandler.getLists(this::loadLists);
    }

    /**
     * Checks if lists are empty.
     * @param lists loaded lists from database
     */
    public void loadLists(ArrayList<ListOfItems> lists) {
        this.lists = lists;

        // Online
        if (isOnlineVote) {
            if (lists.isEmpty()) {
                // Hide lists spinner
                findViewById(R.id.listsSpinner).setVisibility(View.GONE);
                findViewById(R.id.noLists).setVisibility(View.VISIBLE);
            }

            initializeVotingSetupOnline();
            return;
        }

        // Offline
        if (lists.isEmpty()) {
            // If there are no lists, show alert message
            Buddy.showAlert(this, getString(R.string.alert_no_lists_title),
                    getString(R.string.alert_no_lists_message),
                    getString(R.string.alert_no_lists_move),
                    getString(R.string.alert_no_lists_back),
                    () -> startActivity(new Intent(_this, ListsActivity.class)),
                    () -> startActivity(new Intent(_this, MainActivity.class)));
        } else {
            // Else start loading profiles
            DatabaseHandler.getProfiles(this::profilesLoaded);
        }
    }

    /**
     * Initialize views after profiles have been retrieved.
     * @param profiles loaded lists from database
     */
    public void profilesLoaded(ArrayList<Profile> profiles) {
        this.profiles = profiles;

        initializeVotingSetupOffline();
    }

    /**
     * Loads items for selected list and starts voting.
     * @param items loaded lists from database
     */
    public void itemsLoaded(ArrayList<ListItem> items) {
        Buddy.filterListByFallen(items, false);

        if (items.size() < firstVoteSize) {
            // If offline, show toast
            if (!isOnlineVote) {
                Buddy.showToast(getString(R.string.toast_not_enough_activities,
                        String.valueOf(firstVoteSize)), Toast.LENGTH_LONG);
            }
            return;
        }

        selectedList.setItems(items); // Put loaded items to selected list
        listBigEnough = true;
    }

    //////////////////////// INITIALIZE VIEWS ////////////////////////

    private void initializeVotingSetupOffline() {
        Buddy.hideLoadingBar(this);
        setupListsSpinner();

        // Set listeners for confirm and cancel
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.addProfile).setOnClickListener(this);

        // Add profiles to ListView
        final ListView profileListView = findViewById(R.id.savedProfiles);
        profileListAdapter = new DatabaseValueListAdapter(this, profiles, this,
                DatabaseType.PROFILE);
        profileListView.setAdapter(profileListAdapter);
    }

    private void initializeVotingSetupOnline() {
        Buddy.hideLoadingBar(this);

        if (!lists.isEmpty())
            setupListsSpinner();

        // Load previously saved values to nickname and room code
        roomCodeView = findViewById(R.id.roomCode);
        roomCodeView.setText(GlobalPrefs.loadOnlineRoomCode());
        Buddy.forceUpperCaseEditText(roomCodeView);

        nicknameView = findViewById(R.id.nickName);
        nicknameView.setText(GlobalPrefs.loadOnlineNickname());

        // Set listeners for buttons
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.host).setOnClickListener(this);
        findViewById(R.id.join).setOnClickListener(this);
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
                listBigEnough = false; // Change to false to prevent starting before items loaded
                selectedList = lists.get(position);
                GlobalPrefs.saveCurrentList(selectedList.getDbID());
                DatabaseHandler.getItems(_this::itemsLoaded, selectedList);
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
                startLocalVoting();
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
            // Online
            case R.id.host:
                if (onlineDetailsValid())
                    hostVoteRoom();
                break;
            case R.id.join:
                if (onlineDetailsValid())
                    joinVoteRoom();
                break;
            default:
                break;
        }
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
    private void startLocalVoting() {
        if (selectedProfiles.isEmpty()) {
            Buddy.showToast(Buddy.getString(R.string.toast_selected_profiles_empty), Toast.LENGTH_LONG);
            return;
        }

        if (!listBigEnough) {
            Buddy.showToast(getString(R.string.toast_not_enough_activities,
                    String.valueOf(firstVoteSize)), Toast.LENGTH_LONG);
            return;
        }

        // Move to voting top list
        Intent intent = new Intent(this, VoteTopActivity.class);
        intent.putExtra("topAmount", GlobalPrefs.loadListVoteSizeFirst());
        intent.putExtra("selectedList", selectedList);
        intent.putParcelableArrayListExtra("selectedProfiles", selectedProfiles);
        startActivity(intent);
    }

    private boolean onlineDetailsValid() {
        roomCode = roomCodeView.getText().toString();
        nickname = nicknameView.getText().toString();
        int roomCodeLength = getResources().getInteger(R.integer.room_code_length);

        if (roomCode.length() < roomCodeLength) {
            Buddy.showToast(getString(R.string.room_code_empty, roomCodeLength), Toast.LENGTH_SHORT);
            return false;
        }

        if (nickname.isEmpty()) {
            Buddy.showToast(getString(R.string.nickname_empty), Toast.LENGTH_SHORT);
            return false;
        }

        // Save name and code to memory
        GlobalPrefs.saveOnlineRoomCode(roomCode);
        GlobalPrefs.saveOnlineNickname(nickname);

        return true;
    }

    private void hostVoteRoom() {
        // If there are no lists, show toast
        if (lists.isEmpty()) {
            Buddy.showToast(getString(R.string.online_no_lists), Toast.LENGTH_LONG);
            return;
        }

        // If selected list is not big enough, show toast
        if (!listBigEnough) {
            Buddy.showToast(getString(R.string.toast_not_enough_activities,
                    String.valueOf(firstVoteSize)), Toast.LENGTH_LONG);
            return;
        }

        Buddy.showOnlineVoteLoadingBar(this);

        String roomCode = ((EditText) findViewById(R.id.roomCode)).getText().toString();
        // Create vote room
        final VoteRoom voteRoom = new VoteRoom(roomCode, selectedList.getName(), firstVoteSize,
                GlobalPrefs.loadListVoteSizeSecond(), GlobalPrefs.loadMaxPerilPoints(),
                GlobalPrefs.loadIgnoreUnselected(), GlobalPrefs.loadHalveExtra(),
                GlobalPrefs.loadShowExtra(), GlobalPrefs.loadShowVoted());

        // Add created vote room to database
        DatabaseHandler.addVoteRoom((added) -> {
            if (added) {
                moveToOnlineLobby(voteRoom, true);
            } else {
                Buddy.showToast(getString(R.string.online_host_duplicate), Toast.LENGTH_LONG);
                Buddy.hideOnlineVoteLoadingBar(this);
            }
        }, voteRoom);
    }

    private void joinVoteRoom() {
        Buddy.showOnlineVoteLoadingBar(this);

        DatabaseHandler.getVoteRoom((voteRoom) -> {
            if (voteRoom != null) {
                // If vote has already started, don't allow joining
                if (!voteRoom.getState().equals(VoteRoom.LOBBY)) {
                    Buddy.showToast(getString(R.string.vote_already_started), Toast.LENGTH_LONG);
                    Buddy.hideOnlineVoteLoadingBar(this);
                } else {
                    moveToOnlineLobby(voteRoom, false);
                }
            } else {
                Buddy.showToast(getString(R.string.room_not_found), Toast.LENGTH_LONG);
                Buddy.hideOnlineVoteLoadingBar(this);
            }
        }, roomCode);
    }

    private void moveToOnlineLobby(VoteRoom voteRoom, boolean host) {
        // Create online profile
        OnlineProfile profile = new OnlineProfile(nickname, host);

        // Move to lobby
        Intent intent = new Intent(this, VoteLobbyActivity.class);
        intent.putExtra("voteRoom", voteRoom);
        intent.putExtra("onlineProfile", profile);
        if (host)
            intent.putParcelableArrayListExtra("items", selectedList.getItems());

        startActivity(intent);
    }
}
