package io.github.tonimheinonen.whattodonext;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.Profile;

public class VoteResultList {

    private Activity activity;
    private ArrayList<Profile> profiles;
    private ArrayList<ListItem> items;
    private boolean showVotes;
    private boolean lastResults;
    private ArrayList<ListItem> itemsToReset;

    public VoteResultList(Activity activity, ArrayList<Profile> profiles, ArrayList<ListItem> items,
                          ArrayList<ListItem> itemsToReset, boolean showVotes, boolean lastResults) {
        this.activity = activity;
        this.profiles = profiles;
        this.items = items;
        this.itemsToReset = itemsToReset;
        this.showVotes = showVotes;
        this.lastResults = lastResults;
    }

    public void show() {
        LinearLayout viewHolder = activity.findViewById(R.id.listHolder);

        // Add title and scrollable layout for items
        View resultTopicView = activity.getLayoutInflater().inflate(R.layout.vert_result_title, null);
        viewHolder.addView(resultTopicView);

        LinearLayout resultItemsLayout = resultTopicView.findViewById(R.id.resultItemsLayout);

        // Add all selected items
        for (ListItem item : items) {
            // Add item
            View resultItemView = activity.getLayoutInflater().inflate(R.layout.vert_result_item, null);
            resultItemsLayout.addView(resultItemView);

            // Set name and points
            TextView name = resultItemView.findViewById(R.id.resultName);
            name.setText(item.getName());
            TextView total = resultItemView.findViewById(R.id.resultTotal);
            total.setText(String.valueOf(item.getVotePoints()));
            TextView extra = resultItemView.findViewById(R.id.resultExtra);
            extra.setText(String.valueOf(item.getTotal()));

            // If show votes is true, add profiles and their points
            if (showVotes) {
                LinearLayout resultUserLayout = resultItemView.findViewById(R.id.resultUserLayout);

                for (Profile profile : profiles) {
                    View resultUserView = activity.getLayoutInflater().inflate(R.layout.vert_result_user, null);

                    int userPointsOnItem = profile.votePointsOfItem(item);
                    TextView resultUserPoints = resultUserView.findViewById(R.id.resultUserPoints);
                    resultUserPoints.setText(userPointsOnItem == 0 ? "" : String.valueOf(userPointsOnItem));

                    TextView resultUserName = resultUserView.findViewById(R.id.resultUserName);
                    resultUserName.setText(profile.getName());

                    resultUserLayout.addView(resultUserView);
                }
            }

            // Listen for clicks on last results
            if (lastResults) {
                View resultItemClickArea = resultItemView.findViewById(R.id.clickArea);
                resultItemClickArea.setOnClickListener((clicked) -> {
                    // Reset selected items
                    if (itemsToReset.contains(item)) {
                        itemsToReset.remove(item);
                        item.setSelected(false);
                        resultItemClickArea.setSelected(false);
                    } else {
                        itemsToReset.add(item);
                        item.setSelected(true);
                        resultItemClickArea.setSelected(true);
                    }
                });
            }
        }
    }
}
