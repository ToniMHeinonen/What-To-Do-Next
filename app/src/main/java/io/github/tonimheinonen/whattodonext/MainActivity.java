package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Starts selected activity.
     * @param v clicked button
     */
    public void menuButtonClicked(View v) {
        Intent intent;

        if (v.getId() == R.id.voteButton) {
            intent = new Intent(this, VoteActivity.class);
        } else if (v.getId() == R.id.listButton) {
            intent = new Intent(this, ListsActivity.class);
        } else if (v.getId() == R.id.settingsButton) {
            intent = new Intent(this, SettingsActivity.class);
        } else {
            return;
        }

        startActivity(intent);
    }
}
