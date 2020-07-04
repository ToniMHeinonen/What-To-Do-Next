package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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
import io.github.tonimheinonen.whattodonext.tools.Buddy;

public class VoteLobbyActivity extends AppCompatActivity {

    private String roomCode;
    private OnlineProfile profile;

    private ArrayList<OnlineProfile> users;
    private DatabaseValueListAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_lobby);

        Intent intent = getIntent();
        roomCode = intent.getStringExtra("roomCode");
        profile = intent.getParcelableExtra("onlineProfile");

        DatabaseHandler.getVoteRoom((room) -> OnGetVoteRoom(room), roomCode);
    }

    private void OnGetVoteRoom(VoteRoom voteRoom) {
        if (voteRoom != null) {
            DatabaseHandler.addOnlineProfile(voteRoom, profile);

            ChildEventListener childEventListener = new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);
                    users.add(onlineProfile);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);
                    users.remove(onlineProfile);
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            };
            DatabaseHandler.getOnlineProfiles(voteRoom, childEventListener);

        } else {
            Buddy.showToast(getString(R.string.vote_room_error), Toast.LENGTH_LONG);
            finish();
        }
    }

    /*private void SetupUsersList() {
        // Add users to ListView
        final ListView listView = findViewById(R.id.usersList);
        usersAdapter = new DatabaseValueListAdapter(this, users, null,
                DatabaseType.ONLINE_USER);
        profileListView.setAdapter(profileListAdapter);
    }*/
}
