package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.DatabaseValue;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;

public class VoteItemAdapter extends BaseAdapter {

    private ArrayList<ListItem> data;
    private LayoutInflater layoutInflater;

    public VoteItemAdapter(Context aContext, ArrayList<ListItem> data) {
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
            view = layoutInflater.inflate(R.layout.vote_item, null);
        }

        ListItem item = (ListItem) getItem(position);

        if (item != null) {
            TextView itemName = view.findViewById(R.id.voteName);
            itemName.setText(data.get(position).getName());

            TextView itemVoteAmount = view.findViewById(R.id.voteAmount);
            int points = data.get(position).getCurrentVotePoints();
            itemVoteAmount.setText(points == 0 ? "" : String.valueOf(points));
        }

        return view;
    }
}
