package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.OnGetDataListener;
import io.github.tonimheinonen.whattodonext.database.Profile;
import io.github.tonimheinonen.whattodonext.listsactivity.ListDialog;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemDialog;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
    private DatabaseValueListAdapter adapter;

    private final int NAME = 0, TOTAL = 1, BONUS = 2, PERIL = 3;
    private int curSort = NAME;
    private boolean ascending = true;
    private TextView vName, vTotal, vBonus, vPeril, vNoList;
    private EditText vSelectedList;

    private Button fallenButton;
    private boolean fallenList = false;

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
        vNoList = findViewById(R.id.no_list);
        vSelectedList = findViewById(R.id.selected_list);

        vSelectedList.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // If name was changed, save it
                    if (!vSelectedList.getText().toString().equals(curList.getName())) {
                        curList.setName(vSelectedList.getText().toString());
                        DatabaseHandler.modifyList(curList);
                        Buddy.showToast("New list name saved", Toast.LENGTH_SHORT);
                    }

                    Buddy.hideKeyboardAndClear(vSelectedList, false);
                    return true;
                }
                return false;
            }
        });

        // Find fallen button for controlling it's color
        fallenButton = findViewById(R.id.fallenButton);

        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE); // Show loading bar
        DatabaseHandler.getLists(this);
    }

    /**
     * Shows loading bar and starts fetching items for list.
     */
    private void getItems() {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE); // Show loading bar
        DatabaseHandler.getItems(this, curList);
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
                    getItems();
                    break;
                }
            }
        } else {
            // If lists are empty, show list dialog
            // This is mainly intended for new users for them to understand what to do
            new ListDialog(this, lists).show();
        }
    }

    /**
     * Loads items for current list.
     * @param items database items in list
     */
    @Override
    public void onDataGetItems(ArrayList<ListItem> items) {
        curList.setItems(items);
        Buddy.filterListByFallen(curList.getItems(), fallenList);

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
        setFallenStatus(false); // Show normal items when list changes

        // Update list text on top of the screen
        if (list != null) {
            vSelectedList.setVisibility(View.VISIBLE);
            vNoList.setVisibility(View.GONE);
            vSelectedList.setText(list.getName());
        } else {
            vSelectedList.setVisibility(View.GONE);
            vNoList.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Creates ListView with correct adapter and data.
     */
    public void createListItems() {
        list = findViewById(R.id.list);
        adapter = new DatabaseValueListAdapter(this, curList.getItems(),
                null, DatabaseValueListAdapter.AdapterType.LIST_ITEM);

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
            Buddy.filterListByFallen(curList.getItems(), fallenList);

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

        checkFallenStatus(item);
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
        checkFallenStatus(item);
        updateListItems();
    }

    /**
     * Deletes selected item.
     * @param item selected item
     */
    public void deleteItem(ListItem item) {
        // If item is new, it has not been yet added in list
        if (curList.getItems().contains(item)) {
            curList.getItems().remove(item);
            DatabaseHandler.removeItem(item);
            updateListItems();
        }
    }

    /**
     * Checks the fallen status of the item.
     *
     * If fallen status is not the same as fallenList status, drop item from list.
     * @param item item to check
     */
    private void checkFallenStatus(ListItem item) {
        if (item.isFallen() != fallenList)
            curList.getItems().remove(item);
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
                getResources().getColor(R.color.defaultTextColor));

        vTotal.setTextColor(selected == TOTAL ?
                getResources().getColor(R.color.textColorPrimary) :
                getResources().getColor(R.color.defaultTextColor));

        vBonus.setTextColor(selected == BONUS ?
                getResources().getColor(R.color.textColorPrimary) :
                getResources().getColor(R.color.defaultTextColor));

        vPeril.setTextColor(selected == PERIL ?
                getResources().getColor(R.color.textColorPrimary) :
                getResources().getColor(R.color.defaultTextColor));
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

    private void setFallenStatus(boolean status) {
        fallenList = status;

        fallenButton.setBackground(fallenList ?
                getResources().getDrawable(R.drawable.button_bg_clicked) :
                getResources().getDrawable(R.drawable.button_selector));
    }

    /*//////////////////// TOP BAR BUTTONS ////////////////////*/

    /**
     * Handles adding of new item.
     * @param v add button
     */
    public void addClicked(View v) {
        if (curList == null) {
            Toast.makeText(this, getString(R.string.first_select_list), Toast.LENGTH_LONG).show();
            return;
        }

        final ListItem item = new ListItem("", 0, 0);
        item.setFallen(fallenList); // Set fallen status to current fallenList status
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

    /**
     * Handles whether to show fallen items or not.
     * @param v fallen button
     */
    public void fallenClicked(View v) {
        if (curList == null) {
            Toast.makeText(this, getString(R.string.first_select_list), Toast.LENGTH_LONG).show();
            return;
        }

        setFallenStatus(!fallenList);

        // Retrieve items filtered with correct fallen status
        getItems();
    }
}
