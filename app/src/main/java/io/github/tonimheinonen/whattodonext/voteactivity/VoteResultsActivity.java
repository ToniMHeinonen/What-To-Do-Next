package io.github.tonimheinonen.whattodonext.voteactivity;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.Buddy;
import io.github.tonimheinonen.whattodonext.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.Debug;
import io.github.tonimheinonen.whattodonext.GlobalPrefs;
import io.github.tonimheinonen.whattodonext.MainActivity;
import io.github.tonimheinonen.whattodonext.OnGetDataListener;
import io.github.tonimheinonen.whattodonext.Profile;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class VoteResultsActivity extends AppCompatActivity implements OnGetDataListener {

    private boolean lastResults;
    private int topAmount;
    private ListOfItems selectedList;
    private ArrayList<Profile> selectedProfiles;

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
        }

        calculateVotePoints();
        setupItemList();
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
                int index = p.getVoteItem(i);
                items.get(index).addVotePoints(votePoints);
            }
        }

        // Add bonus points to total vote points
        for (ListItem item : items) {
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
        final ListView list = findViewById(R.id.resultItems);
        VoteResultsAdapter adapter = new VoteResultsAdapter(this, selectedList.getItems());
        list.setAdapter(adapter);
    }

    /**
     * Check whether to start new vote or to end voting.
     * @param v
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
        Buddy.exitVoting(this);
    }

    /**
     * Calculates bonus and peril points for items.
     * @param items loaded lists from database
     */
    @Override
    public void onDataGetItems(ArrayList<ListItem> items) {
        ArrayList<ListItem> itemsLeft = selectedList.getItems();

        // Loop through all items and calculate bonus and peril
        for (ListItem item : items) {
            boolean contains = false;

            // If item is inside itemsLeft
            for (ListItem itemLeft : itemsLeft) {
                if (item.getDbID().equals(itemLeft.getDbID())) {
                    contains = true;
                    itemsLeft.remove(itemLeft);
                    break;
                }
            }

            Debug.print(this, "onDataGetItems", item.toString(), 1);

            // If item made it to final results, add bonus point
            if (contains) {
                item.setBonus(item.getBonus() + 1);
                Debug.print(this, "true","", 1);
            } else {
                item.setPeril(item.getPeril() + 1);
                Debug.print(this, "false","", 1);

                // If peril points are over maximum peril points, drop item from list
                if (item.getPeril() > GlobalPrefs.loadMaxPerilPoints()) {
                    item.setPeril(0);
                    item.setFallen(true);
                }
            }

            DatabaseHandler.modifyItem(item);
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

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
