package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.database.OnlineProfile;
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
public class VoteWaitingActivity extends VotingParentActivity implements View.OnClickListener {

    private VoteWaitingActivity _this;

    private VoteRoom voteRoom;
    private OnlineProfile onlineProfile;

    private ArrayList<OnlineProfile> users = new ArrayList<>();
    private ArrayList<OnlineProfile> notReadyUsers = new ArrayList();
    private DatabaseValueListAdapter usersAdapter;

    // Firebase listeners
    private ValueEventListener usersStateListener;
    private ChildEventListener usersAddAndRemoveListener;

    // Used so moving does not get called multiple times
    private boolean movingToResults = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_waiting);
        _this = this;

        voteRoom = voteMaster.getVoteRoom();
        onlineProfile = voteMaster.getOnlineProfile();

        // Listen for back button
        findViewById(R.id.exit).setOnClickListener(this);

        Buddy.showOnlineVoteLoadingBar(this);
        setupLobby();

        // Listen for app going to background
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    private void setupLobby() {
        // Setup list before adding and retrieving users
        setupUsersList();

        // Retrieve all users and listen for user removal
        createUsersAddAndRemoveListener();

        // Listens for user state changes
        createUsersStateListener();
    }

    private void setupUsersList() {
        // Add users to ListView
        final ListView listView = findViewById(R.id.usersList);
        // Show not ready users in waiting room
        usersAdapter = new DatabaseValueListAdapter(this, notReadyUsers, null,
                DatabaseType.ONLINE_PROFILE);
        listView.setAdapter(usersAdapter);
    }

    private void createUsersAddAndRemoveListener() {
        usersAddAndRemoveListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Buddy.hideOnlineVoteLoadingBar(_this);
                OnlineProfile loadedProfile = dataSnapshot.getValue(OnlineProfile.class);
                loadedProfile.setDbID(dataSnapshot.getKey());

                Debug.print(_this, "createUsersListener", "Added: " + onlineProfile, 1);

                users.add(loadedProfile);

                // Add user to notReadyUsers if it is not ready
                if (!checkIfProfileReady(loadedProfile))
                    notReadyUsers.add(loadedProfile);

                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                OnlineProfile removedProfile = dataSnapshot.getValue(OnlineProfile.class);
                removedProfile.setDbID(dataSnapshot.getKey());

                Debug.print(_this, "createUsersListener", "Removed: " + onlineProfile, 1);

                users.remove(removedProfile);

                // Remove profile from not ready users and check if everyone is ready
                if (notReadyUsers.contains(removedProfile)) {
                    Debug.print(_this, "createUsersListener", "Removed from not ready list", 1);
                    notReadyUsers.remove(removedProfile);
                }

                checkIfEveryoneReady();

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

        DatabaseHandler.listenForOnlineProfiles(voteRoom, usersAddAndRemoveListener);
    }

    private void createUsersStateListener() {
        // Create listener for users who connect to the room
        usersStateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print(_this, "createUsersListener", "onDataChange", 1);

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    OnlineProfile loadedProfile = dataSnapshot.getValue(OnlineProfile.class);
                    loadedProfile.setDbID(dataSnapshot.getKey());

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
                checkIfEveryoneReady();
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

    /**
     * Listens for clicks of views.
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exit:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private void checkIfEveryoneReady() {
        if (notReadyUsers.isEmpty())
            moveToNextActivity();
    }

    private void moveToNextActivity() {
        if (movingToResults)
            return;

        Buddy.showOnlineVoteLoadingBar(_this);

        movingToResults = true;

        removeListeners();

        // Change state and move to next activity
        DatabaseHandler.changeOnlineProfileState(voteRoom, onlineProfile, () -> {
            parentStartActivity(new Intent(this, VoteResultsActivity.class));
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        // Disconnect all listeners, so they are not called when app is backgrounded
        removeListeners();
    }

    private void removeListeners() {
        if (usersStateListener != null)
            DatabaseHandler.stopListeningForOnlineProfiles(voteRoom, usersStateListener);

        if (usersAddAndRemoveListener != null)
            DatabaseHandler.stopListeningForOnlineProfiles(voteRoom, usersAddAndRemoveListener);
    }

    /**
     * Override what happens when pressing back during voting.
     */
    @Override
    public void onBackPressed() {
        Buddy.exitVoting(this, () -> {
            DatabaseHandler.disconnectOnlineProfile(voteRoom, onlineProfile);
            removeListeners();
            finish();
        });
    }
}
