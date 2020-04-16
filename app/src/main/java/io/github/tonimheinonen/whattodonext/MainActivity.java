package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.registration.LoginActivity;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Handles moving from one activity to another.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Initializes necessary values.
     * @param savedInstanceState previous Activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Buddy.initialize(this);
            DatabaseHandler.initialize();
            GlobalPrefs.initialize(this, auth.getCurrentUser().getEmail());
            Buddy.showToast("Logged in", Toast.LENGTH_SHORT);
        }
    }

    /**
     * Starts selected activity.
     * @param v clicked button
     */
    public void menuButtonClicked(View v) {
        Intent intent;

        if (v.getId() == R.id.voteButton) {
            intent = new Intent(this, StartVoteActivity.class);
        } else if (v.getId() == R.id.listButton) {
            intent = new Intent(this, ListsActivity.class);
        } else if (v.getId() == R.id.settingsButton) {
            intent = new Intent(this, SettingsActivity.class);
        } else {
            // Add else statement to fix possibility for null intent
            return;
        }

        startActivity(intent);
    }

    public void logOutClicked(View v) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
