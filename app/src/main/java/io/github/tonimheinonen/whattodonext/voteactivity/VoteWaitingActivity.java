package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import io.github.tonimheinonen.whattodonext.tools.Debug;

/**
 * Handles showing the waiting room between VoteTopActivity and VoteResultsActivity.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.3
 * @since 1.3
 */
public class VoteWaitingActivity extends VotingParentActivity {

    private VoteWaitingActivity _this;
    private ListOfItems selectedList;
    private VoteRoom voteRoom;
    private OnlineProfile onlineProfile;

    private ArrayList<OnlineProfile> users = new ArrayList<>();
    private ArrayList<OnlineProfile> notReadyUsers = new ArrayList<>();
    private DatabaseValueListAdapter usersAdapter;

    private ArrayList<OnlineVotedItem> votedItems;

    // Used so moving does not get called multiple times
    private boolean movingToResults = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_waiting);
        _this = this;

        // Lock orientation during voting to prevent million different problems
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        Intent intent = getIntent();
        selectedList = intent.getParcelableExtra(VoteIntents.LIST);
        voteRoom = intent.getParcelableExtra(VoteIntents.ROOM);
        onlineProfile = intent.getParcelableExtra(VoteIntents.ONLINE_PROFILE);

        Buddy.showOnlineVoteLoadingBar(this);
        setupLobby();
    }

    private void setupLobby() {
        // Setup list before adding and retrieving users
        setupUsersList();

        // Get all the users
        DatabaseHandler.getOnlineProfiles(voteRoom, (onlineProfiles -> {
            Buddy.hideOnlineVoteLoadingBar(_this);
            users = onlineProfiles;

            // Add not ready users
            for (OnlineProfile pro : users) {
                // If user is not ready yet, add it to the list
                if (!pro.isReady()) {
                    notReadyUsers.add(pro);
                }
            }

            usersAdapter.notifyDataSetChanged();

            // Listen for ready states if host
            if (onlineProfile.isHost()) {
                if (notReadyUsers.isEmpty()) {
                    // If all other users are ready, move to results
                    Debug.print(this, "setupLobby", "prepareMoveToResults", 1);
                    prepareMoveToResults();
                } else {
                    // Else wait and listen for other user ready changes
                    createUsersListener();
                }
            }

            // Listen for room state changes to know when to move to results
            createRoomStateListener();
        }));
    }

    private void setupUsersList() {
        // Add users to ListView
        final ListView listView = findViewById(R.id.usersList);
        // Show not ready users in waiting room
        usersAdapter = new DatabaseValueListAdapter(this, notReadyUsers, null,
                DatabaseType.ONLINE_PROFILE);
        listView.setAdapter(usersAdapter);
    }

    private void createUsersListener() {
        // Create listener for users who connect to the room
        ChildEventListener childEventListener = new ChildEventListener() {

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Remove from list when user becomes ready
                OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);
                onlineProfile.setDbID(dataSnapshot.getKey());

                if (onlineProfile.isReady()) {
                    for (OnlineProfile pro : notReadyUsers) {
                        // Check by db id
                        if (pro.getDbID().equals(onlineProfile.getDbID())) {
                            Debug.print(_this, "remove ", pro.getNickName(), 1);
                            notReadyUsers.remove(pro);
                            Debug.print(_this, "size ", "" + notReadyUsers.size(), 1);

                            // If all the users are ready, move host to results to calculate them
                            if (notReadyUsers.isEmpty()) {
                                Debug.print(_this, "createUsersListener", "prepareMoveToResults", 1);
                                prepareMoveToResults();
                            }
                            break;
                        }
                    }
                    usersAdapter.notifyDataSetChanged();
                }


            }

            // Unused
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        DatabaseHandler.listenForOnlineProfiles(voteRoom, childEventListener);
    }

    private void createRoomStateListener() {
        // Create listener for users who are not the host
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String state = dataSnapshot.getValue(String.class);

                if (state != null && (state.equals(VoteRoom.RESULTS_FIRST) ||
                            state.equals(VoteRoom.RESULTS_LAST))) {
                    Buddy.showOnlineVoteLoadingBar(_this);
                    voteRoom.setState(state);

                    retrieveVotedItemsAndMoveToResults();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        DatabaseHandler.listenForVoteRoomState(voteRoom, eventListener);
    }

    private void prepareMoveToResults() {
        Buddy.showOnlineVoteLoadingBar(_this);

        DatabaseHandler.getVoteRoomState(voteRoom, (state) -> {
            // Change vote room state
            if (state.equals(VoteRoom.VOTING_FIRST))
                DatabaseHandler.changeVoteRoomState(voteRoom, VoteRoom.RESULTS_FIRST);
            else if (state.equals(VoteRoom.VOTING_LAST))
                DatabaseHandler.changeVoteRoomState(voteRoom, VoteRoom.RESULTS_LAST);
        });
    }

    private void retrieveVotedItemsAndMoveToResults() {
        DatabaseHandler.getVoteRoomVotedItems(voteRoom, (votedItems) -> {
            this.votedItems = votedItems;
            moveToNextActivity();
        });
    }

    private void moveToNextActivity() {
        if (movingToResults)
            return;

        movingToResults = true;

        Intent intent = new Intent(this, VoteResultsActivity.class);
        intent.putExtra(VoteIntents.ONLINE_PROFILE, onlineProfile);
        intent.putExtra(VoteIntents.ROOM, voteRoom);
        intent.putExtra(VoteIntents.IS_ONLINE, true);
        intent.putExtra(VoteIntents.LIST, selectedList);

        // Create SelectedProfiles so code does not have to be modified so much
        ArrayList<Profile> selectedProfiles = createProfilesFromOnlineProfile(selectedList.getItems());

        intent.putExtra(VoteIntents.PROFILES, selectedProfiles);

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
                if (votedItem.getUserID().equals(pro.getDbID())) {
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
