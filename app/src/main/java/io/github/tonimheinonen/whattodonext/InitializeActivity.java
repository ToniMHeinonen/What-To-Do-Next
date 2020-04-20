package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Runs once when app starts, initializes necessary values.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0.2
 * @since 1.0
 */
public class InitializeActivity extends AppCompatActivity {

    /**
     * Initialize values which needs to be initialized only once.
     * @param savedInstanceState previous instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Buddy.initialize(this);               // Set context for Buddy

        // Start MainActivity and close this activity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
