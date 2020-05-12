package io.github.tonimheinonen.whattodonext.voteactivity;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.ResultItem;
import io.github.tonimheinonen.whattodonext.ResultsShowVotesAdapter;
import io.github.tonimheinonen.whattodonext.SavedResult;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.tools.Debug;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;
import io.github.tonimheinonen.whattodonext.MainActivity;
import io.github.tonimheinonen.whattodonext.database.OnGetDataListener;
import io.github.tonimheinonen.whattodonext.database.Profile;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Handles showing results of voting.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class VoteResultsActivity extends AppCompatActivity implements OnGetDataListener {

    private boolean lastResults;
    private int topAmount;
    private ListOfItems selectedList;
    private ArrayList<Profile> selectedProfiles;
    private ArrayList<ListItem> itemsToReset = new ArrayList<>();

    /**
     * Initializes VoteResultsActivity.
     * @param savedInstanceState previous activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_results);

        Intent intent = getIntent();
        topAmount = intent.getIntExtra("topAmount", -1);
        selectedList = intent.getExtras().getParcelable("selectedList");
        selectedProfiles = intent.getParcelableArrayListExtra("selectedProfiles");

        // If current topAmount is the same as last vote size
        lastResults = topAmount == GlobalPrefs.loadListVoteSizeSecond();

        if (lastResults) {
            Button next = findViewById(R.id.nextButton);
            next.setText(getString(R.string.save_and_exit));
            findViewById(R.id.resultsInfoText).setVisibility(View.VISIBLE);
        }

        // If activity has not been recreated (by screen rotation)
        if (savedInstanceState == null) {
            calculateVotePoints();
        } else {
            // Load items to reset before orientation change
            itemsToReset = savedInstanceState.getParcelableArrayList("itemsToReset");
        }

        setupItemList();
    }

    /**
     * Save selected items to reset.
     *
     * This gets called when screen rotation changes.
     * @param outState bundle to be saved
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("itemsToReset", itemsToReset);
    }

    /**
     * Calculates vote points.
     */
    private void calculateVotePoints() {
        ArrayList<ListItem> items = selectedList.getItems();

        // Add points from profile votepoints
        for (int i = 0; i < topAmount; i++) {
            int votePoints = i + 1; // i + 1 because index starts at 0, points at 1

            for (Profile p : selectedProfiles) {
                ListItem votedItem = p.getVoteItem(i);

                // Loop through all the items to give votePoints to correct item
                for (ListItem item : items) {
                    if (item.equalsTo(votedItem)) {
                        item.addVotePoints(votePoints);
                        break;
                    }
                }
            }
        }

        // Add extra points to total vote points
        for (ListItem item : items) {
            // If first vote, user wants to ignore unselected and item was not voted at all
            if (!lastResults && GlobalPrefs.loadIgnoreUnselected() && item.getVotePoints() == 0)
                continue;

            item.addVotePoints(item.getTotal());
            Debug.print(this, "item", "name: " + item.getName(), 1);
            Debug.print(this, "item", "points: " + item.getVotePoints(), 1);
        }

        // Sort items by vote points
        Collections.sort(items, new Comparator<ListItem>() {
            public int compare(ListItem o1, ListItem o2) {
                return ((Integer)o2.getVotePoints()).compareTo(o1.getVotePoints());
            }
        });

        // If these are not the last results, leave only items in top amount and items
        // which share the same amount of vote points as the last item in top amount
        // (example: top 7 list, leave all items which have same points as item number 7)
        if (!lastResults) {
            ArrayList<ListItem> selectedItems = new ArrayList<>();

            // Add top amount items to selected items
            for (int i = 0; i < topAmount; i++) {
                selectedItems.add(items.get(i));
            }

            // Add items which share same points amount as last item
            int lastItemPoints = items.get(topAmount - 1).getVotePoints();
            int currentIndex = topAmount;

            while (true) {
                // Prevent index out of bounds if there is no more items
                if (currentIndex < items.size()) {
                    ListItem item = items.get(currentIndex);

                    if (item.getVotePoints() == lastItemPoints) {
                        selectedItems.add(item);
                        currentIndex++;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }

            selectedList.setItems(selectedItems);
        }
    }

    /**
     * Shows results in list view.
     */
    private void setupItemList() {
        // Load whether to show vote items or not
        LinearLayout viewHolder = findViewById(R.id.listHolder);
        BaseAdapter adapter;
        View child;
        if (GlobalPrefs.loadShowVoted()) {
            // Inflate list which shows vote points
            child = getLayoutInflater().inflate(R.layout.result_show_votes_list, null);
            viewHolder.addView(child);

            // Add profile names to topics
            LinearLayout topicLayout = child.findViewById(R.id.topicLayout);
            for (Profile p : selectedProfiles) {
                TextView name = (TextView) getLayoutInflater().inflate(R.layout.result_voter_name, topicLayout, false);
                name.setText(p.getName());
                topicLayout.addView(name);
            }

            adapter = new ResultsShowVotesAdapter(this, selectedList.getItems(),
                    selectedProfiles);
        } else {
            // Inflate list with no vote points
            child = getLayoutInflater().inflate(R.layout.result_hide_votes_list, null);
            viewHolder.addView(child);

            adapter = new DatabaseValueListAdapter(this, selectedList.getItems(),
                    null, DatabaseType.VOTE_RESULTS);
        }

        final ListView list = child.findViewById(R.id.resultItems);
        list.setAdapter(adapter);

        // Listen for clicks
        if (lastResults) {
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ListItem item = (ListItem) list.getItemAtPosition(position);

                    // Reset selected items
                    if (itemsToReset.contains(item)) {
                        itemsToReset.remove(item);
                        item.setSelected(false);
                    } else {
                        itemsToReset.add(item);
                        item.setSelected(true);
                    }

                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * Check whether to start new vote or to end voting.
     * @param v next button view
     */
    public void nextPressed(View v) {
        if (lastResults) {
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            DatabaseHandler.getItems(this, selectedList);
        } else {
            // Proceed to next voting
            Intent intent = new Intent(this, VoteTopActivity.class);
            intent.putExtra("topAmount", GlobalPrefs.loadListVoteSizeSecond());
            intent.putExtra("selectedList", selectedList);
            intent.putParcelableArrayListExtra("selectedProfiles", selectedProfiles);
            startActivity(intent);
        }
    }

    /**
     * Shows exit confirmation prompt.
     * @param v exit button view
     */
    public void exitPressed(View v) {
        onBackPressed();
    }

    /**
     * Override what happens when pressing back during voting.
     */
    @Override
    public void onBackPressed() {
        Buddy.exitVoting(this);
    }

    /**
     * Calculates bonus and peril points for items.
     * @param items loaded lists from database
     */
    @Override
    public void onDataGetItems(ArrayList<ListItem> items) {
        Buddy.filterListByFallen(items, false); // Ignore fallen items
        ArrayList<ListItem> itemsLeft = selectedList.getItems();

        // Saving results
        ArrayList<ResultItem> resultsSaving = new ArrayList<>();
        int votePosition = 1;

        // Loop through all items and calculate bonus and peril
        for (ListItem item : itemsLeft) {
            boolean resetItem = false;

            // Check if user selected this item to be done
            for (ListItem itemToReset : itemsToReset) {
                if (item.equalsTo(itemToReset)) {
                    resetItem = true;
                    itemsToReset.remove(itemToReset);
                    break;
                }
            }

            // Remove all items left from the database items
            for (ListItem databaseItem : items) {
                if (databaseItem.equalsTo(item)) {
                    items.remove(databaseItem);
                    break;
                }
            }

            Debug.print(this, "onDataGetItems", item.toString(), 1);

            // If user selected item, reset it, else add bonus to it
            if (resetItem) {
                resultsSaving.add(new ResultItem(votePosition, item, ResultItem.RESET));
                item.setBonus(0);
                item.setPeril(0);
            } else {
                resultsSaving.add(new ResultItem(votePosition, item, ResultItem.BONUS));
                item.setBonus(item.getBonus() + 1);
            }

            DatabaseHandler.modifyItem(item);
            votePosition++;
        }

        // Loop through all of the rest database items and add peril point to them,
        // since they did not make it in to the last vote
        for (ListItem item: items) {
            resultsSaving.add(new ResultItem(-1, item, ResultItem.PERIL));
            item.setPeril(item.getPeril() + 1);

            // If peril points are over maximum peril points, drop item from list
            if (item.getPeril() > GlobalPrefs.loadMaxPerilPoints()) {
                item.setPeril(0);
                item.setFallen(true);
            }

            DatabaseHandler.modifyItem(item);
        }

        GlobalPrefs.saveNewResult(new SavedResult(resultsSaving));

        startActivity(new Intent(this, MainActivity.class));
        Buddy.showToast(getString(R.string.save_successful), Toast.LENGTH_LONG);
        finish();
    }

    /*private String formatResults(int position, ListItem item, int state) {
        int newBonus = 0, newPeril = 0;
        String bonus = getString(R.string.lists_bonus);
        String peril = getString(R.string.lists_peril);
        String dropped = "";

        return String.format("%d. %s: %s = %d -> %d / %s = %d -> %d %s",
                position, item.getName(), bonus, item.getBonus(), newBonus, peril, item.getPeril(),
                newPeril, dropped);
    }*/

    /**
     * Gets lists data from database.
     * @param lists loaded lists from database
     */
    @Override
    public void onDataGetLists(ArrayList<ListOfItems> lists) {}

    /**
     * Gets profile data from database.
     * @param profiles loaded lists from database
     */
    @Override
    public void onDataGetProfiles(ArrayList<Profile> profiles) {}
}
