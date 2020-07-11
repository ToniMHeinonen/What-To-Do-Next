package io.github.tonimheinonen.whattodonext.voteactivity;

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
import io.github.tonimheinonen.whattodonext.tools.Debug;

public class VoteLobbyActivity extends AppCompatActivity implements View.OnClickListener {

    private VoteLobbyActivity _this;
    private VoteRoom voteRoom;
    private OnlineProfile onlineProfile;

    private ArrayList<OnlineProfile> users = new ArrayList<>();
    private DatabaseValueListAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_lobby);
        _this = this;

        Intent intent = getIntent();
        voteRoom = intent.getParcelableExtra("voteRoom");
        onlineProfile = intent.getParcelableExtra("onlineProfile");

        // Set onClick listeners for buttons
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);

        Buddy.showLoadingBar(this);
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
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Buddy.hideLoadingBar(_this);
                OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);
                users.add(onlineProfile);
                usersAdapter.notifyDataSetChanged();
                Debug.print("listener", "onChildAdded", onlineProfile.getNickName(), 1);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);
                Debug.print("listener", "onChildRemoved", onlineProfile.getNickName(), 1);
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

            // Unused
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
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

                if (state != null && state.equals(VoteRoom.VOTING_FIRST)) {
                    Buddy.showLoadingBar(_this);
                    voteRoom.setState(state);
                    DatabaseHandler.getVoteRoomItems(voteRoom, (items) -> moveToVoting(items));
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

    private void moveToVoting(ArrayList<ListItem> items) {
        // Create list of items so code does not have to be modified so much
        ListOfItems selectedList = new ListOfItems(voteRoom.getListName());
        selectedList.setDbID(items.get(0).getListID());
        selectedList.setItems(items);

        Intent intent = new Intent(this, VoteTopActivity.class);
        intent.putExtra("onlineProfile", onlineProfile);
        intent.putExtra("voteRoom", voteRoom);
        intent.putExtra("isOnline", true);
        intent.putExtra("selectedList", selectedList);
        startActivity(intent);
    }
}
