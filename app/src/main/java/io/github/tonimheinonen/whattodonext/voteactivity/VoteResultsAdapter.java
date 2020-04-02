package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;

public class VoteResultsAdapter extends BaseAdapter {

    private ArrayList<ListItem> data;
    private LayoutInflater layoutInflater;

    public VoteResultsAdapter(Context aContext, ArrayList<ListItem> data) {
        this.data = data;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.results_item, null);
        }

        ListItem item = (ListItem) getItem(position);

        if (item != null) {
            TextView itemName = view.findViewById(R.id.resultsName);
            itemName.setText(data.get(position).getName());

            TextView itemBonus = view.findViewById(R.id.resultsBonus);
            itemBonus.setText(String.valueOf(data.get(position).getTotal()));

            TextView itemPoints = view.findViewById(R.id.resultsVotePoints);
            itemPoints.setText(String.valueOf(data.get(position).getVotePoints()));
        }

        return view;
    }
}
