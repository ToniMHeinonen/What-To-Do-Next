package io.github.tonimheinonen.whattodonext;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

public class StartVoteActivity extends AppCompatActivity
        implements View.OnClickListener {

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
                Buddy.moveToVoteSetup(this, false);
                break;
            case R.id.online:
                Buddy.moveToVoteSetup(this, true);
                break;
            default:
                break;
        }
    }
}
