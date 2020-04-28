package io.github.tonimheinonen.whattodonext;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import androidx.fragment.app.Fragment;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.listsactivity.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemDialog;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.voteactivity.VoteTopActivity;

public class ListViewFragment extends Fragment implements View.OnClickListener {

    private DatabaseType type;
    private ListOfItems curList;

    private ListView listView;
    private DatabaseValueListAdapter adapter;

    private TextView vName, vTotal, vBonus, vPeril, vVoteAmount;

    // Sorting
    private final int NAME = 0, TOTAL = 1, BONUS = 2, PERIL = 3, VOTE_POINTS = 4;
    private int curSort = NAME;
    private boolean ascending = true;

    /**
     * Default constructor for handling device orientation changes.
     */
    public ListViewFragment() {}

    /**
     * Creates the Fragment view.
     * @param inflater used for inflating the view
     * @param container holds view data
     * @param savedInstanceState previous instance state
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;

        type = DatabaseType.valueOf(getArguments().getString("type"));
        curList = getArguments().getParcelable("curList");

        switch (type) {
            case LIST_ITEM:
                view = inflater.inflate(R.layout.fragment_list_item, container, false);
                break;
            case VOTE_SHOW_EXTRA:
                view = inflater.inflate(R.layout.fragment_vote_show_extra, container, false);
                break;
            case VOTE_HIDE_EXTRA:
                view = inflater.inflate(R.layout.fragment_vote_hide_extra, container, false);
                break;
            default:
                // Prevents "view not initialized" error
                view = inflater.inflate(null, null, false);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Find topics so that color and sorting type can be changed
        vName = view.findViewById(R.id.name);
        vTotal = view.findViewById(R.id.total);
        vBonus = view.findViewById(R.id.bonus);
        vPeril = view.findViewById(R.id.peril);
        vVoteAmount = view.findViewById(R.id.voteAmount);

        if (vName != null) vName.setOnClickListener(this);
        if (vTotal != null) vTotal.setOnClickListener(this);
        if (vBonus != null) vBonus.setOnClickListener(this);
        if (vPeril != null) vPeril.setOnClickListener(this);
        if (vVoteAmount != null) vVoteAmount.setOnClickListener(this);

        createListItems();
        updateListItems();
    }

    /**
     * Creates ListView with correct adapter and data.
     */
    private void createListItems() {
        listView = getView().findViewById(R.id.list);
        // If current list is null, add empty arraylist
        ArrayList<ListItem> items = curList != null ?
                curList.getItems() :
                new ArrayList<>();

        adapter = new DatabaseValueListAdapter(getActivity(), items,
                null, type);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (type) {
                    case LIST_ITEM:
                        ListItem listItem = (ListItem) listView.getItemAtPosition(position);
                        ListItemDialog dialog = new ListItemDialog((ListsActivity) getActivity(), listItem);
                        dialog.show();
                        break;
                    case VOTE_SHOW_EXTRA:
                    case VOTE_HIDE_EXTRA:
                        ListItem voteShowItem = (ListItem) listView.getItemAtPosition(position);
                        VoteTopActivity activity = (VoteTopActivity) getActivity();
                        activity.itemClicked(voteShowItem, position);
                        break;
                }
            }
        });
    }

    /**
     * Sorts list correctly and adjusts changes to list.
     */
    public void updateListItems() {
        // If getView() is null, the onCreateView is not completed yet
        // If listView is null, createListItems has not been called yet
        // If curList is null, Activity has not given the list yet
        if (getView() == null || listView == null || curList == null)
            return;

        sortList();
        adapter.updateItems(curList.getItems());
    }

    /**
     * Sets current list.
     * @param list list to set to
     */
    public void setCurrentList(ListOfItems list) {
        curList = list;

        // Hide list if curList is null
        if (curList == null)
            adapter.updateItems(new ArrayList<>());
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
            case VOTE_POINTS:
                Collections.sort(curList.getItems(), new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                ((Integer)o1.getVotePoints()).compareTo(o2.getVotePoints()) :
                                ((Integer)o2.getVotePoints()).compareTo(o1.getVotePoints());
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
        if (vName != null)
            vName.setTextColor(selected == NAME ?
                    getResources().getColor(R.color.textColorPrimary) :
                    getResources().getColor(R.color.defaultTextColor));

        if (vTotal != null)
            vTotal.setTextColor(selected == TOTAL ?
                    getResources().getColor(R.color.textColorPrimary) :
                    getResources().getColor(R.color.defaultTextColor));

        if (vBonus != null)
            vBonus.setTextColor(selected == BONUS ?
                    getResources().getColor(R.color.textColorPrimary) :
                    getResources().getColor(R.color.defaultTextColor));

        if (vPeril != null)
            vPeril.setTextColor(selected == PERIL ?
                    getResources().getColor(R.color.textColorPrimary) :
                    getResources().getColor(R.color.defaultTextColor));

        if (vVoteAmount != null)
            vVoteAmount.setTextColor(selected == VOTE_POINTS ?
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
        if (curList == null)
            return;

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
            case R.id.voteAmount:
                sort = VOTE_POINTS;
                break;
        }

        invertSortOrder(sort);
        curSort = sort;
        updateListItems();
    }
}
