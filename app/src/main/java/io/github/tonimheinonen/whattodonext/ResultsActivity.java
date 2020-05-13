package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.savedresults.SavedResult;
import io.github.tonimheinonen.whattodonext.savedresults.SavedResultDialog;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        setupResultsList();
    }

    private void setupResultsList() {
        final ListView list = findViewById(R.id.savedResults);
        results = (ArrayList<SavedResult>) GlobalPrefs.loadResults();

        // If there are not saved results, show text
        if (results.isEmpty()) {
            findViewById(R.id.noResults).setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
            return;
        }

        // Sort by newest
        Collections.sort(results, (o1, o2) -> o2.date.compareTo(o1.date));

        DatabaseValueListAdapter adapter = new DatabaseValueListAdapter(this, results, this,
                DatabaseType.SAVED_RESULTS);
        list.setAdapter(adapter);
    }

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
