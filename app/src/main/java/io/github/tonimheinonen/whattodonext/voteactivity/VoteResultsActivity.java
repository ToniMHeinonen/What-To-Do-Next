package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.ResultsShowVotesAdapter;
import io.github.tonimheinonen.whattodonext.VoteResultList;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.database.DatabaseType;
import io.github.tonimheinonen.whattodonext.database.DatabaseValueListAdapter;
import io.github.tonimheinonen.whattodonext.database.GlobalSettings;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.database.OnlineProfile;
import io.github.tonimheinonen.whattodonext.database.Profile;
import io.github.tonimheinonen.whattodonext.database.SavedResult;
import io.github.tonimheinonen.whattodonext.database.SavedResultItem;
import io.github.tonimheinonen.whattodonext.database.VoteRoom;
import io.github.tonimheinonen.whattodonext.database.VoteSettings;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.Debug;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;
import io.github.tonimheinonen.whattodonext.tools.HTMLDialog;
import io.github.tonimheinonen.whattodonext.tools.ResultStyle;

/**
 * Handles showing results of voting.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class VoteResultsActivity extends VotingParentActivity {

    private boolean lastResults;
    private VoteSettings voteSettings;
    private GlobalSettings globalSettings;
    private int topAmount;
    private ListOfItems selectedList;
    private ArrayList<Profile> selectedProfiles;
    private final ArrayList<ListItem> itemsToReset = new ArrayList<>();

    private boolean isOfflineOrIsOnlineHost;

    // Online
    private boolean isOnline;
    private VoteRoom voteRoom;
    private OnlineProfile onlineProfile;

    // Options
    private int listVoteSizeLast;
    private int maxPerilPoints;
    private boolean ignoreUnselected;
    private boolean showVotes;
    private boolean halveExtra;

    /**
     * Initializes VoteResultsActivity.
     * @param savedInstanceState previous activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_results);

        isOnline = voteMaster.isOnline();

        voteSettings = voteMaster.getVoteSettings();
        globalSettings = voteMaster.getGlobalSettings();
        selectedList = voteMaster.getSelectedList();

        if (isOnline) {
            onlineProfile = voteMaster.getOnlineProfile();
            voteRoom = voteMaster.getVoteRoom();

            // Get correct vote amount
            if (onlineProfile.getState() == VoteRoom.RESULTS_FIRST)
                topAmount = voteSettings.getFirstVote();
            else
                topAmount = voteSettings.getLastVote();
        } else {
            topAmount = voteMaster.getTopAmount();
            selectedProfiles = voteMaster.getSelectedProfiles();
        }

        isOfflineOrIsOnlineHost = !isOnline || onlineProfile.isHost();
        setOptions();

        // If current topAmount is the same as last vote size
        lastResults = topAmount == listVoteSizeLast;

        if (lastResults) {
            // If tutorial has not been confirmed yet, show it
            if (GlobalPrefs.loadPopupInfo(GlobalPrefs.TUTORIAL_LAST_RESULTS))
                new HTMLDialog(this, HTMLDialog.HTMLText.TUTORIAL_LAST_RESULTS).show();

            TextView next = findViewById(R.id.nextButton);
            next.setText(getString(R.string.save));
            findViewById(R.id.resultsInfoText).setVisibility(View.VISIBLE);
        }

        // Generate some values when voting online
        if (isOnline) {
            // Retrieve all profiles
            DatabaseHandler.getOnlineProfiles(voteRoom, (onlineProfiles -> {
                // Retrieve all disconnected profiles and add them to profiles if not duplicate
                DatabaseHandler.getDisconnectedOnlineProfiles(voteRoom, (disconnectedProfiles) -> {
                    for (OnlineProfile pro : disconnectedProfiles) {
                        if (!onlineProfiles.contains(pro))
                            onlineProfiles.add(pro);
                    }

                    // Retrieve all vote room items
                    DatabaseHandler.getVoteRoomItems(voteRoom, (items -> {
                        // Generate list from correct items
                        ListOfItems.generateOnlineListOfItems(selectedList, onlineProfile, items);

                        // Get vote room items
                        DatabaseHandler.getVoteRoomVotedItems(voteRoom, onlineProfile, (votedItems) -> {
                            try {
                                // Create temporary SelectedProfiles so code does not have to be modified so much
                                selectedProfiles = Profile.generateProfilesFromOnlineProfile(
                                        onlineProfiles, selectedList.getItems(), votedItems, onlineProfile, voteSettings);

                                startSetup();
                            } catch (Exception e) {
                                Debug.error("VoteResultsActivity", "getVoteRoomVotedItems", e, 1);
                            }
                        });
                    }));
                });
            }));
        } else {
            startSetup();
        }
    }

    private void startSetup() {
        if (isOnline) {
            // If it's the last results and halve is selected, halve the total bonus points on item
            if (lastResults && halveExtra) {
                Buddy.halveTotalBonusPoints(selectedList);
            }
        }

        calculateVotePoints();

        int resultStyle = globalSettings.getResultStyle();

        if (resultStyle == ResultStyle.VERTICAL)
            setupItemListVertical();
        else if (resultStyle == ResultStyle.HORIZONTAL)
            setupItemListHorizontal();
    }

    /**
     * Sets options for voting.
     */
    private void setOptions() {
        listVoteSizeLast = voteSettings.getLastVote();
        maxPerilPoints = voteSettings.getMaxPeril();
        ignoreUnselected = voteSettings.isIgnoreUnselected();
        showVotes = voteSettings.isShowVoted();
        halveExtra = voteSettings.isHalveExtra();
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
                        item.addVoterAmount();
                        break;
                    }
                }
            }
        }

        // Add extra points to total vote points
        for (ListItem item : items) {
            // If first vote, user wants to ignore unselected and item was not voted at all
            if (!lastResults && ignoreUnselected && item.getVotePoints() == 0)
                continue;

            item.addVotePoints(item.getTotal());
            Debug.print(this, "item", "name: " + item.getName(), 1);
            Debug.print(this, "item", "points: " + item.getVotePoints(), 1);
        }

        // Sort items by vote points and voters
        Collections.sort(items, new Comparator<ListItem>() {
            public int compare(ListItem o1, ListItem o2) {
                return o2.compareTo(o1);
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

            if (isOnline) {
                // Set is in online vote to true in online vote room
                for (ListItem item : selectedItems)
                    item.setInOnlineLastVote(true);

                DatabaseHandler.setVoteRoomLastVoteItems(voteRoom, selectedItems);
            }

            selectedList.setItems(selectedItems);
        }
    }

    /**
     * Shows results in list view.
     */
    private void setupItemListVertical() {
        VoteResultList voteResultList = new VoteResultList(this, selectedProfiles,
                selectedList.getItems(), itemsToReset, showVotes, lastResults);
        voteResultList.show();
    }

    /**
     * Shows results in list view.
     */
    private void setupItemListHorizontal() {
        // Load whether to show vote items or not
        LinearLayout viewHolder = findViewById(R.id.listHolder);
        BaseAdapter adapter;
        View child;
        if (showVotes) {
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
        if (isOnline) {
            // Online
            Buddy.showOnlineVoteLoadingBar(this);

            if (lastResults) {
                if (Buddy.isRegistered) {
                    // If tutorial has not been confirmed yet, show it
                    if (GlobalPrefs.loadPopupInfo(GlobalPrefs.TUTORIAL_VOTE_COMPLETE)) {
                        Buddy.hideOnlineVoteLoadingBar(this);
                        showVoteCompleteTutorial();
                    } else {
                        DatabaseHandler.getVoteRoomItems(voteRoom, this::itemsLoaded);
                    }
                } else {
                    endVoting(getString(R.string.unregistered_vote_ended));
                }
            } else {
                // Move to last voting state
                moveToOnlineLastVote();
            }
        } else {
            // Local
            if (lastResults) {
                // If tutorial has not been confirmed yet, show it
                if (GlobalPrefs.loadPopupInfo(GlobalPrefs.TUTORIAL_VOTE_COMPLETE)) {
                    showVoteCompleteTutorial();
                } else {
                    Buddy.showLoadingBar(this);
                    DatabaseHandler.getItems(this::itemsLoaded, selectedList);
                }
            } else {
                // Proceed to next voting
                voteMaster.setTopAmount(listVoteSizeLast);
                parentStartActivity(new Intent(this, VoteTopActivity.class));
            }
        }
    }

    private void showVoteCompleteTutorial() {
        HTMLDialog tutorial = new HTMLDialog(this, HTMLDialog.HTMLText.TUTORIAL_VOTE_COMPLETE);
        tutorial.show();
        tutorial.setOnDismissListener((complete) -> nextPressed(null));
    }

    private void moveToOnlineLastVote() {
        DatabaseHandler.changeOnlineProfileState(voteRoom, onlineProfile, () -> {
            parentStartActivity(new Intent(this, VoteTopActivity.class));
        });
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
        if (isOnline)
            Buddy.exitVoting(this, () -> DatabaseHandler.disconnectOnlineProfile(voteRoom, onlineProfile));
        else
            Buddy.exitVoting(this, null);
    }

    /**
     * Calculates bonus and peril points for items.
     * @param items loaded lists from database
     */
    public void itemsLoaded(ArrayList<ListItem> items) {
        if (!isOnline)
            Buddy.filterListByFallen(items, false); // Ignore fallen items

        // Set items left in the last results
        ArrayList<ListItem> itemsLeft = selectedList.getItems();

        // Saving results
        ArrayList<SavedResultItem> resultsSaving = new ArrayList<>();
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
                resultsSaving.add(new SavedResultItem(votePosition, item,
                        SavedResultItem.RESET, maxPerilPoints));
                item.setBonus(0);
                item.setPeril(0);
            } else {
                resultsSaving.add(new SavedResultItem(votePosition, item,
                        SavedResultItem.BONUS, maxPerilPoints));
                item.setBonus(item.getBonus() + 1);
            }

            if (isOfflineOrIsOnlineHost)
                DatabaseHandler.modifyItem(item);

            votePosition++;
        }

        // Loop through all of the rest database items and add peril point to them,
        // since they did not make it in to the last vote
        for (ListItem item: items) {
            resultsSaving.add(new SavedResultItem(-1, item,
                    SavedResultItem.PERIL, maxPerilPoints));
            item.setPeril(item.getPeril() + 1);

            // If peril points are over maximum peril points, drop item from list
            if (item.getPeril() > maxPerilPoints) {
                item.setPeril(0);
                item.setFallen(true);
            }

            if (isOfflineOrIsOnlineHost)
                DatabaseHandler.modifyItem(item);
        }

        // Increase times voted by 1 and modify list in database
        selectedList.setTimesVoted(selectedList.getTimesVoted() + 1);

        if (isOfflineOrIsOnlineHost)
            DatabaseHandler.modifyList(selectedList);

        // Generate result
        SavedResult result = new SavedResult(selectedList, selectedProfiles);
        DatabaseHandler.addResult(result, resultsSaving,
                () -> endVoting(getString(R.string.save_successful)));
    }

    private void endVoting(String toastToShow) {
        // Disconnect the user from the vote room
        if (isOnline)
            DatabaseHandler.disconnectOnlineProfile(voteRoom, onlineProfile);

        Buddy.showToast(toastToShow, Toast.LENGTH_LONG);
        Buddy.resetToMenuScreen(this);
    }
}
