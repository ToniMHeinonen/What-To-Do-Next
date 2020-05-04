package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.Profile;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.MyViewHolder> {

    private Context context;
    private List<ListItem> itemsList;
    private ArrayList<Profile> profiles;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView name, extra, votes;
        public TextView[] votePoints;

        public MyViewHolder(View view) {
            super(view);
            this.view = view;
            name = view.findViewById(R.id.resultsName);
            extra = view.findViewById(R.id.resultsBonus);
            votes = view.findViewById(R.id.resultsVotePoints);
            votePoints = new TextView[profiles.size()];
        }
    }


    public ResultsAdapter(Context context, List<ListItem> itemsList, ArrayList<Profile> profiles) {
        this.itemsList = itemsList;
        this.profiles = profiles;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.result_show_votes_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ListItem item = itemsList.get(position);
        holder.name.setText(item.getName());
        holder.extra.setText(String.valueOf(item.getTotal()));
        holder.votes.setText(String.valueOf(item.getVotePoints()));

        // Add vote points to layout
        LinearLayout layout = holder.view.findViewById(R.id.resultItemLayout);
        LayoutInflater inflater = LayoutInflater.from(context);
        for (int i = 0; i < profiles.size(); i++) {
            ListItem voteItem = itemsList.get(position);
            Profile profile = profiles.get(i);
            int voteAmount = profile.votePointsOfItem(voteItem);

            // Use "layout" and "false" as parameters to get the width and height from result_voter_points
            TextView vote = (TextView) inflater.inflate(R.layout.result_voter_points, layout, false);
            vote.setText(voteAmount == 0 ? "" : String.valueOf(voteAmount));
            layout.addView(vote);

            holder.votePoints[i] = vote;
        }
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }
}
