package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.OnlineProfile;
import io.github.tonimheinonen.whattodonext.database.VoteRoom;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.Debug;

public class VoteLobbyActivity extends VotingParentActivity implements View.OnClickListener {

    private VoteLobbyActivity _this;

    private VoteRoom voteRoom;
    private OnlineProfile onlineProfile;

    private ArrayList<OnlineProfile> users;
    private DatabaseValueListAdapter usersAdapter;

    // Listeners
    private ChildEventListener usersListener;
    private ValueEventListener voteRoomListener;

    private boolean reconnecting;
    private boolean setupOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Debug.print(this, "onCreate", "", 1);

        super.onCreate(savedInstanceState);
        _this = this;

        voteRoom = voteMaster.getVoteRoom();
        onlineProfile = voteMaster.getOnlineProfile();
        reconnecting = voteMaster.isReconnect();

        Debug.print(this, "onCreate", "reconnecting: " + reconnecting, 1);

        setContentView(R.layout.activity_vote_lobby);

        // Set room code
        ((TextView) findViewById(R.id.codeForRoom)).setText(voteRoom.getRoomCode());

        // Set onClick listeners for buttons
        Button startBtn = findViewById(R.id.start);
        findViewById(R.id.back).setOnClickListener(this);

        // Listen to start button clicks if host
        if (onlineProfile.isHost()) {
            startBtn.setOnClickListener(this);
            findViewById(R.id.waiting_for_host_lobby).setVisibility(View.GONE);
        } else {
            // Else hide button and show "waiting for host" text
            startBtn.setVisibility(View.GONE);
        }

        Buddy.showOnlineVoteLoadingBar(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Create necessary objects after onCreate and when app returns from background
        setupLobby();
    }

    @Override
    protected void onPause() {
        // Disconnect all listeners, so they are not called when app is backgrounded
        removeListeners();

        super.onPause();
    }

    /**
     * Initializes lobby activity.
     */
    private void setupLobby() {
        // Setup list before adding and retrieving users
        setupUsersList();

        // Add user's profile to the vote room if not reconnecting or recreating lobby
        if (!reconnecting && !setupOnce)
            DatabaseHandler.connectOnlineProfile(voteRoom, onlineProfile);

        createUsersListener();
        createRoomStateListener();

        setupOnce = true;
    }

    /**
     * Sets up list of users which are visible on screen.
     */
    private void setupUsersList() {
        users = new ArrayList<>();

        // Add users to ListView
        final ListView listView = findViewById(R.id.usersList);
        // Show all users in lobby
        usersAdapter = new DatabaseValueListAdapter(this, users, null,
                DatabaseType.ONLINE_PROFILE);
        listView.setAdapter(usersAdapter);
    }

    /**
     * Adds and removes users from list.
     */
    private void createUsersListener() {
        // Create listener for users who connect to the room
        usersListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Buddy.hideOnlineVoteLoadingBar(_this);
                OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);
                onlineProfile.setDbID(dataSnapshot.getKey());

                Debug.print(_this, "createUsersListener", "Added: " + onlineProfile, 1);

                users.add(onlineProfile);
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                OnlineProfile removedProfile = dataSnapshot.getValue(OnlineProfile.class);
                removedProfile.setDbID(dataSnapshot.getKey());

                // If host disconnected, leave the room
                if (removedProfile.isHost()) {
                    Debug.print(_this, "createUsersListener", "Host disconnected", 1);
                    Buddy.showToast(getString(R.string.host_disconnected), Toast.LENGTH_LONG);
                    removeListeners();
                    finish();
                } else {
                    Debug.print(_this, "createUsersListener", "User left", 1);
                    // Else remove user from visible list
                    for (OnlineProfile pro : users) {
                        // Check by database id
                        if (pro.getDbID().equals(removedProfile.getDbID())) {
                            users.remove(pro);
                            break;
                        }
                    }

                    usersAdapter.notifyDataSetChanged();
                }
            }

            // Unused
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };

        DatabaseHandler.listenForOnlineProfiles(voteRoom, usersListener);
    }

    /**
     * Listens for when vote has started.
     */
    private void createRoomStateListener() {
        // Create listener for users who connect to the room
        voteRoomListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer state = dataSnapshot.getValue(Integer.class);
                // Move to voting first when state is correct
                if (state != null && state >= VoteRoom.VOTING_FIRST) {
                    retrieveItemsAndMoveToVoting();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        DatabaseHandler.listenForVoteRoomState(voteRoom, voteRoomListener);
    }

    /**
     * Listens for clicks of views.
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
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

    /**
     * Override what happens when pressing back during voting.
     */
    @Override
    public void onBackPressed() {
        DatabaseHandler.disconnectOnlineProfile(voteRoom, onlineProfile);
        removeListeners();
        finish();
    }

    /**
     * Starts voting if able.
     */
    private void startVoting() {
        if (users.size() <= 1) {
            Buddy.showToast(getString(R.string.online_vote_solo), Toast.LENGTH_SHORT);
            return;
        }

        retrieveItemsAndMoveToVoting();
    }

    /**
     * Retrieves vote items from database and starts voting.
     */
    private void retrieveItemsAndMoveToVoting() {
        Buddy.showOnlineVoteLoadingBar(_this);
        DatabaseHandler.getVoteRoomItems(voteRoom, this::moveToNextActivity);
    }

    /**
     * Moves to voting activity.
     * @param items voting items
     */
    private void moveToNextActivity(ArrayList<ListItem> items) {
        removeListeners();

        DatabaseHandler.changeOnlineProfileState(voteRoom, onlineProfile, () -> {
            parentStartActivity(new Intent(this, VoteTopActivity.class));
        });
    }

    private void removeListeners() {
        if (usersListener != null)
            DatabaseHandler.stopListeningForOnlineProfiles(voteRoom, usersListener);

        if (voteRoomListener != null)
            DatabaseHandler.stopListeningForVoteRoomState(voteRoom, voteRoomListener);
    }
}
