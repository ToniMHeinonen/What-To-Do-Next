package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.BuildConfig;
import io.github.tonimheinonen.whattodonext.ListSettingDialog;
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
import io.github.tonimheinonen.whattodonext.database.VoteSettings;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.Debug;
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
    private VoteSettings voteSettings;

    private final int minimumItemCount = 2;

    private enum LobbyJoinOptions { HOST_NEW, JOIN_NEW, JOIN_RECONNECT }

    // Setup voting options
    private ListOfItems selectedList;
    private DatabaseValueListAdapter profileListAdapter;
    private ArrayList<Profile> selectedProfiles = new ArrayList<>();

    private boolean isOnlineVote;
    private OnlineProfile onlineProfile;

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
        isOnlineVote = intent.getBooleanExtra(VoteIntents.IS_ONLINE, false);

        if (isOnlineVote) {
            setContentView(R.layout.activity_vote_online);
            // Remove old vote rooms
            DatabaseHandler.removeExpiredVoteRooms();
        } else {
            setContentView(R.layout.activity_vote_local);
        }
    }

    //////////////////////// LOAD DATA FROM FIREBASE ////////////////////////

    /**
     * Shows loading bar and starts loading lists.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Check if user has registered
        if (Buddy.isRegistered) {
            // Load user's lists from database
            Buddy.showLoadingBar(this);
            DatabaseHandler.getLists(this::loadLists);
        } else {
            // Initialize unregistered voting
            // Hide lists spinner
            findViewById(R.id.listsSpinner).setVisibility(View.GONE);
            TextView noListsView = findViewById(R.id.noLists);
            noListsView.setVisibility(View.VISIBLE);
            noListsView.setText(getString(R.string.unregistered_lists_locked));
            initializeVotingSetupOnline();
        }
    }

    /**
     * Checks if lists are empty.
     * @param lists loaded lists from database
     */
    public void loadLists(ArrayList<ListOfItems> lists) {
        this.lists = lists;

        if (isOnlineVote) {
            // Online (Remember to modify unregistered behaviour if you modify this)
            if (lists.isEmpty()) {
                // Hide lists spinner
                findViewById(R.id.listsSpinner).setVisibility(View.GONE);
                findViewById(R.id.noLists).setVisibility(View.VISIBLE);
            }

            initializeVotingSetupOnline();
        } else {
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

        if (items.size() < minimumItemCount) {
            // If offline, show toast
            if (!isOnlineVote) {
                Buddy.showToast(getString(R.string.toast_not_enough_activities,
                        minimumItemCount), Toast.LENGTH_LONG);
            }
            return;
        }

        selectedList.setItems(items); // Put loaded items to selected list
        listBigEnough = true;
    }

    //////////////////////// INITIALIZE VIEWS ////////////////////////

    private void initializeVotingSetupOffline() {
        // If tutorial has not been confirmed yet, show it
        if (GlobalPrefs.loadPopupInfo(GlobalPrefs.TUTORIAL_OFFLINE_VOTE))
            new HTMLDialog(this, HTMLDialog.HTMLText.TUTORIAL_OFFLINE_VOTE).show();

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
        // If tutorial has not been confirmed yet, show it
        if (GlobalPrefs.loadPopupInfo(GlobalPrefs.TUTORIAL_ONLINE_VOTE))
            new HTMLDialog(this, HTMLDialog.HTMLText.TUTORIAL_ONLINE_VOTE).show();

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
                // If user has not selected profiles, return
                if (selectedProfiles.isEmpty()) {
                    Buddy.showToast(Buddy.getString(R.string.toast_selected_profiles_empty), Toast.LENGTH_LONG);
                    return;
                }

                showListSettingDialog();
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
                if (!Buddy.isRegistered) {
                    // Don't allow hosting for unregistered users
                    Buddy.showToast(getString(R.string.unregistered_hosting), Toast.LENGTH_LONG);
                } else if (lists.isEmpty()) {
                    // If there are no lists, show toast
                    Buddy.showToast(getString(R.string.online_no_lists), Toast.LENGTH_LONG);
                } else if (onlineDetailsValid()) {
                    // If all online details are valid, show list settings
                    showListSettingDialog();
                }
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
     * Shows settings dialog for current list.
     */
    private void showListSettingDialog() {
        // If list is not big enough, return
        if (!listBigEnough) {
            Buddy.showToast(getString(R.string.toast_not_enough_activities,
                    minimumItemCount), Toast.LENGTH_LONG);
            return;
        }

        new ListSettingDialog(this, selectedList).show();
    }

    /**
     * Starts the correct voting after settings has been confirmed.
     * @param voteSettings
     */
    public void listSettingDialogConfirmed(VoteSettings voteSettings) {
        this.voteSettings = voteSettings;

        if (isOnlineVote)
            hostVoteRoom();
        else
            startLocalVoting();
    }

    /**
     * Starts local voting.
     */
    private void startLocalVoting() {
        // Move to voting top list
        Intent intent = new Intent(this, VoteTopActivity.class);
        intent.putExtra(VoteIntents.SETTINGS, voteSettings);
        intent.putExtra(VoteIntents.TOP_AMOUNT, voteSettings.getFirstVote());
        intent.putExtra(VoteIntents.LIST, selectedList);
        intent.putParcelableArrayListExtra(VoteIntents.PROFILES, selectedProfiles);
        startActivity(intent);
    }

    /**
     * Checks if room code and nickname is valid.
     * @return true if valid
     */
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

    /**
     * Hosts new vote room with given room code.
     */
    private void hostVoteRoom() {
        Buddy.showOnlineVoteLoadingBar(this);

        String roomCode = ((EditText) findViewById(R.id.roomCode)).getText().toString();
        // Create vote room
        final VoteRoom voteRoom = new VoteRoom(roomCode, selectedList.getName());

        // Add created vote room to database
        DatabaseHandler.addVoteRoom((added) -> {
            if (added) {
                // Add settings to vote room and move to lobby
                DatabaseHandler.addVoteRoomSettings(voteRoom, voteSettings,
                        () -> moveToOnlineLobby(voteRoom, LobbyJoinOptions.HOST_NEW));
            } else {
                Buddy.showToast(getString(R.string.online_host_duplicate), Toast.LENGTH_LONG);
                Buddy.hideOnlineVoteLoadingBar(this);
            }
        }, voteRoom);
    }

    /**
     * Joins vote room with given room code.
     */
    private void joinVoteRoom() {
        Buddy.showOnlineVoteLoadingBar(this);

        DatabaseHandler.getVoteRoom((voteRoom) -> {
            if (voteRoom != null) {
                Debug.print(this, "joinVoteRoom", "Vote Room found", 1);

                DatabaseHandler.getOnlineProfiles(voteRoom, (onlineProfiles -> {
                    // Loop through all profiles and check user id
                    for (OnlineProfile pro : onlineProfiles) {
                        if (pro.getDbID().equals(DatabaseHandler.getUserDbID())) {
                            onlineProfile = pro;
                            break;
                        }
                    }

                    // If user was already in the vote room
                    if (onlineProfile != null) {
                        Debug.print(this, "joinVoteRoom", "Reconnect", 1);
                        // Reconnect to vote room
                        tryConnectToVoteRoom(voteRoom, true);
                    } else {
                        if (voteRoom.getState() == VoteRoom.LOBBY) {
                            Debug.print(this, "joinVoteRoom", "Join as new user", 1);
                            // If voting has not yet started, join as new user
                            tryConnectToVoteRoom(voteRoom, false);
                        } else {
                            // Else vote has already started, so block joining
                            Buddy.showToast(getString(R.string.vote_already_started), Toast.LENGTH_LONG);
                            Buddy.hideOnlineVoteLoadingBar(this);
                        }
                    }
                }));
            } else {
                Buddy.showToast(getString(R.string.room_not_found), Toast.LENGTH_LONG);
                Buddy.hideOnlineVoteLoadingBar(this);
            }
        }, roomCode);
    }

    private void tryConnectToVoteRoom(VoteRoom voteRoom, boolean reconnecting) {
        int ownVersionCode = BuildConfig.VERSION_CODE;

        if (voteRoom.getVersionCode() == ownVersionCode) {
            // Load vote room settings and move to lobby
            DatabaseHandler.getVoteRoomSettings((settings) -> {
                voteSettings = settings;
                if (reconnecting)
                    reconnectToVoteRoom(voteRoom);
                else
                    moveToOnlineLobby(voteRoom, LobbyJoinOptions.JOIN_NEW);
            }, voteRoom);
        } else if (ownVersionCode < voteRoom.getVersionCode()) {
            Buddy.showToast(getString(R.string.joiner_update_required), Toast.LENGTH_LONG);
            Buddy.hideOnlineVoteLoadingBar(this);
        } else {
            Buddy.showToast(getString(R.string.host_update_required), Toast.LENGTH_LONG);
            Buddy.hideOnlineVoteLoadingBar(this);
        }
    }

    /**
     * Moves to online lobby.
     * @param voteRoom vote room to move to
     * @param lobbyJoinOptions how to join the vote room lobby
     */
    private void moveToOnlineLobby(VoteRoom voteRoom, LobbyJoinOptions lobbyJoinOptions) {
        boolean host = lobbyJoinOptions == LobbyJoinOptions.HOST_NEW;
        boolean reconnect = lobbyJoinOptions == LobbyJoinOptions.JOIN_RECONNECT;

        // Create online profile if not reconnecting
        if (!reconnect)
            onlineProfile = new OnlineProfile(DatabaseHandler.getUserDbID(), nickname, host);

        Intent intent = new Intent(this, VoteLobbyActivity.class);
        intent.putExtra(VoteIntents.ROOM, voteRoom);
        intent.putExtra(VoteIntents.SETTINGS, voteSettings);
        intent.putExtra(VoteIntents.ONLINE_PROFILE, onlineProfile);
        intent.putExtra(VoteIntents.RECONNECT, reconnect);

        Debug.print(this, "moveToOnlineLobby", onlineProfile.toString(), 1);

        // If hosting, add items to vote room
        if (host) {
            // Add items to vote room and move to lobby when items are added
            DatabaseHandler.addItemsToVoteRoom(voteRoom, selectedList.getItems(),
                    () -> startVote(voteRoom, intent));
        } else {
            startVote(voteRoom, intent);
        }
    }

    private void reconnectToVoteRoom(VoteRoom voteRoom) {
        Debug.print(this, "reconnectToVoteRoom", onlineProfile.toString(), 1);

        Intent intent;

        // Move to correct Activity
        switch (onlineProfile.getState()) {
            case VoteRoom.LOBBY:
                moveToOnlineLobby(voteRoom, LobbyJoinOptions.JOIN_RECONNECT);
                return;
            case VoteRoom.VOTING_FIRST:
            case VoteRoom.VOTING_LAST:
                intent = new Intent(this, VoteTopActivity.class);
                break;
            case VoteRoom.WAITING_FIRST:
            case VoteRoom.WAITING_LAST:
                intent = new Intent(this, VoteWaitingActivity.class);
                break;
            case VoteRoom.RESULTS_FIRST:
            case VoteRoom.RESULTS_LAST:
                intent = new Intent(this, VoteResultsActivity.class);
                break;
            default:
                Buddy.showToast(getString(R.string.reconnect_failed), Toast.LENGTH_LONG);
                return;
        }

        Debug.print(this, "reconnectToVoteRoom", "intent: " + intent, 1);

        intent.putExtra(VoteIntents.RECONNECT, true);
        intent.putExtra(VoteIntents.IS_ONLINE, true);
        intent.putExtra(VoteIntents.ROOM, voteRoom);
        intent.putExtra(VoteIntents.SETTINGS, voteSettings);
        intent.putExtra(VoteIntents.ONLINE_PROFILE, onlineProfile);
        intent.putExtra(VoteIntents.PROFILES, selectedProfiles);

        startVote(voteRoom, intent);
    }

    private void startVote(VoteRoom voteRoom, Intent intent) {
        Buddy.hideOnlineVoteLoadingBar(_this);

        // Start listening for vote room expiration
        DatabaseHandler.listenForVoteRoomExpiration(this, voteRoom.getRoomCode());
        DatabaseHandler.listenForUserLeavingRoom(this, voteRoom);

        finish();
        startActivity(intent);
    }
}
