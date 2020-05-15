package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.database.SavedResult;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Handles user selecting saved results to display.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.2
 * @since 1.2
 */
public class ResultsActivity extends AppCompatActivity implements View.OnClickListener {

    ArrayList<SavedResult> results;

    /**
     * Initializes ResultsActivity.
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Retrieve saved results
        Buddy.showLoadingBar(this, R.id.resultsLayout);
        DatabaseHandler.getResults(this::resultsLoaded);
    }

    /**
     * Initializes values after results are loaded from database.
     * @param savedResults loaded results
     */
    private void resultsLoaded(ArrayList<SavedResult> savedResults) {
        Collections.reverse(savedResults); // Reverse order to put newest at the top
        results = savedResults;
        setupResultsList();
        Buddy.hideLoadingBar(this, R.id.resultsLayout);
    }

    /**
     * Creates a list View of saved results.
     */
    private void setupResultsList() {
        final ListView list = findViewById(R.id.savedResults);

        // If there are not saved results, show text
        if (results.isEmpty()) {
            findViewById(R.id.noResults).setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
            return;
        }

        DatabaseValueListAdapter adapter = new DatabaseValueListAdapter(this, results, this,
                DatabaseType.SAVED_RESULTS);
        list.setAdapter(adapter);
    }

    /**
     * Opens correct saved result when it is clicked.
     * @param v clicked saved result view
     */
    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();

        new SavedResultDialog(this, results.get(position)).show();
    }

    /**
     * Listens for back button presses.
     * @param v back button
     */
    public void backSelected(View v) {
        super.onBackPressed();
    }
}
