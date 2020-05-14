package io.github.tonimheinonen.whattodonext.savedresults;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

/**
 * Handles showing saved results in a dialog.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.2
 * @since 1.2
 */
public class SavedResultDialog extends Dialog implements
        View.OnClickListener {

    private Activity activity;
    private SavedResult result;
    private ArrayList<SavedResultItem> resultItems;

    /**
     * Initializes necessary values.
     * @param a current activity
     * @param result saved result
     */
    public SavedResultDialog(Activity a, SavedResult result) {
        super(a);
        this.activity = a;
        this.result = result;
    }

    /**
     * Initializes views.
     * @param savedInstanceState saved state of dialog
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.saved_result_dialog);

        // Set dialog window size to 90% of the screen width and height
        int width = (int)(activity.getResources().getDisplayMetrics().widthPixels);
        int height = (int)(activity.getResources().getDisplayMetrics().heightPixels);
        getWindow().setLayout(width, height);

        // Set listeners for back button
        findViewById(R.id.back).setOnClickListener(this);

        initializeViews();
        DatabaseHandler.getResultItems(this::resultItemsLoaded, result);
    }

    /**
     * Gets loaded result items and adds them to a list.
     * @param resultItems loaded result items
     */
    private void resultItemsLoaded(ArrayList<SavedResultItem> resultItems) {
        // Set result item list
        final ListView list = findViewById(R.id.resultItems);
        DatabaseValueListAdapter adapter = new DatabaseValueListAdapter(activity, resultItems,
                null, DatabaseType.SAVED_RESULT_ITEM);
        list.setAdapter(adapter);
    }

    /**
     * Changes texts of views and shows necessary views.
     */
    private void initializeViews() {
        TextView dateView = findViewById(R.id.date);
        TextView listNameView = findViewById(R.id.listName);
        TextView voterView = findViewById(R.id.voters);

        // Set date
        dateView.setText(Buddy.formatResultDate(result.getDate()));

        // Set list name
        listNameView.setText(activity.getString(R.string.resultList, result.getListName()));

        // Set voters names
        voterView.setText(activity.getString(R.string.resultVoters, result.getVoters()));
    }

    /**
     * Listens for view clicks.
     * @param v clicked view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                dismiss();
                break;
        }
    }
}
