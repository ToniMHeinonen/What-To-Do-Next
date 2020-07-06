package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.database.OnlineProfile;
import io.github.tonimheinonen.whattodonext.database.VoteRoom;
import io.github.tonimheinonen.whattodonext.tools.Debug;

public class VoteLobbyActivity extends AppCompatActivity {

    private VoteRoom voteRoom;
    private OnlineProfile profile;

    private ArrayList<OnlineProfile> users = new ArrayList<>();
    private DatabaseValueListAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_lobby);

        Intent intent = getIntent();
        voteRoom = intent.getParcelableExtra("voteRoom");
        profile = intent.getParcelableExtra("onlineProfile");

        SetupLobby();
    }

    private void SetupLobby() {
        // Setup list before adding and retrieving users
        SetupUsersList();

        // Add user's profile to the voteroom
        DatabaseHandler.addOnlineProfile(voteRoom, profile);

        // Create listener for users who connect to the room
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
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

    private void SetupUsersList() {
        // Add users to ListView
        final ListView listView = findViewById(R.id.usersList);
        usersAdapter = new DatabaseValueListAdapter(this, users, null,
                DatabaseType.ONLINE_PROFILE);
        listView.setAdapter(usersAdapter);
    }
}
