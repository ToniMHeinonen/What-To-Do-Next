package io.github.tonimheinonen.whattodonext.voteactivity;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.listsactivity.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.database.Profile;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Handles voting top list with List Items.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class VoteTopActivity extends AppCompatActivity {

    private int topAmount;
    private ListOfItems selectedList;
    private ArrayList<Profile> selectedProfiles;

    private Button nextButton;
    private TextView profileView, infoView;
    private DatabaseValueListAdapter adapter;

    private ArrayList<Integer> votePoints;
    private ArrayList<ListItem> votedItems;
    private int currentVotePoint, currentProfileIndex;
    private Profile currentProfile;

    /**
     * Initializes VoteTopActivity.
     * @param savedInstanceState previous activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_top);

        Intent intent = getIntent();
        topAmount = intent.getIntExtra("topAmount", -1);
        selectedList = intent.getExtras().getParcelable("selectedList");
        selectedProfiles = intent.getParcelableArrayListExtra("selectedProfiles");

        profileView = findViewById(R.id.profileName);
        infoView = findViewById(R.id.voteInfoText);
        nextButton = findViewById(R.id.nextButton);

        Buddy.sortItemsByName(selectedList.getItems(), true);

        setupVoteItems();

        startVoting();
    }

    /**
     * Setups voting items.
     */
    private void setupVoteItems() {
        final ListView list = findViewById(R.id.voteItems);
        adapter = new DatabaseValueListAdapter(this, selectedList.getItems(), null,
                DatabaseType.VOTE_HIDE_EXTRA);

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item = (ListItem) list.getItemAtPosition(position);

                if (votedItems.contains(item)) {
                    votedItems.remove(item);
                    int points = item.retrieveVotePoints();
                    addVotePoint(points);
                    // Index -1 since index starts at 0, vote points start at 1
                    currentProfile.removeVoteItem(points - 1);
                } else {
                    // If all points have not been given
                    if (!votePoints.isEmpty()) {
                        votedItems.add(item);
                        item.setVotePoints(currentVotePoint);
                        // Index -1 since index starts at 0, vote points start at 1
                        currentProfile.addVoteItem(currentVotePoint - 1, position);
                        removeVotePoint();
                    }
                }
            }
        });
    }

    /**
     * Sorts vote points so highest available number is always used.
     */
    private void sortVotePoints() {
        Collections.sort(votePoints, Collections.<Integer>reverseOrder());
        currentVotePoint = votePoints.get(0);
    }

    /**
     * Adds vote point back to the available points.
     * @param value points value
     */
    private void addVotePoint(int value) {
        votePoints.add(value);
        sortVotePoints();
        updateVoteItems();
    }

    /**
     * Removes vote point from available points.
     */
    private void removeVotePoint() {
        votePoints.remove(0);

        if (!votePoints.isEmpty()) {
            currentVotePoint = votePoints.get(0);
        } else {
            currentVotePoint = -1;
        }

        updateVoteItems();
    }

    /**
     * Update views.
     */
    private void updateVoteItems() {
        if (!votePoints.isEmpty()) {
            infoView.setText(getString(R.string.give_points_to, currentVotePoint));
            nextButton.setVisibility(View.INVISIBLE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
            infoView.setText(getString(R.string.vote_ready, getString(R.string.vote_next)));
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Loads correct profile for voting and resets necessary values.
     */
    private void startVoting() {
        votePoints = new ArrayList<>();
        votedItems = new ArrayList<>();

        currentProfile = selectedProfiles.get(currentProfileIndex);
        currentProfile.initVoteSize(topAmount);

        profileView.setText(currentProfile.getName());

        // Add vote points to list
        for (int i = topAmount; i > 0; i--) {
            votePoints.add(i);
        }

        // Pick highest available number
        currentVotePoint = votePoints.get(0);

        // Clear previous vote points from items
        for (ListItem item : selectedList.getItems()) {
            item.setVotePoints(0);
        }

        updateVoteItems();
    }

    /**
     * Make exit prompt when clicking exit.
     * @param v exit view
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
     * Checks whether to move to next profile or go to results.
     * @param v next button view
     */
    public void nextPressed(View v) {
        currentProfileIndex++;

        if (currentProfileIndex == selectedProfiles.size()) {
            finish();
            // Move to results screen
            Intent intent = new Intent(this, VoteResultsActivity.class);
            intent.putExtra("topAmount", topAmount);
            intent.putExtra("selectedList", selectedList);
            intent.putParcelableArrayListExtra("selectedProfiles", selectedProfiles);
            startActivity(intent);
        } else {
            startVoting();
        }
    }
}
