package io.github.tonimheinonen.whattodonext;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;
import io.github.tonimheinonen.whattodonext.tools.HTMLDialog;

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
            Intent intent = new Intent(this, PreLoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            DatabaseHandler.initializeUserDatabase();
            GlobalPrefs.initialize(this, auth.getCurrentUser().getEmail());
            Buddy.isRegistered = true;

            // If first tutorial has not been confirmed yet, show it
            if (GlobalPrefs.loadPopupInfo(GlobalPrefs.TUTORIAL_WELCOME))
                new HTMLDialog(this, HTMLDialog.HTMLText.TUTORIAL_WELCOME).show();
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
        } else if (v.getId() == R.id.resultsButton) {
            intent = new Intent(this, ResultsActivity.class);
        } else {
            // Add else statement to fix possibility for null intent
            return;
        }

        startActivity(intent);
    }

    public void logOutClicked(View v) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, PreLoginActivity.class));
        finish();
    }
}
