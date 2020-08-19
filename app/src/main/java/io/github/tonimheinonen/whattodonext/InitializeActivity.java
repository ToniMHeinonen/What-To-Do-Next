package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

/**
 * Runs once when app starts, initializes necessary values.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0.2
 * @since 1.0
 */
public class InitializeActivity extends AppCompatActivity {

    private static Context context;

    /**
     * Initialize values which needs to be initialized only once.
     * @param savedInstanceState previous instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        // Set static context
        InitializeActivity.context = getApplicationContext();

        // Initialize vote room database
        DatabaseHandler.initializeVoteRoomDatabase();

        // Initialize default global prefs
        GlobalPrefs.initialize(this, "anonymous");

        // Start MainActivity and close this activity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Return static application context.
     * @return static application context
     */
    public static Context getAppContext() {
        return InitializeActivity.context;
    }
}
