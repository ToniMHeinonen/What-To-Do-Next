package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.listsactivity.ListDialog;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemAdapter;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemDialog;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ListsActivity extends AppCompatActivity implements OnGetDataListener {

    private ListsActivity _this = this;

    private ListOfItems curList;
    private int curListIndex = 0;

    private ArrayList<ListOfItems> lists = new ArrayList<>();

    private final int NAME = 0, TOTAL = 1, BONUS = 2, PERIL = 3;
    private int curSort = NAME;
    private boolean ascending = true;
    private TextView vName, vTotal, vBonus, vPeril;
    private int originalTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        // Find topics so that color can be changed
        vName = findViewById(R.id.name);
        vTotal = findViewById(R.id.total);
        vBonus = findViewById(R.id.bonus);
        vPeril = findViewById(R.id.peril);
        originalTextColor = vName.getCurrentTextColor();

        DatabaseHandler.getLists(this);
    }

    @Override
    public void onDataGetLists(ArrayList<ListOfItems> lists) {
        this.lists = lists;

        if (lists.size() >= curListIndex + 1) {
            curList = lists.get(curListIndex);
            DatabaseHandler.getItems(this, curList);
        }
    }

    @Override
    public void onDataGetItems(ArrayList<ListItem> items) {
        curList.setItems(items);

        sortList(curSort, false);

        showListItems();
    }

    public void showListItems() {
        final ListView list = findViewById(R.id.list);

        list.setAdapter(new ListItemAdapter(this, curList.getItems()));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item = (ListItem) list.getItemAtPosition(position);
                ListItemDialog dialog = new ListItemDialog(_this, item);
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        showListItems();
                    }
                });
            }
        });
    }

    public void itemDialogConfirmed(ListItem item) {
        DatabaseHandler.modifyItem(item);
        sortList(curSort, false);
        showListItems();
    }

    public void addList(String name) {
        ListOfItems list = new ListOfItems(name);
        curList = list;
        showListItems();

        DatabaseHandler.addList(list);
    }

    public void loadList(String name) {

    }

    public void addClicked(View v) {
        if (curList == null) {
            Toast.makeText(this, "First create new list", Toast.LENGTH_LONG);
            return;
        }

        final ListItem item = new ListItem("", 0, 0);
        curList.getItems().add(item);
        DatabaseHandler.addItem(curList, item);
        ListItemDialog dialog = new ListItemDialog(this, item);
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                deleteItem(item);
            }
        });
    }

    private void deleteItem(ListItem item) {
        curList.getItems().remove(item);
        DatabaseHandler.removeItem(item);
    }

    public void listClicked(View v) {
        ListDialog dialog = new ListDialog(this);
        dialog.show();
    }

    public void topicClicked(View v) {
        int sort = -1;

        switch (v.getId()) {
            case R.id.name:
                sort = NAME;
                break;
            case R.id.total:
                sort = TOTAL;
                break;
            case R.id.bonus:
                sort = BONUS;
                break;
            case R.id.peril:
                sort = PERIL;
                break;
        }

        sortList(sort, true);
    }

    private void changeColor(int selected) {
        vName.setTextColor(selected == NAME ?
                getResources().getColor(R.color.textColorPrimary) :
                originalTextColor);

        vTotal.setTextColor(selected == TOTAL ?
                getResources().getColor(R.color.textColorPrimary) :
                originalTextColor);

        vBonus.setTextColor(selected == BONUS ?
                getResources().getColor(R.color.textColorPrimary) :
                originalTextColor);

        vPeril.setTextColor(selected == PERIL ?
                getResources().getColor(R.color.textColorPrimary) :
                originalTextColor);
    }

    private void sortList(final int sort, boolean topicClicked) {
        changeColor(sort);

        // If new sort is same as current, reverse order
        if (sort == curSort && topicClicked) {
            ascending = !ascending;
        }

        switch (sort) {
            case NAME:
                Collections.sort(curList.getItems(), new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                o1.getName().compareTo(o2.getName()) :
                                o2.getName().compareTo(o1.getName());
                    }
                });
                break;
            case TOTAL:
                Collections.sort(curList.getItems(), new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                ((Integer)o1.getTotal()).compareTo(o2.getTotal()) :
                                ((Integer)o2.getTotal()).compareTo(o1.getTotal());
                    }
                });
                break;
            case BONUS:
                Collections.sort(curList.getItems(), new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                ((Integer)o1.getBonus()).compareTo(o2.getBonus()) :
                                ((Integer)o2.getBonus()).compareTo(o1.getBonus());
                    }
                });
                break;
            case PERIL:
                Collections.sort(curList.getItems(), new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                ((Integer)o1.getPeril()).compareTo(o2.getPeril()) :
                                ((Integer)o2.getPeril()).compareTo(o1.getPeril());
                    }
                });
                break;
        }

        curSort = sort;
        showListItems();
    }
}
