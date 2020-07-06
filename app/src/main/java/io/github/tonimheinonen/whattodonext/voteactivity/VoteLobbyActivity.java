package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.database.OnlineProfile;
import io.github.tonimheinonen.whattodonext.database.VoteRoom;

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
        // Add user's profile to the voteroom
        DatabaseHandler.addOnlineProfile(voteRoom, profile);

        // Create listener for users who connect to the room
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);
                users.add(onlineProfile);
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);
                users.remove(onlineProfile);
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        DatabaseHandler.getOnlineProfiles(voteRoom, childEventListener);
    }

    /*private void SetupUsersList() {
        // Add users to ListView
        final ListView listView = findViewById(R.id.usersList);
        usersAdapter = new DatabaseValueListAdapter(this, users, null,
                DatabaseType.ONLINE_USER);
        profileListView.setAdapter(profileListAdapter);
    }*/
}
