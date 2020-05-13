package io.github.tonimheinonen.whattodonext;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

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

        initializeViews();

        // Set listeners for confirm and cancel
        findViewById(R.id.back).setOnClickListener(this);
    }

    /**
     * Changes texts of views and shows necessary views.
     */
    private void initializeViews() {
        TextView dateView = findViewById(R.id.date);
        TextView listNameView = findViewById(R.id.listName);
        TextView voterView = findViewById(R.id.voters);

        // Set date
        dateView.setText(Buddy.formatResultDate(result.date));
        listNameView.setText(activity.getString(R.string.resultList, result.listName));

        // Set voters names
        StringBuilder voters = new StringBuilder(activity.getString(R.string.resultVoters));
        String prefix = " ";
        for (String v : result.voterNames) {
            voters.append(prefix).append(v);
            prefix = ", ";
        }
        voterView.setText(voters);

        // Set result item list
        final ListView list = findViewById(R.id.resultItems);
        DatabaseValueListAdapter adapter = new DatabaseValueListAdapter(activity, result.resultItems,
                null, DatabaseType.SAVED_RESULT_ITEM);
        list.setAdapter(adapter);
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
