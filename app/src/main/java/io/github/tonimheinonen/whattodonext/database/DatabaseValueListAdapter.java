package io.github.tonimheinonen.whattodonext.database;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.SavedResult;

/**
 * Handles List View element with List Item.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class DatabaseValueListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<? extends DatabaseValue> listData;
    private LayoutInflater layoutInflater;
    private View.OnClickListener listener;

    private View view;
    private int position;

    private DatabaseType type;

    private static final HashMap<DatabaseType, Integer> layouts = new HashMap<>();

    // Set correct resources for types in HashMap
    static {
        layouts.put(DatabaseType.LIST_ITEM, R.layout.list_item);
        layouts.put(DatabaseType.LIST_OF_ITEMS, R.layout.saved_list_and_profile);
        layouts.put(DatabaseType.PROFILE, R.layout.saved_list_and_profile);
        layouts.put(DatabaseType.VOTE_HIDE_EXTRA, R.layout.vote_item);
        layouts.put(DatabaseType.VOTE_SHOW_EXTRA, R.layout.vote_item_show_extra);
        layouts.put(DatabaseType.VOTE_RESULTS, R.layout.result_hide_votes_item);
        layouts.put(DatabaseType.SAVED_RESULTS, R.layout.saved_result);
    }

    /**
     * Initializes adapter for list items.
     * @param aContext current context
     * @param listData list items
     */
    public DatabaseValueListAdapter(Context aContext, ArrayList<? extends DatabaseValue> listData,
                                    View.OnClickListener listener, DatabaseType type) {
        this.context = aContext;
        this.listData = listData;
        this.listener = listener;
        this.type = type;
        layoutInflater = LayoutInflater.from(aContext);
    }

    /**
     * Returns size of the data.
     * @return size of the data
     */
    @Override
    public int getCount() {
        return listData.size();
    }

    /**
     * Returns item at given position.
     * @param position index of the item
     * @return item
     */
    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    /**
     * Returns position of the item.
     * @param position index of the item
     * @return position of the item
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateItems(ArrayList<? extends DatabaseValue> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }

    /**
     * Creates view of the given data.
     * @param position index of the data
     * @param convertView part of the view reserved for current data
     * @param parent listview parent
     * @return view of the data
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        this.view = convertView;
        this.position = position;

        if (view == null) {
            view = layoutInflater.inflate(layouts.get(type), null);
        }

        // Test this later
        if (getItem(position) != null) {
            if (type.equals(DatabaseType.LIST_ITEM))
                listItem();
            else if (type.equals(DatabaseType.LIST_OF_ITEMS))
                listOfItems();
            else if (type.equals(DatabaseType.PROFILE))
                profile();
            else if (type.equals(DatabaseType.VOTE_HIDE_EXTRA))
                voteHideExtra();
            else if (type.equals(DatabaseType.VOTE_SHOW_EXTRA))
                voteShowExtra();
            else if (type.equals(DatabaseType.VOTE_RESULTS))
                voteResults();
            else if (type.equals(DatabaseType.SAVED_RESULTS))
                savedResults();
        }

        return this.view;
    }

    /**
     * Displays ListItem in a ListView in ListsActivity.
     *
     * Displays the name and total, bonus and peril points.
     */
    private void listItem() {
        ListItem item = (ListItem) getItem(position);

        // Retrieve views
        TextView name = view.findViewById(R.id.name);
        TextView total = view.findViewById(R.id.total);
        TextView bonus = view.findViewById(R.id.bonus);
        TextView peril = view.findViewById(R.id.peril);

        // Change texts for views
        name.setText(item.getName());
        total.setText(String.valueOf(item.getTotal()));
        bonus.setText(String.valueOf(item.getBonus()));
        peril.setText(String.valueOf(item.getPeril()));
    }

    /**
     * Displays ListOfItems in a ListView in ListsActivity.
     *
     * Displays the name and delete button.
     */
    private void listOfItems() {
        ListOfItems item = (ListOfItems) getItem(position);

        Button listName = view.findViewById(R.id.savedName);
        listName.setText(item.getName());
        listName.setOnClickListener(listener);
        listName.setTag(position);

        if (item.isSelected())
            listName.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        else
            listName.setTextColor(context.getResources().getColor(R.color.profileAndListOfItemsTextColor));

        Button listDelete = view.findViewById(R.id.savedDelete);
        listDelete.setOnClickListener(listener);
        listDelete.setTag(position);
    }

    /**
     * Displays Profile in a ListView in StartVoteActivity.
     *
     * Displays the name and delete button.
     */
    private void profile() {
        Profile item = (Profile) getItem(position);

        Button listName = view.findViewById(R.id.savedName);
        listName.setText(item.getName());
        listName.setOnClickListener(listener);
        listName.setTag(position);

        listName.setTextColor(context.getResources().getColor(item.isSelected() ?
                R.color.colorPrimary : R.color.profileAndListOfItemsTextColor));

        Button listDelete = view.findViewById(R.id.savedDelete);
        listDelete.setOnClickListener(listener);
        listDelete.setTag(position);
    }

    /**
     * Displays ListItem in a ListView in VoteTopActivity.
     *
     * Displays only the name and the vote points.
     */
    private void voteHideExtra() {
        ListItem item = (ListItem) getItem(position);

        TextView itemName = view.findViewById(R.id.voteName);
        itemName.setText(item.getName());

        TextView itemVoteAmount = view.findViewById(R.id.voteAmount);
        int points = item.getVotePoints();
        itemVoteAmount.setText(points == 0 ? "" : String.valueOf(points));
    }

    /**
     * Displays ListItem in a ListView in VoteTopActivity.
     *
     * Displays the name and vote points, bonus and extra points.
     */
    private void voteShowExtra() {
        ListItem item = (ListItem) getItem(position);

        // Retrieve views
        TextView votePoints = view.findViewById(R.id.voteAmount);
        TextView name = view.findViewById(R.id.voteName);
        TextView total = view.findViewById(R.id.voteTotal);
        TextView bonus = view.findViewById(R.id.voteBonus);
        TextView peril = view.findViewById(R.id.votePeril);

        // Adjust view texts
        int points = item.getVotePoints();
        votePoints.setText(points == 0 ? "" : String.valueOf(points));
        name.setText(item.getName());
        total.setText(String.valueOf(item.getTotal()));
        bonus.setText(String.valueOf(item.getBonus()));
        peril.setText(String.valueOf(item.getPeril()));

        // If total points are halved, change text color
        if (item.getTotal() != (item.getBonus() + item.getPeril()))
            total.setTextColor(context.getResources().getColor(R.color.colorAccent));
    }

    /**
     * Displays ListItem in a ListView in VoteResultsActivity.
     *
     * Displays the name and total points.
     */
    private void voteResults() {
        ListItem item = (ListItem) getItem(position);

        TextView itemName = view.findViewById(R.id.resultsName);
        itemName.setText(item.getName());
        itemName.setTextColor(context.getResources().getColor(item.isSelected() ?
                R.color.colorPrimary : R.color.defaultTextColor));

        TextView itemBonus = view.findViewById(R.id.resultsBonus);
        itemBonus.setText(String.valueOf(item.getTotal()));

        TextView itemPoints = view.findViewById(R.id.resultsVotePoints);
        itemPoints.setText(String.valueOf(item.getVotePoints()));
    }

    /**
     * Displays saved results date in a button.
     */
    private void savedResults() {
        SavedResult item = (SavedResult) getItem(position);

        Button itemName = view.findViewById(R.id.savedResult);
        itemName.setText(item.date.toString());

        itemName.setOnClickListener(listener);
        itemName.setTag(position);
    }
}
