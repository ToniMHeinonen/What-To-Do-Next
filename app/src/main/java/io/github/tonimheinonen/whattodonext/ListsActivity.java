package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.OnGetDataListener;
import io.github.tonimheinonen.whattodonext.database.Profile;
import io.github.tonimheinonen.whattodonext.listsactivity.ListDialog;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemDialog;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Handles modifying of ListOfViews and ListItems.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class ListsActivity extends AppCompatActivity implements OnGetDataListener {

    private ListOfItems curList;
    private String curListId = "";
    private ListItemFragment itemsFragment;

    private ArrayList<ListOfItems> lists = new ArrayList<>();

    private TextView vNoList;
    private EditText vSelectedList;

    // Fallen
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

        // Setup list fragment
        itemsFragment = Buddy.createListItemFragment(this,
                DatabaseType.LIST_ITEM, curList);

        // Retrieve name of the list views
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
                        Buddy.showToast(getString(R.string.list_rename_saved), Toast.LENGTH_SHORT);
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
        itemsFragment.updateListItems();
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
        itemsFragment.setCurrentList(curList);

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

    /*//////////////////// LIST DIALOG ////////////////////*/

    /**
     * Adds new list.
     * @param name name of the list
     */
    public void addList(String name) {
        ListOfItems list = new ListOfItems(name);
        setCurrentList(list);
        itemsFragment.updateListItems();

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
            setFallenStatus(false);
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
        itemsFragment.updateListItems();
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
        itemsFragment.updateListItems();
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
            itemsFragment.updateListItems();
        }
    }

    /**
     * Checks the fallen status of the item.
     *
     * If fallen status is not the same as fallenList status, drop item from list.
     * @param item item to check
     */
    public void checkFallenStatus(ListItem item) {
        if (item.isFallen() != fallenList)
            curList.getItems().remove(item);
    }

    /**
     * Changes fallen status and updates it's button's color.
     * @param status whether to load fallen or not
     */
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
