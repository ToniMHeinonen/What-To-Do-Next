package io.github.tonimheinonen.whattodonext;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;

import androidx.fragment.app.Fragment;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.listsactivity.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemDialog;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

public class ListViewFragment extends Fragment implements View.OnClickListener {

    private DatabaseType type;
    private ListOfItems curList;

    private ListView listView;
    private DatabaseValueListAdapter adapter;

    private TextView vName, vTotal, vBonus, vPeril;

    // Sorting
    private final int NAME = 0, TOTAL = 1, BONUS = 2, PERIL = 3;
    private int curSort = NAME;
    private boolean ascending = true;

    // Fallen
    private boolean fallenList = false;

    /**
     * Default constructor for handling device orientation changes.
     */
    public ListViewFragment() {}

    /**
     * Initializes ListViewFragment with necessary values.
     * @param type
     * @param curList
     */
    public ListViewFragment(DatabaseType type, ListOfItems curList) {
        this.type = type;
        this.curList = curList;
    }

    /**
     * Creates the Fragment view.
     * @param inflater used for inflating the view
     * @param container holds view data
     * @param savedInstanceState previous instance state
     * @return
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_item_fragment, // The xml file
                container,
                false);

        // Find topics so that color can be changed
        vName = view.findViewById(R.id.name);
        vTotal = view.findViewById(R.id.total);
        vBonus = view.findViewById(R.id.bonus);
        vPeril = view.findViewById(R.id.peril);

        vName.setOnClickListener(this);
        vTotal.setOnClickListener(this);
        vBonus.setOnClickListener(this);
        vPeril.setOnClickListener(this);

        return view;
    }

    /**
     * Creates ListView with correct adapter and data.
     */
    public void createListItems() {
        listView = getView().findViewById(R.id.list);
        adapter = new DatabaseValueListAdapter(getActivity(), curList.getItems(),
                null, DatabaseType.LIST_ITEM);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (type) {
                    case LIST_ITEM:
                        ListItem item = (ListItem) listView.getItemAtPosition(position);
                        ListItemDialog dialog = new ListItemDialog((ListsActivity) getActivity(), item);
                        dialog.show();
                        break;

                }
            }
        });
    }

    /**
     * Sorts list correctly and adjusts changes to list.
     */
    public void updateListItems() {
        if (listView == null || curList == null)
            return;

        sortList();

        if (listView.getAdapter() != null)
            adapter.notifyDataSetChanged();
        else
            createListItems();
    }

    /**
     * Sets current list.
     * @param list list to set to
     */
    public void setCurrentList(ListOfItems list) {
        curList = list;
        setFallenStatus(false); // Show normal items when list changes

        // Hide list if curList is null
        if (curList == null)
            listView.setAdapter(null);
    }

    /*//////////////////// SORTING ////////////////////*/

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
     * Sorts list correctly when some of the topic texts are touched.
     * @param v topic value
     */
    @Override
    public void onClick(View v) {
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

    /*//////////////////// FALLEN ////////////////////*/

    /**
     * Sets fallen status.
     *
     * Determines if to load fallen or unfallen items.
     * @param status true if to load fallen items
     */
    public void setFallenStatus(boolean status) {
        fallenList = status;
    }

    /**
     * Returns fallen status.
     * @return
     */
    public boolean getFallenStatus() {
        return fallenList;
    }
}
