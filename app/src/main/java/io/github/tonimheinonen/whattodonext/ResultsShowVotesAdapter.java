package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.Profile;

/**
 * Handles List View element with dynamic columns.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.2
 * @since 1.2
 */
public class ResultsShowVotesAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ListItem> listData;
    private ArrayList<Profile> profiles;
    private LayoutInflater layoutInflater;

    /**
     * Initializes adapter for list items.
     * @param aContext current context
     * @param listData list items
     * @param profiles selected profiles
     */
    public ResultsShowVotesAdapter(Context aContext, ArrayList<ListItem> listData, ArrayList<Profile> profiles) {
        this.context = aContext;
        this.listData = listData;
        this.profiles = profiles;
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

    /**
     * Creates view of the given data.
     * @param position index of the data
     * @param convertView part of the view reserved for current data
     * @param parent listview parent
     * @return view of the data
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.result_show_votes_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ListItem item = listData.get(position);

        holder.name.setText(item.getName());
        holder.extra.setText(String.valueOf(item.getTotal()));
        holder.total.setText(String.valueOf(item.getVotePoints()));

        // Set correct text color
        holder.name.setTextColor(context.getResources().getColor(item.isSelected() ?
                R.color.colorPrimary : R.color.defaultTextColor));

        // Loop through voters and update their given points
        for (int i = 0; i < profiles.size(); i++) {
            Profile profile = profiles.get(i);                  // Get profile
            TextView vote = holder.votePoints[i];               // Get textview for vote point
            int voteAmount = profile.votePointsOfItem(item);    // Get vote points amount for item
            vote.setText(voteAmount == 0 ? "" : String.valueOf(voteAmount));
        }

        return convertView;
    }

    /**
     * Class for holding view data.
     */
    class ViewHolder {
        View view;
        TextView name, extra, total;
        public TextView[] votePoints;

        /**
         * Holds all the necessary data for the view.
         * @param view convertview
         */
        public ViewHolder(View view) {
            this.view = view;
            name = view.findViewById(R.id.resultsName);
            extra = view.findViewById(R.id.resultsBonus);
            total = view.findViewById(R.id.resultsVotePoints);
            votePoints = new TextView[profiles.size()];

            // Add vote points to layout
            LinearLayout layout = view.findViewById(R.id.resultItemLayout);
            for (int i = 0; i < profiles.size(); i++) {
                // Use "layout" and "false" as parameters to get the width and height from result_voter_points
                TextView vote = (TextView) layoutInflater.inflate(R.layout.result_voter_points, layout, false);
                layout.addView(vote);
                votePoints[i] = vote;
            }
        }
    }
}
