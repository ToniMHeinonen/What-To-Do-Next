package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.database.OnlineProfile;
import io.github.tonimheinonen.whattodonext.database.VoteRoom;
import io.github.tonimheinonen.whattodonext.database.VoteSettings;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.Debug;

public class VoteLobbyActivity extends VotingParentActivity implements View.OnClickListener {

    private VoteLobbyActivity _this;
    private VoteRoom voteRoom;
    private VoteSettings voteSettings;
    private OnlineProfile onlineProfile;

    private ArrayList<OnlineProfile> users = new ArrayList<>();
    private DatabaseValueListAdapter usersAdapter;
    private ChildEventListener usersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _this = this;

        // Lock orientation during voting to prevent million different problems
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        Intent intent = getIntent();
        voteRoom = intent.getParcelableExtra(VoteIntents.ROOM);
        voteSettings = intent.getParcelableExtra(VoteIntents.SETTINGS);
        onlineProfile = intent.getParcelableExtra(VoteIntents.ONLINE_PROFILE);

        setContentView(R.layout.activity_vote_lobby);

        // Listen for vote room removal if not host
        if (!onlineProfile.isHost())
            DatabaseHandler.listenForVoteRoomDeletion(this, voteRoom.getRoomCode());

        // Set room code
        ((TextView) findViewById(R.id.codeForRoom)).setText(voteRoom.getRoomCode());
        // Set onClick listeners for buttons
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);

        Buddy.showOnlineVoteLoadingBar(this);
        setupLobby();
    }

    private void setupLobby() {
        // Setup list before adding and retrieving users
        setupUsersList();

        // Add user's profile to the voteroom
        DatabaseHandler.connectOnlineProfile(voteRoom, onlineProfile);

        createUsersListener();
        createRoomStateListener();
    }

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
                for (OnlineProfile pro : users) {
                    // Check by database id
                    if (pro.getDbID().equals(dataSnapshot.getKey())) {
                        users.remove(pro);
                        break;
                    }
                }

                usersAdapter.notifyDataSetChanged();
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

    private void createRoomStateListener() {
        // Create listener for users who connect to the room
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer state = dataSnapshot.getValue(Integer.class);
                // Move to voting first when state is correct
                if (state != null && state == VoteRoom.VOTING_FIRST) {
                    Buddy.showOnlineVoteLoadingBar(_this);
                    voteRoom.setState(state);
                    DatabaseHandler.getVoteRoomItems(voteRoom, (items) ->
                            moveToNextActivity(items));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        DatabaseHandler.listenForVoteRoomState(voteRoom, eventListener);
    }

    private void setupUsersList() {
        // Add users to ListView
        final ListView listView = findViewById(R.id.usersList);
        // Show all users in lobby
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
        if (users.size() <= 1) {
            Buddy.showToast(getString(R.string.online_vote_solo), Toast.LENGTH_SHORT);
            return;
        }

        ArrayList<ListItem> items = getIntent().getParcelableArrayListExtra(VoteIntents.ITEMS);
        DatabaseHandler.addItemsToVoteRoom(voteRoom, items);
        Buddy.showOnlineVoteLoadingBar(this);
    }

    private void moveToNextActivity(ArrayList<ListItem> items) {
        // Create list of items so code does not have to be modified so much
        ListOfItems selectedList = new ListOfItems(voteRoom.getListName());
        selectedList.setDbID(items.get(0).getListID());
        selectedList.setItems(items);

        // Stop listening for new users
        DatabaseHandler.stopListeningForOnlineProfiles(voteRoom, usersListener);

        DatabaseHandler.changeOnlineProfileState(voteRoom, onlineProfile, () -> {
            Intent intent = new Intent(this, VoteTopActivity.class);
            intent.putExtra(VoteIntents.ONLINE_PROFILE, onlineProfile);
            intent.putExtra(VoteIntents.ROOM, voteRoom);
            intent.putExtra(VoteIntents.SETTINGS, voteSettings);
            intent.putExtra(VoteIntents.IS_ONLINE, true);
            intent.putExtra(VoteIntents.LIST, selectedList);

            startActivity(intent);
        });
    }
}
