package io.github.tonimheinonen.whattodonext;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.voteactivity.VoteSetupActivity;

public class StartVoteActivity extends AppCompatActivity
        implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_vote);

        // Set listeners for buttons
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.local).setOnClickListener(this);
        findViewById(R.id.online).setOnClickListener(this);
    }

    /**
     * Listens clicks of views.
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.local:
                SetupVote(false);
                break;
            case R.id.online:
                SetupVote(true);
                break;
            default:
                break;
        }
    }

    private void SetupVote(boolean online) {
        // Checks if the user is connected to the internet
        if (online) {
             if (!Buddy.isOnline(this)) {
                 Buddy.showToast(getString(R.string.no_internet), Toast.LENGTH_LONG);
                 return;
             }
        }

        // Move to setting up vote
        Intent intent = new Intent(this, VoteSetupActivity.class);
        intent.putExtra("isOnline", online);
        startActivity(intent);
    }
}
