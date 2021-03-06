package io.github.tonimheinonen.whattodonext;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.listsactivity.ListDialog;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemDialog;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;
import io.github.tonimheinonen.whattodonext.tools.HTMLDialog;

/**
 * Handles modifying of ListOfViews and ListItems.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class ListsActivity extends AppCompatActivity {

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

        // If tutorial has not been confirmed yet, show it
        if (GlobalPrefs.loadPopupInfo(GlobalPrefs.TUTORIAL_CREATE_LIST))
            new HTMLDialog(this, HTMLDialog.HTMLText.TUTORIAL_CREATE_LIST).show();

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
        DatabaseHandler.getLists(this::listsLoaded);
    }

    /**
     * Shows loading bar and starts fetching items for list.
     */
    private void getItems() {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE); // Show loading bar
        DatabaseHandler.getItems(this::itemsLoaded, curList);
    }

    /**
     * Loads all lists and select's previously chosen list.
     * @param lists database lists
     */
    public void listsLoaded(ArrayList<ListOfItems> lists) {
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
    public void itemsLoaded(ArrayList<ListItem> items) {
        curList.setItems(items);
        Buddy.filterListByFallen(curList.getItems(), fallenList);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE); // Hide loading bar
        itemsFragment.updateListItems();
    }

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
     * @return created list
     */
    public ListOfItems addList(String name) {
        ListOfItems list = new ListOfItems(name);
        setCurrentList(list);
        itemsFragment.updateListItems();

        lists.add(list);
        DatabaseHandler.addList(list);

        // If tutorial has not been confirmed yet, show it
        if (GlobalPrefs.loadPopupInfo(GlobalPrefs.TUTORIAL_ADD_ITEM))
            new HTMLDialog(this, HTMLDialog.HTMLText.TUTORIAL_ADD_ITEM).show();

        return list;
    }

    /**
     * Loads selected list.
     * @param selectedList selected list
     */
    public void loadList(ListOfItems selectedList) {
        // If selection is not the same as current list
        if (curList != selectedList) {
            setCurrentList(selectedList);
            setFallenStatus(false);
            Buddy.filterListByFallen(curList.getItems(), fallenList);

            GlobalPrefs.saveCurrentList(curList.getDbID());
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE); // Show loading bar
            DatabaseHandler.getItems(this::itemsLoaded, curList);
        }
    }

    /**
     * Deletes selected list.
     * @param selectedList selected list
     */
    public void deleteList(ListOfItems selectedList) {
        lists.remove(selectedList);
        DatabaseHandler.removeList(selectedList);

        // If current list is the deleted one
        if (curList == selectedList) {
            setCurrentList(null);
            GlobalPrefs.saveCurrentList("");
        }
    }

    public void duplicateList(ListOfItems selectedList) {
        ListOfItems list = addList(selectedList.getName() + getString(R.string.copy_name));

        // Retrieve items which belong to the selected list
        // (selectedList.getItems() does not work, since it does not contain fallen and not fallen)
        DatabaseHandler.getItems((items) -> {
            // Add the items to the newly created list
            DatabaseHandler.addMultipleItems(curList, items);
            // Refresh list
            curList = null; // Set curList null, otherwise it skips the loadList() code
            loadList(list);
        }, selectedList);
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
            Buddy.showToast(getString(R.string.first_select_list), Toast.LENGTH_LONG);
            return;
        }

        final ListItem item = new ListItem("", 0, 0);
        item.setFallen(fallenList); // Set fallen status to current fallenList status
        ListItemDialog dialog = new ListItemDialog(this, item);
        dialog.show();

        // If tutorial has not been confirmed yet, show it
        if (GlobalPrefs.loadPopupInfo(GlobalPrefs.TUTORIAL_ITEM_INFO))
            new HTMLDialog(this, HTMLDialog.HTMLText.TUTORIAL_ITEM_INFO).show();
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
            Buddy.showToast(getString(R.string.first_select_list), Toast.LENGTH_LONG);
            return;
        }

        setFallenStatus(!fallenList);

        // Retrieve items filtered with correct fallen status
        getItems();
    }
}
