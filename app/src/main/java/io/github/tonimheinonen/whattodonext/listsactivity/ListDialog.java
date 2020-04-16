package io.github.tonimheinonen.whattodonext.listsactivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.tools.Debug;
import io.github.tonimheinonen.whattodonext.ListsActivity;
import io.github.tonimheinonen.whattodonext.R;

/**
 * Handles list of items.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class ListDialog extends Dialog implements
        android.view.View.OnClickListener {

    private ListsActivity activity;
    private ArrayList<ListOfItems> lists;

    /**
     * Initializes necessary values.
     * @param a current activity
     * @param lists selected list
     */
    public ListDialog(ListsActivity a, ArrayList<ListOfItems> lists) {
        super(a);
        this.activity = a;
        this.lists = lists;
    }

    /**
     * Initializes views.
     * @param savedInstanceState previous Activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.list_dialog);

        // Set listeners for confirm and cancel
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.addList).setOnClickListener(this);

        // Add lists to ListView
        final ListView list = findViewById(R.id.savedLists);
        list.setAdapter(new ListAndProfileAdapter(activity, lists, this));
    }

    /**
     * Listends for view clicks.
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addList:
                addList();
                break;
            case R.id.back:
                cancel();
                break;
            case R.id.savedName:
                loadList(v);
                break;
            case R.id.savedDelete:
                deleteList(v);
                break;
            default:
                break;
        }
    }

    /**
     * Adds new list.
     */
    private void addList() {
        EditText view = findViewById(R.id.newList);
        String name = view.getText().toString();
        activity.addList(name);
        dismiss();
    }

    /**
     * Loads list.
     * @param v selected list
     */
    private void loadList(View v) {
        Debug.print("ListDialog", "loadList", "", 1);
        activity.loadList((int) v.getTag());
        dismiss();
    }

    /**
     * Deletes list.
     * @param v selected list
     */
    private void deleteList(View v) {
        Debug.print("ListDialog", "deleteList", "", 1);
        final int index = (int) v.getTag();
        String name = lists.get(index).getName();
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.alert_delete_topic, name))
                .setMessage(activity.getString(R.string.alert_delete_message))

                .setPositiveButton(activity.getString(R.string.alert_delete_confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.deleteList(index);
                        dismiss();
                    }
                })

                .setNegativeButton(activity.getString(R.string.alert_delete_cancel),  null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
