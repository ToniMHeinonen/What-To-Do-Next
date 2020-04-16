package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

import android.content.Intent;
import android.os.Bundle;

public class InitializeActivity extends AppCompatActivity {

    /**
     * Initialize values which needs to be initialized only once.
     * @param savedInstanceState previous instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseHandler.initializePersistence();    // Set database offline persistence
        Buddy.initialize(this);               // Set context for Buddy

        // Start MainActivity and close this activity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
