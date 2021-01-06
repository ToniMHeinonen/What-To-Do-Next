package io.github.tonimheinonen.whattodonext.listsactivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.ListsActivity;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;

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
        list.setAdapter(new DatabaseValueListAdapter(activity, lists, this,
                DatabaseType.LIST_OF_ITEMS));
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
                modifyList(v);
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
    private void modifyList(View v) {
        ListOfItems list = lists.get((int) v.getTag());
        new ListModifyDialog(activity, list).show();
        dismiss();
    }
}
