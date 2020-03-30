package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.listsactivity.ListDialog;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemAdapter;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemDialog;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

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
    private String curListId = "";

    private ArrayList<ListOfItems> lists = new ArrayList<>();
    private ListView list;
    private ListItemAdapter adapter;

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

        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE); // Show loading bar
        DatabaseHandler.getLists(this);
    }

    @Override
    public void onDataGetLists(ArrayList<ListOfItems> lists) {
        this.lists = lists;
        curListId = GlobalPrefs.loadCurrentList();

        if (!lists.isEmpty()) {
            for (ListOfItems list : lists) {
                if (curListId.equals(list.getDbID())) {
                    curList = list;
                    DatabaseHandler.getItems(this, curList);
                    break;
                }
            }
        }
    }

    @Override
    public void onDataGetItems(ArrayList<ListItem> items) {
        curList.setItems(items);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE); // Hide loading bar
        createListItems();
        updateListItems();
    }

    @Override
    public void onDataGetProfiles(ArrayList<Profile> profiles) {}

    public void createListItems() {
        list = findViewById(R.id.list);
        adapter = new ListItemAdapter(this, curList.getItems());

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item = (ListItem) list.getItemAtPosition(position);
                ListItemDialog dialog = new ListItemDialog(_this, item);
                dialog.show();
            }
        });
    }

    private void updateListItems() {
        if (list == null)
            return;

        sortList();

        if (list.getAdapter() != null)
            adapter.notifyDataSetChanged();
        else
            createListItems();
    }

    private void hideListItems() {
        list.setAdapter(null);
    }


    /*//////////////////// LIST DIALOG ////////////////////*/

    public void addList(String name) {
        ListOfItems list = new ListOfItems(name);
        updateListItems();

        lists.add(list);
        DatabaseHandler.addList(curList);
    }

    public void loadList(int listIndex) {
        if (curList != lists.get(listIndex)) {
            curList = lists.get(listIndex);
            GlobalPrefs.saveCurrentList(curList.getDbID());
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE); // Show loading bar
            DatabaseHandler.getItems(this, curList);
        }
    }

    public void deleteList(int listIndex) {
        ListOfItems list = lists.get(listIndex);
        lists.remove(list);
        DatabaseHandler.removeList(list);

        if (curList == list) {
            curList = null;
            GlobalPrefs.saveCurrentList("");
            hideListItems();
        }
    }

    /*//////////////////// ITEM DIALOG ////////////////////*/

    public void addItem(ListItem item) {
        curList.getItems().add(item);
        DatabaseHandler.addItem(curList, item);

        updateListItems();
    }

    public void modifyItem(ListItem item) {
        if (!curList.getItems().contains(item)) {
            addItem(item);
            return;
        }

        DatabaseHandler.modifyItem(item);
        updateListItems();
    }

    private void deleteItem(ListItem item) {
        curList.getItems().remove(item);
        DatabaseHandler.removeItem(item);
    }

    /*//////////////////// SORTING ////////////////////*/

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

        invertSortOrder(sort);
        curSort = sort;
        updateListItems();
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

    private void invertSortOrder(int sort) {
        // If new sort is same as current, reverse order
        if (sort == curSort) {
            ascending = !ascending;
        }
    }

    private void sortList() {
        changeColor(curSort);

        switch (curSort) {
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
    }

    /*//////////////////// TOP BAR BUTTONS ////////////////////*/

    public void addClicked(View v) {
        if (curList == null) {
            Toast.makeText(this, "First select list", Toast.LENGTH_LONG).show();
            return;
        }

        final ListItem item = new ListItem("", 0, 0);
        ListItemDialog dialog = new ListItemDialog(this, item);
        dialog.show();
    }

    public void listClicked(View v) {
        ListDialog dialog = new ListDialog(this, lists);
        dialog.show();
    }

    public void backClicked(View v) {
        super.onBackPressed();
    }
}
