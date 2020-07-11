package io.github.tonimheinonen.whattodonext.voteactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
import io.github.tonimheinonen.whattodonext.database.VoteRoom;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

public class VoteLobbyActivity extends AppCompatActivity implements View.OnClickListener {

    private VoteLobbyActivity _this;
    private VoteRoom voteRoom;
    private OnlineProfile onlineProfile;

    private ArrayList<OnlineProfile> users = new ArrayList<>();
    private DatabaseValueListAdapter usersAdapter;

    private int LOBBY = 1, WAITING = 2;
    private int votingState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_lobby);
        _this = this;

        Intent intent = getIntent();
        voteRoom = intent.getParcelableExtra("voteRoom");
        onlineProfile = intent.getParcelableExtra("onlineProfile");

        if (voteRoom.getState().equals(VoteRoom.LOBBY))
            votingState = LOBBY;
        else
            votingState = WAITING;

        if (votingState == LOBBY) {
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
                if (votingState == LOBBY) {
                    users.add(onlineProfile);
                    usersAdapter.notifyDataSetChanged();
                } else {
                    // If it's not the users profile
                    if (!onlineProfile.getUserID().equals(_this.onlineProfile.getUserID())) {
                        // If user is not ready yet, add it to the list
                        if (!onlineProfile.isReady()) {
                            users.add(onlineProfile);
                            usersAdapter.notifyDataSetChanged();
                        }
                    }
                }
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
                            users.remove(pro);
                            break;
                        }
                    }
                    usersAdapter.notifyDataSetChanged();
                }

                // Check if all users ready, then move host to results to calculate them
                if (_this.onlineProfile.isHost()) {
                    boolean allReady = true;

                    // Loop through all the users, if someone is not ready, set false
                    for (OnlineProfile pro : users) {
                        if (!pro.isReady()) {
                            allReady = false;
                            break;
                        }
                    }

                    if (allReady) {
                        Buddy.showLoadingBar(_this);

                        // Set correct voteroom state locally for VoteResultsActivity
                        if (voteRoom.getState().equals(VoteRoom.VOTING_FIRST))
                            voteRoom.setState(VoteRoom.RESULTS_FIRST);
                        else if (voteRoom.getState().equals(VoteRoom.VOTING_LAST))
                            voteRoom.setState(VoteRoom.RESULTS_LAST);

                        DatabaseHandler.getVoteRoomItems(voteRoom, (items) ->
                                moveToNextActivity(items, VoteResultsActivity.class));
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
                        DatabaseHandler.getVoteRoomItems(voteRoom, (items) ->
                                moveToNextActivity(items, VoteResultsActivity.class));
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
        usersAdapter = new DatabaseValueListAdapter(this, users, null,
                DatabaseType.ONLINE_PROFILE);
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

    private void moveToNextActivity(ArrayList<ListItem> items, Class<? extends Activity> activity) {
        // Reset user's ready state
        DatabaseHandler.setOnlineProfileReady(voteRoom, onlineProfile, false);

        // Create list of items so code does not have to be modified so much
        ListOfItems selectedList = new ListOfItems(voteRoom.getListName());
        selectedList.setDbID(items.get(0).getListID());
        selectedList.setItems(items);

        Intent intent = new Intent(this, activity);
        intent.putExtra("onlineProfile", onlineProfile);
        intent.putExtra("voteRoom", voteRoom);
        intent.putExtra("isOnline", true);
        intent.putExtra("selectedList", selectedList);
        startActivity(intent);
    }
}
