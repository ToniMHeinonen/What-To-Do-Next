package io.github.tonimheinonen.whattodonext.listsactivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.ClickListenerDialog;
import io.github.tonimheinonen.whattodonext.Debug;
import io.github.tonimheinonen.whattodonext.ListAndProfileAdapter;
import io.github.tonimheinonen.whattodonext.ListsActivity;
import io.github.tonimheinonen.whattodonext.R;

public class ListDialog extends ClickListenerDialog implements
        android.view.View.OnClickListener {

    private ListsActivity activity;
    private ArrayList<ListOfItems> lists;

    public ListDialog(ListsActivity a, ArrayList<ListOfItems> lists) {
        super(a);
        this.activity = a;
        this.lists = lists;
    }

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

    private void addList() {
        EditText view = findViewById(R.id.newList);
        String name = view.getText().toString();
        activity.addList(name);
        dismiss();
    }

    private void loadList(View v) {
        Debug.print("ListDialog", "loadList", "", 1);
        activity.loadList((int) v.getTag());
        dismiss();
    }

    private void deleteList(View v) {
        Debug.print("ListDialog", "deleteList", "", 1);
        activity.deleteList((int) v.getTag());
        dismiss();
    }
}
