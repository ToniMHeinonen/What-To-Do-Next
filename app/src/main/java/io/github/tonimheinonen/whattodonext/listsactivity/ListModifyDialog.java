package io.github.tonimheinonen.whattodonext.listsactivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import io.github.tonimheinonen.whattodonext.ListsActivity;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

/**
 * Handles list of items.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class ListModifyDialog extends Dialog implements
        View.OnClickListener {

    private ListsActivity listsActivity;
    private ListOfItems selectedList;

    /**
     * Initializes necessary values.
     * @param a current activity
     * @param selectedList selected list to modify
     */
    public ListModifyDialog(ListsActivity a, ListOfItems selectedList) {
        super(a);
        this.listsActivity = a;
        this.selectedList = selectedList;
    }

    /**
     * Initializes views.
     * @param savedInstanceState previous Activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.list_modify_dialog);

        // Change the title according to the name of the list
        TextView name = findViewById(R.id.listName);
        name.setText(selectedList.getName());

        // Set listeners for buttons
        findViewById(R.id.open).setOnClickListener(this);
        findViewById(R.id.duplicate).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
    }

    /**
     * Listens for view clicks.
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open:
                listsActivity.loadList(selectedList);
                dismiss();
                break;
            case R.id.duplicate:
                listsActivity.duplicateList(selectedList);
                dismiss();
                break;
            case R.id.delete:
                deleteList(v);
                break;
            case R.id.cancel:
                cancel();
                // Reopen list dialog
                listsActivity.listClicked(null);
                break;
            default:
                break;
        }
    }

    /**
     * Deletes list.
     * @param v selected list
     */
    private void deleteList(View v) {
        String name = selectedList.getName();

        Buddy.showAlert(listsActivity, listsActivity.getString(R.string.alert_delete_topic, name),
                listsActivity.getString(R.string.alert_delete_message),
                listsActivity.getString(R.string.alert_delete_confirm), null,
                () -> {
                    listsActivity.deleteList(selectedList);
                    dismiss();
                    // Reopen list dialog
                    listsActivity.listClicked(null);
                }, null);
    }
}
