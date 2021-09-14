package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import androidx.annotation.NonNull;
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
import io.github.tonimheinonen.whattodonext.database.VoteSettings;
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
    private VoteSettings voteSettings;
    private OnlineProfile onlineProfile;

    private ArrayList<OnlineProfile> users = new ArrayList<>();
    private ArrayList<OnlineProfile> notReadyUsers = new ArrayList();
    private DatabaseValueListAdapter usersAdapter;

    // Firebase listeners
    private ValueEventListener usersStateListener;

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
        voteSettings = intent.getParcelableExtra(VoteIntents.SETTINGS);
        onlineProfile = intent.getParcelableExtra(VoteIntents.ONLINE_PROFILE);

        Buddy.showOnlineVoteLoadingBar(this);
        setupLobby();
    }

    private void setupLobby() {
        // Setup list before adding and retrieving users
        setupUsersList();

        // Get all users and listen for changes
        createUsersListener();
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
        usersStateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print(_this, "createUsersListener", "onDataChange", 1);

                // Check if this is the first time that users are retrieved
                boolean firstTime = users.isEmpty();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    OnlineProfile loadedProfile = dataSnapshot.getValue(OnlineProfile.class);
                    loadedProfile.setDbID(dataSnapshot.getKey());

                    if (firstTime) {
                        Debug.print(_this, "createUsersListener", "First time", 1);
                        Buddy.hideOnlineVoteLoadingBar(_this);
                        users.add(loadedProfile);

                        // Add user to notReadyUsers if it is not ready
                        if (!checkIfProfileReady(loadedProfile))
                            notReadyUsers.add(loadedProfile);
                    }

                    // Remove from notReadyUsers if profile still there and it is now ready
                    if (notReadyUsers.contains(loadedProfile)) {
                        Debug.print(_this, "setupLobby", "Checking if ready: " + loadedProfile, 1);
                        if (checkIfProfileReady(loadedProfile)) {
                            Debug.print(_this, "setupLobby", "Removing: " + loadedProfile, 1);
                            notReadyUsers.remove(loadedProfile);
                        }
                    }
                }

                usersAdapter.notifyDataSetChanged();

                // Move to next activity if everyone is ready
                if (notReadyUsers.isEmpty())
                    moveToNextActivity();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print(_this, "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        };

        DatabaseHandler.listenForOnlineProfiles(voteRoom, usersStateListener);
    }

    private boolean checkIfProfileReady(OnlineProfile loadedProfile) {
        // Check in which waiting room we currently are at
        if (onlineProfile.getState() == VoteRoom.WAITING_FIRST) {
            // If currently at first waiting room
            if (loadedProfile.getState() >= VoteRoom.WAITING_FIRST)
                // If loadedProfile is at least at same point
                return true;
        } else {
            // Else we are currently at last waiting room
            if (loadedProfile.getState() >= VoteRoom.WAITING_LAST)
                // If loadedProfile is at least at same point
                return true;
        }

        return false;
    }

    private void moveToNextActivity() {
        if (movingToResults)
            return;

        Buddy.showOnlineVoteLoadingBar(_this);

        movingToResults = true;

        // Remove listener
        DatabaseHandler.stopListeningForOnlineProfiles(voteRoom, usersStateListener);

        // Change state and move to next activity
        DatabaseHandler.changeOnlineProfileState(voteRoom, onlineProfile, () -> {
            Intent intent = new Intent(this, VoteResultsActivity.class);
            intent.putExtra(VoteIntents.ONLINE_PROFILE, onlineProfile);
            intent.putExtra(VoteIntents.ROOM, voteRoom);
            intent.putExtra(VoteIntents.SETTINGS, voteSettings);
            intent.putExtra(VoteIntents.IS_ONLINE, true);
            intent.putExtra(VoteIntents.LIST, selectedList);

            Buddy.hideOnlineVoteLoadingBar(_this);

            startActivity(intent);
        });
    }

    /**
     * Override what happens when pressing back during voting.
     */
    @Override
    public void onBackPressed() {
        Buddy.exitVoting(this, () -> DatabaseHandler.disconnectOnlineProfile(voteRoom, onlineProfile));
    }
}
