package io.github.tonimheinonen.whattodonext.listsactivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.ListsActivity;
import io.github.tonimheinonen.whattodonext.R;

public class ListDialog extends Dialog implements
        android.view.View.OnClickListener {

    private ListsActivity activity;

    public ListDialog(ListsActivity a) {
        super(a);
        this.activity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.list_dialog);

        // Set listeners for confirm and cancel
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.addList).setOnClickListener(this);

        // Load saves lists and inflate them to linear layout
        ArrayList<String> listNames = new ArrayList<>();
        listNames.add("Lista 1");
        listNames.add("Lista 2");
        listNames.add("Lista 3");

        LinearLayout lists = findViewById(R.id.savedLists);
        for (String name : listNames) {
            View view = getLayoutInflater().inflate(R.layout.saved_list, null);
            TextView listName = view.findViewById(R.id.savedListName);
            listName.setText(name);
            lists.addView(view);
            view.setOnClickListener(this);
        }
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
            case R.id.savedListName:
                loadList(v);
                break;
            case R.id.savedListDelete:
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
        Button btn = (Button) v;
        activity.loadList(btn.getText().toString());
        dismiss();
    }

    private void deleteList(View v) {

    }
}
