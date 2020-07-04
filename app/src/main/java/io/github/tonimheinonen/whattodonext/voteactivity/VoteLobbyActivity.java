package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.database.OnlineProfile;

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

    }

    /*private void SetupUsersList() {
        // Add users to ListView
        final ListView listView = findViewById(R.id.usersList);
        usersAdapter = new DatabaseValueListAdapter(this, users, null,
                DatabaseType.ONLINE_USER);
        profileListView.setAdapter(profileListAdapter);
    }*/
}
