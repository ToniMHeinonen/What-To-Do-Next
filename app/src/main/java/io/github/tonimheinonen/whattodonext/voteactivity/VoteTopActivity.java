package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.ListItemFragment;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.database.OnlineProfile;
import io.github.tonimheinonen.whattodonext.database.OnlineVotedItem;
import io.github.tonimheinonen.whattodonext.database.Profile;
import io.github.tonimheinonen.whattodonext.database.VoteRoom;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

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
    private ListItemFragment itemsFragment;

    private ArrayList<Integer> votePoints;
    private ArrayList<ListItem> votedItems;
    private int currentVotePoint, currentProfileIndex;
    private Profile currentProfile;

    // Online
    private boolean isOnline;
    private VoteRoom voteRoom;
    private OnlineProfile onlineProfile;

    // Options
    private int listVoteSizeLast;
    private boolean showExtra;
    private boolean halveExtra;

    /**
     * Initializes VoteTopActivity.
     * @param savedInstanceState previous activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_top);
        // Lock orientation during voting to prevent million different problems
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        Intent intent = getIntent();
        isOnline = intent.getBooleanExtra("isOnline", false);
        selectedList = intent.getParcelableExtra("selectedList");

        if (isOnline) {
            onlineProfile = intent.getParcelableExtra("onlineProfile");
            voteRoom = intent.getParcelableExtra("voteRoom");

            // Get correct vote amount
            if (voteRoom.getState().equals(VoteRoom.VOTING_FIRST))
                topAmount = voteRoom.getFirstVoteSize();
            else
                topAmount = voteRoom.getLastVoteSize();
        } else {
            topAmount = intent.getIntExtra("topAmount", -1);
            selectedProfiles = intent.getParcelableArrayListExtra("selectedProfiles");
        }

        setOptions();

        profileView = findViewById(R.id.profileName);
        infoView = findViewById(R.id.voteInfoText);
        nextButton = findViewById(R.id.nextButton);

        // If it's the last vote and halve is selected, halve the total bonus points on item
        if (topAmount == listVoteSizeLast && halveExtra) {
            for (ListItem item : selectedList.getItems()) {
                item.setTotal((int) Math.ceil((double) item.getTotal() / 2));
            }
        }

        // Setup voting items
        itemsFragment = Buddy.createListItemFragment(this,
                showExtra ? DatabaseType.VOTE_SHOW_EXTRA : DatabaseType.VOTE_HIDE_EXTRA,
                selectedList);

        startVoting();
    }

    /**
     * Sets options for voting.
     *
     * Online and local vote retrieves options from different places.
     */
    private void setOptions() {
        if (isOnline) {
            listVoteSizeLast = voteRoom.getLastVoteSize();
            showExtra = voteRoom.isShowExtra();
            halveExtra = voteRoom.isHalveExtra();
        } else {
            listVoteSizeLast = GlobalPrefs.loadListVoteSizeSecond();
            showExtra = GlobalPrefs.loadShowExtra();
            halveExtra = GlobalPrefs.loadHalveExtra();
        }
    }

    public void itemClicked(ListItem item) {
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
                currentProfile.addVoteItem(currentVotePoint - 1, item);
                removeVotePoint();
            }
        }
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

        itemsFragment.updateListItems();
    }

    /**
     * Loads correct profile for voting and resets necessary values.
     */
    private void startVoting() {
        votePoints = new ArrayList<>();
        votedItems = new ArrayList<>();

        if (isOnline) {
            currentProfile = new Profile(onlineProfile.getNickName());
        } else {
            currentProfile = selectedProfiles.get(currentProfileIndex);
        }
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
        if (isOnline)
            Buddy.exitVoting(this, () -> DatabaseHandler.disconnectOnlineProfile(voteRoom, onlineProfile));
        else
            Buddy.exitVoting(this, null);
    }

    /**
     * Checks whether to move to next profile or go to results.
     * @param v next button view
     */
    public void nextPressed(View v) {
        if (isOnline) {
            Buddy.showLoadingBar(this);

            DatabaseHandler.getVoteRoomState(voteRoom, (state) -> {
                if (state.equals(VoteRoom.RESULTS_FIRST)) {
                    Buddy.showToast(getString(R.string.waiting_for_host), Toast.LENGTH_LONG);
                    Buddy.hideLoadingBar(this);
                } else {
                    moveToWaitingRoom();
                }
            });
        } else {
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

    private void moveToWaitingRoom() {
        // Convert voted items to OnlineVotedItem
        ArrayList<OnlineVotedItem> onlineVotedItems = new ArrayList<>();
        for (int i = 0; i < currentProfile.getVotedItems().length; i++) {
            OnlineVotedItem votedItem = new OnlineVotedItem(onlineProfile.getUserID(),
                    currentProfile.getVoteItem(i).getDbID(), i + 1);
            onlineVotedItems.add(votedItem);
        }

        // Add voted items to the vote room
        DatabaseHandler.addVoteRoomVotedItems(voteRoom, onlineVotedItems, () -> {
            // Change user's ready state
            DatabaseHandler.setOnlineProfileReady(voteRoom, onlineProfile, true, () -> {
                // Move to waiting room when ready has changed
                Intent intent = new Intent(this, VoteWaitingActivity.class);
                intent.putExtra("voteRoom", voteRoom);
                intent.putExtra("onlineProfile", onlineProfile);
                intent.putExtra("selectedList", selectedList);
                startActivity(intent);
            });
        });
    }
}
