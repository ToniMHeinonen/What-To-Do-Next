package io.github.tonimheinonen.whattodonext.voteactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.database.OnlineProfile;
import io.github.tonimheinonen.whattodonext.database.OnlineVotedItem;
import io.github.tonimheinonen.whattodonext.database.Profile;
import io.github.tonimheinonen.whattodonext.database.VoteRoom;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

public class VoteLobbyActivity extends AppCompatActivity implements View.OnClickListener {

    private VoteLobbyActivity _this;
    private VoteRoom voteRoom;
    private OnlineProfile onlineProfile;

    private ArrayList<OnlineProfile> users = new ArrayList<>();
    private ArrayList<OnlineProfile> notReadyUsers = new ArrayList<>();
    private DatabaseValueListAdapter usersAdapter;

    // Used when moving to results
    private ArrayList<OnlineVotedItem> votedItems;

    private int LOBBY = 1, WAITING = 2;
    private int votingState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _this = this;

        Intent intent = getIntent();
        voteRoom = intent.getParcelableExtra("voteRoom");
        onlineProfile = intent.getParcelableExtra("onlineProfile");

        if (voteRoom.getState().equals(VoteRoom.LOBBY)) {
            votingState = LOBBY;
            setContentView(R.layout.activity_vote_lobby);
        } else {
            votingState = WAITING;
            setContentView(R.layout.activity_vote_waiting);
        }

        if (votingState == LOBBY) {
            // Set room code
            ((TextView) findViewById(R.id.codeForRoom)).setText(voteRoom.getRoomCode());
            // Set onClick listeners for buttons
            findViewById(R.id.back).setOnClickListener(this);
            findViewById(R.id.start).setOnClickListener(this);
        }

