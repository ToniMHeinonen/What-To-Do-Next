package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

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

        results = (ArrayList) GlobalPrefs.loadResults();
        setupResultsList();
    }

    private void setupResultsList() {
        final ListView list = findViewById(R.id.savedProfiles);
        DatabaseValueListAdapter adapter = new DatabaseValueListAdapter(this, results, null,
                DatabaseType.SAVED_RESULTS);
        list.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();

        new SavedResultDialog(this, results.get(position));
    }
}
