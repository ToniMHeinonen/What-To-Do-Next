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

    /**
     * Initializes adapter for vote items.
     * @param aContext current context
     * @param data vote items
     */
    public VoteItemAdapter(Context aContext, ArrayList<ListItem> data) {
        this.data = data;
        layoutInflater = LayoutInflater.from(aContext);
    }

    /**
     * Returns size of the data.
     * @return size of the data
     */
    @Override
    public int getCount() {
        return data.size();
    }

    /**
     * Returns item at given position.
     * @param position index of the item
     * @return item
     */
    @Override
    public Object getItem(int position) {
        return data.get(position);
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
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.vote_item, null);
        }

        ListItem item = (ListItem) getItem(position);

        if (item != null) {
            TextView itemName = view.findViewById(R.id.voteName);
            itemName.setText(data.get(position).getName());

            TextView itemVoteAmount = view.findViewById(R.id.voteAmount);
            int points = data.get(position).getVotePoints();
            itemVoteAmount.setText(points == 0 ? "" : String.valueOf(points));
        }

        return view;
    }
}