        Buddy.showLoadingBar(this);
        setupLobby();
    }

    private void setupLobby() {
        // Setup list before adding and retrieving users
        setupUsersList();

        // Add user's profile to the voteroom
        if (votingState == LOBBY)
            DatabaseHandler.connectOnlineProfile(voteRoom, onlineProfile);
        else
            DatabaseHandler.setOnlineProfileReady(voteRoom, onlineProfile, true);

        createUsersListener();
        createRoomStateListener();
    }

    private void createUsersListener() {
        // Create listener for users who connect to the room
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Buddy.hideLoadingBar(_this);
                OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);
                onlineProfile.setDbID(dataSnapshot.getKey());
                users.add(onlineProfile);
                if (votingState == WAITING) {
                    // If it's not the users profile
                    if (!onlineProfile.getUserID().equals(_this.onlineProfile.getUserID())) {
                        // If user is not ready yet, add it to the list
                        if (!onlineProfile.isReady()) {
                            notReadyUsers.add(onlineProfile);
                        }
                    }
                }
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);

                for (OnlineProfile pro : users) {
                    // Check user id and nick name, in finished product id check is enough
                    if (pro.getUserID().equals(onlineProfile.getUserID()) &&
                            pro.getNickName().equals(onlineProfile.getNickName())) {
                        users.remove(pro);
                        break;
                    }
                }

                // Remove not ready users if user disconnects
                if (votingState == WAITING) {
                    for (OnlineProfile pro : notReadyUsers) {
                        // Check user id and nick name, in finished product id check is enough
                        if (pro.getUserID().equals(onlineProfile.getUserID()) &&
                                pro.getNickName().equals(onlineProfile.getNickName())) {
                            notReadyUsers.remove(pro);
                            break;
                        }
                    }
                }
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (votingState != WAITING)
                    return;

                // Remove from list when user becomes ready
                OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);
                if (onlineProfile.isReady()) {
                    for (OnlineProfile pro : users) {
                        // Check user id and nick name, in finished product id check is enough
                        if (pro.getUserID().equals(onlineProfile.getUserID()) &&
                                pro.getNickName().equals(onlineProfile.getNickName())) {
                            notReadyUsers.remove(pro);
                            break;
                        }
                    }
                    usersAdapter.notifyDataSetChanged();
                }

                // Check if all users ready, then move host to results to calculate them
                if (_this.onlineProfile.isHost()) {
                    // If all the users are ready
                    if (notReadyUsers.isEmpty()) {
                        Buddy.showLoadingBar(_this);

                        // Loop through all profiles and reset the ready state
                        for (OnlineProfile pro : users) {
                            DatabaseHandler.setOnlineProfileReady(voteRoom, pro, false);
                        }

                        // Set correct voteroom state locally for VoteResultsActivity
                        if (voteRoom.getState().equals(VoteRoom.VOTING_FIRST))
                            voteRoom.setState(VoteRoom.RESULTS_FIRST);
                        else if (voteRoom.getState().equals(VoteRoom.VOTING_LAST))
                            voteRoom.setState(VoteRoom.RESULTS_LAST);

                        retrieveVotedItemsAndMoveToResults();
                    }
                }
            }

            // Unused
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        DatabaseHandler.getOnlineProfiles(voteRoom, childEventListener);
    }

    private void createRoomStateListener() {
        // Create listener for users who connect to the room
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String state = dataSnapshot.getValue(String.class);

                if (votingState == LOBBY) {
                    // Move to voting first when state is correct
                    if (state != null && state.equals(VoteRoom.VOTING_FIRST)) {
                        Buddy.showLoadingBar(_this);
                        voteRoom.setState(state);
                        DatabaseHandler.getVoteRoomItems(voteRoom, (items) ->
                                moveToNextActivity(items, VoteTopActivity.class));
                    }
                } else if (votingState == WAITING) {
                    if (state != null && (state.equals(VoteRoom.RESULTS_FIRST) ||
                            state.equals(VoteRoom.RESULTS_LAST))) {
                        Buddy.showLoadingBar(_this);
                        voteRoom.setState(state);

                        retrieveVotedItemsAndMoveToResults();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        DatabaseHandler.getVoteRoomState(voteRoom, eventListener);
    }

    private void setupUsersList() {
        // Add users to ListView
        final ListView listView = findViewById(R.id.usersList);
        if (votingState == LOBBY) {
            // Show all users in lobby
            usersAdapter = new DatabaseValueListAdapter(this, users, null,
                    DatabaseType.ONLINE_PROFILE);
        } else {
            // Show not ready users in waiting room
            usersAdapter = new DatabaseValueListAdapter(this, notReadyUsers, null,
                    DatabaseType.ONLINE_PROFILE);
        }
        listView.setAdapter(usersAdapter);
    }

    /**
     * Listens for clicks of views.
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                DatabaseHandler.disconnectOnlineProfile(voteRoom, onlineProfile);
                onBackPressed();
                break;
            case R.id.start:
                if (onlineProfile.isHost()) {
                    startVoting();
                } else {
                    Buddy.showToast(getString(R.string.host_allowed_start), Toast.LENGTH_SHORT);
                }
                break;
            default:
                break;
        }
    }

    private void startVoting() {
        ArrayList<ListItem> items = getIntent().getParcelableArrayListExtra("items");
        DatabaseHandler.addItemsToVoteRoom(voteRoom, items);
        Buddy.showLoadingBar(this);
    }

    private void retrieveVotedItemsAndMoveToResults() {
        DatabaseHandler.getVoteRoomVotedItems(voteRoom, (votedItems) -> {
            this.votedItems = votedItems;
            DatabaseHandler.getVoteRoomItems(voteRoom, (items) ->
                    moveToNextActivity(items, VoteResultsActivity.class));
        });
    }

    private void moveToNextActivity(ArrayList<ListItem> items, Class<? extends Activity> activity) {
        // Create list of items so code does not have to be modified so much
        ListOfItems selectedList = new ListOfItems(voteRoom.getListName());
        selectedList.setDbID(items.get(0).getListID());
        selectedList.setItems(items);

        Intent intent = new Intent(this, activity);
        intent.putExtra("onlineProfile", onlineProfile);
        intent.putExtra("voteRoom", voteRoom);
        intent.putExtra("isOnline", true);
        intent.putExtra("selectedList", selectedList);

        // If moving to results, create SelectedProfiles
        if (activity.equals(VoteResultsActivity.class)) {
            ArrayList selectedProfiles = createProfilesFromOnlineProfile(items);

            intent.putExtra("selectedProfiles", selectedProfiles);
        }

        startActivity(intent);
    }

    private ArrayList<Profile> createProfilesFromOnlineProfile(ArrayList<ListItem> items) {
        ArrayList<Profile> selectedProfiles = new ArrayList<>();

        // Create normal profile from online users so the same code can be used in results
        for (OnlineProfile pro : users) {
            // Create profile from the user's nickname
            Profile profile = new Profile(pro.getNickName());

            // Set correct vote points size depending on if it's the firs or last results
            if (voteRoom.getState().equals(VoteRoom.RESULTS_FIRST))
                profile.initVoteSize(voteRoom.getFirstVoteSize());
            else
                profile.initVoteSize(voteRoom.getLastVoteSize());

            // Iterate through all of the voted items so they can be deleted during iteration
            Iterator<OnlineVotedItem> iterator = votedItems.iterator();
            while (iterator.hasNext()) {
                OnlineVotedItem votedItem = iterator.next();

                // If voted item belongs to the current user, proceed...
                if (votedItem.getUserID().equals(pro.getUserID())) {
                    // Loop through all the items and find which item has the same id as voted item
                    for (ListItem item : items) {
                        // If item has the same id, add it to the profile's vote items
                        if (item.getDbID().equals(votedItem.getItemID())) {
                            int index = votedItem.getVotePoints() - 1; // -1 since array starts from 0
                            profile.addVoteItem(index, item);
                            break;
                        }
                    }

                    // Remove the item afterwards so it does not need to be looped through again
                    iterator.remove();
                }
            }

            selectedProfiles.add(profile);
        }

        return selectedProfiles;
    }
}
