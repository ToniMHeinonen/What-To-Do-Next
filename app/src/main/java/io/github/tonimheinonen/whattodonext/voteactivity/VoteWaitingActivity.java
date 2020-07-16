package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
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
import io.github.tonimheinonen.whattodonext.tools.Debug;

public class VoteWaitingActivity extends AppCompatActivity {

    private VoteWaitingActivity _this;
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

        Intent intent = getIntent();
        voteRoom = intent.getParcelableExtra("voteRoom");
        onlineProfile = intent.getParcelableExtra("onlineProfile");

        Buddy.showLoadingBar(this);
        setupLobby();
    }

    private void setupLobby() {
        // Setup list before adding and retrieving users
        setupUsersList();

        // Change user's ready state
        DatabaseHandler.setOnlineProfileReady(voteRoom, onlineProfile, true);

        // Get all the users
        DatabaseHandler.getOnlineProfilesOnce(voteRoom, (onlineProfiles -> {
            Buddy.hideLoadingBar(_this);
            users = onlineProfiles;

            // Add not ready users
            for (OnlineProfile pro : users) {
                // If it's not the users profile
                if (!pro.getUserID().equals(onlineProfile.getUserID())) {
                    // If user is not ready yet, add it to the list
                    if (!pro.isReady()) {
                        notReadyUsers.add(pro);
                    }
                }
            }

            usersAdapter.notifyDataSetChanged();

            // Listen for ready states if host
            if (onlineProfile.isHost()) {
                if (notReadyUsers.isEmpty()) {
                    // If all other users are ready, move to results
                    moveHostToResults();
                } else {
                    // Else wait and listen for other user ready changes
                    createUsersListener();
                }
            } else {
                // Listen for room state changes if not host
                createRoomStateListener();
            }
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
                if (onlineProfile.isReady()) {
                    for (OnlineProfile pro : notReadyUsers) {
                        // Check user id and nick name, in finished product id check is enough
                        if (pro.getUserID().equals(onlineProfile.getUserID()) &&
                                pro.getNickName().equals(onlineProfile.getNickName())) {
                            notReadyUsers.remove(pro);
                            break;
                        }
                    }
                    usersAdapter.notifyDataSetChanged();
                }

                // If all the users are ready, move host to results to calculate them
                if (notReadyUsers.isEmpty())
                    moveHostToResults();
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
        DatabaseHandler.getOnlineProfiles(voteRoom, childEventListener);
    }

    private void createRoomStateListener() {
        // Create listener for users who are not the host
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String state = dataSnapshot.getValue(String.class);

                if (state != null && (state.equals(VoteRoom.RESULTS_FIRST) ||
                            state.equals(VoteRoom.RESULTS_LAST))) {
                    Buddy.showLoadingBar(_this);
                    voteRoom.setState(state);

                    retrieveVotedItemsAndMoveToResults();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        DatabaseHandler.getVoteRoomState(voteRoom, eventListener);
    }

    private void moveHostToResults() {
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

    private void retrieveVotedItemsAndMoveToResults() {
        DatabaseHandler.getVoteRoomVotedItems(voteRoom, (votedItems) -> {
            this.votedItems = votedItems;
            DatabaseHandler.getVoteRoomItems(voteRoom, (items) ->
                    moveToNextActivity(items));
        });
    }

    private void moveToNextActivity(ArrayList<ListItem> items) {
        if (movingToResults)
            return;

        movingToResults = true;
        // Create list of items so code does not have to be modified so much
        ListOfItems selectedList = new ListOfItems(voteRoom.getListName());
        selectedList.setDbID(items.get(0).getListID());
        selectedList.setItems(items);

        Intent intent = new Intent(this, VoteResultsActivity.class);
        intent.putExtra("onlineProfile", onlineProfile);
        intent.putExtra("voteRoom", voteRoom);
        intent.putExtra("isOnline", true);
        intent.putExtra("selectedList", selectedList);

        // Create SelectedProfiles so code does not have to be modified so much
        ArrayList<Profile> selectedProfiles = createProfilesFromOnlineProfile(items);
        Debug.print(this, "add intent", selectedProfiles.toString(), 1);

        intent.putExtra("selectedProfiles", selectedProfiles);

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

                Debug.print(this, "createProfile", "has next", 1);
                // If voted item belongs to the current user, proceed...
                if (votedItem.getUserID().equals(pro.getUserID())) {
                    // Loop through all the items and find which item has the same id as voted item
                    Debug.print(this, "createProfile", "Equals " + votedItem.toString(), 1);
                    for (ListItem item : items) {
                        // If item has the same id, add it to the profile's vote items
                        if (item.getDbID().equals(votedItem.getItemID())) {
                            int index = votedItem.getVotePoints() - 1; // -1 since array starts from 0
                            profile.addVoteItem(index, item);
                            Debug.print(this, "createProfile", "Same id " + item.toString(), 1);
                            break;
                        }
                    }

                    // Remove the item afterwards so it does not need to be looped through again
                    iterator.remove();
                }
            }
            Debug.print(this, "createProfile", profile.toString(), 1);

            selectedProfiles.add(profile);
        }

        return selectedProfiles;
    }
}
