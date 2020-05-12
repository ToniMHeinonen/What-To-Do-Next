package io.github.tonimheinonen.whattodonext;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

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
        int width = (int)(activity.getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(activity.getResources().getDisplayMetrics().heightPixels*0.90);
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
        TextView resultsView = findViewById(R.id.results);

        // Set date
        dateView.setText(result.date.toString());
        listNameView.setText(activity.getString(R.string.resultList, result.listName));

        // Set voters names
        StringBuilder voters = new StringBuilder(activity.getString(R.string.resultVoters));
        for (String v : result.voterNames) {
            voters.append(v).append(", ");
        }
        voters.delete(voters.capacity() - 2, voters.capacity());
        voterView.setText(voters);

        // Set results
        StringBuilder results = new StringBuilder();
        for (ResultItem r : result.resultItems) {
            results.append(formatResults(r)).append("\n");
        }
        resultsView.setText(results);
    }

    private String formatResults(ResultItem result) {
        String bonus = activity.getString(R.string.lists_bonus);
        String peril = activity.getString(R.string.lists_peril);
        String dropped = result.dropped ? activity.getString(R.string.dropped) : "";
        String reset = result.reset ? activity.getString(R.string.reset) : "";

        return String.format("%d. %s: %s = %d -> %d / %s = %d -> %d %s%s",
                result.position, result.name, bonus, result.oldBonus, result.newBonus, peril,
                result.oldPeril, result.newPeril, dropped, reset);
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
