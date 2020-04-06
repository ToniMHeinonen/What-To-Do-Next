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

/**
 * Handles modifying of ListOfViews and ListItems.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
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
    private TextView vName, vTotal, vBonus, vPeril, vSelectedList;
    private int originalTextColor;

    /**
     * Initializes necessary values.
     * @param savedInstanceState previous Activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        // Find topics so that color can be changed
        vName = findViewById(R.id.name);
        vTotal = findViewById(R.id.total);
        vBonus = findViewById(R.id.bonus);
        vPeril = findViewById(R.id.peril);
        vSelectedList = findViewById(R.id.selected_list);
        originalTextColor = vName.getCurrentTextColor();

        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE); // Show loading bar
        DatabaseHandler.getLists(this);
    }

    /**
     * Loads all lists and select's previously chosen list.
     * @param lists database lists
     */
    @Override
    public void onDataGetLists(ArrayList<ListOfItems> lists) {
        this.lists = lists;
        curListId = GlobalPrefs.loadCurrentList();
        findViewById(R.id.loadingPanel).setVisibility(View.GONE); // Hide loading bar

        // If there are at least 1 saved list
        if (!lists.isEmpty()) {
            // Loop through all available lists
            for (ListOfItems list : lists) {
                // If list has same id as saved previously used list, select that
                if (curListId.equals(list.getDbID())) {
                    setCurrentList(list);
                    findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE); // Show loading bar
                    DatabaseHandler.getItems(this, curList);
                    break;
                }
            }
        }
    }

    /**
     * Loads items for current list.
     * @param items database items in list
     */
    @Override
    public void onDataGetItems(ArrayList<ListItem> items) {
        curList.setItems(items);
        Buddy.filterListByFallen(curList.getItems(), false);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE); // Hide loading bar
        createListItems();
        updateListItems();
    }

    /**
     * Loads profiles from database.
     * @param profiles database profiles
     */
    @Override
    public void onDataGetProfiles(ArrayList<Profile> profiles) {}

    /**
     * Sets current list.
     * @param list list to set to
     */
    private void setCurrentList(ListOfItems list) {
        curList = list;

        // Update list text on top of the screen
        if (list != null)
            vSelectedList.setText(list.getName());
        else
            vSelectedList.setText(getString(R.string.no_list_selected));
    }

    /**
     * Creates ListView with correct adapter and data.
     */
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

    /**
     * Sorts list correctly and adjusts changes to list.
     */
    private void updateListItems() {
        if (list == null || curList == null)
            return;

        sortList();

        if (list.getAdapter() != null)
            adapter.notifyDataSetChanged();
        else
            createListItems();
    }

    /**
     * Hides list items when current list is deleted.
     */
    private void hideListItems() {
        list.setAdapter(null);
    }


    /*//////////////////// LIST DIALOG ////////////////////*/

    /**
     * Adds new list.
     * @param name name of the list
     */
    public void addList(String name) {
        ListOfItems list = new ListOfItems(name);
        setCurrentList(list);
        createListItems();

        lists.add(list);
        DatabaseHandler.addList(list);
    }

    /**
     * Loads selected list.
     * @param listIndex selected list
     */
    public void loadList(int listIndex) {
        // If selection is not the same as current list
        if (curList != lists.get(listIndex)) {
            setCurrentList(lists.get(listIndex));
            Buddy.filterListByFallen(curList.getItems(), false);

            GlobalPrefs.saveCurrentList(curList.getDbID());
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE); // Show loading bar
            DatabaseHandler.getItems(this, curList);
        }
    }

    /**
     * Deletes selected list.
     * @param listIndex selected list
     */
    public void deleteList(int listIndex) {
        ListOfItems list = lists.get(listIndex);
        lists.remove(list);
        DatabaseHandler.removeList(list);

        // If current list is the deleted one
        if (curList == list) {
            setCurrentList(null);
            GlobalPrefs.saveCurrentList("");
            hideListItems();
        }
    }

    /*//////////////////// ITEM DIALOG ////////////////////*/

    /**
     * Adds new item.
     * @param item item to add
     */
    public void addItem(ListItem item) {
        curList.getItems().add(item);
        DatabaseHandler.addItem(curList, item);

        updateListItems();
    }

    /**
     * Modifies selected item.
     * @param item selected item
     */
    public void modifyItem(ListItem item) {
        // If item is new item, add it
        if (!curList.getItems().contains(item)) {
            addItem(item);
            return;
        }

        DatabaseHandler.modifyItem(item);
        updateListItems();
    }

    /**
     * Deletes selected item.
     * @param item selected item
     */
    private void deleteItem(ListItem item) {
        curList.getItems().remove(item);
        DatabaseHandler.removeItem(item);
    }

    /*//////////////////// SORTING ////////////////////*/

    /**
     * Sorts list correctly when some of the topic texts are touched.
     * @param v topic value
     */
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

    /**
     * Changes currently sorted text color.
     * @param selected currently selected sort type
     */
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

    /**
     * Inverts sorting order.
     * @param sort selected sorting type
     */
    private void invertSortOrder(int sort) {
        // If new sort is same as current, reverse order
        if (sort == curSort) {
            ascending = !ascending;
        }
    }

    /**
     * Sorts list.
     */
    private void sortList() {
        changeColor(curSort);

        switch (curSort) {
            case NAME:
                Buddy.sortItemsByName(curList.getItems(), ascending);
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

    /**
     * Handles adding of new item.
     * @param v add button
     */
    public void addClicked(View v) {
        if (curList == null) {
            Toast.makeText(this, "First select list", Toast.LENGTH_LONG).show();
            return;
        }

        final ListItem item = new ListItem("", 0, 0);
        ListItemDialog dialog = new ListItemDialog(this, item);
        dialog.show();
    }

    /**
     * Handles choosing list.
     * @param v list button
     */
    public void listClicked(View v) {
        ListDialog dialog = new ListDialog(this, lists);
        dialog.show();
    }

    /**
     * Handles going back.
     * @param v back button
     */
    public void backClicked(View v) {
        super.onBackPressed();
    }
}
