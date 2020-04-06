package io.github.tonimheinonen.whattodonext.listsactivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.R;

public class ListItemAdapter extends BaseAdapter {

    private ArrayList<ListItem> listData;
    private LayoutInflater layoutInflater;

    /**
     * Initializes adapter for list items.
     * @param aContext current context
     * @param listData list items
     */
    public ListItemAdapter(Context aContext, ArrayList<ListItem> listData) {
        this.listData = listData;
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
            convertView = layoutInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.txtItemName = convertView.findViewById(R.id.name);
            holder.txtItemTotal = convertView.findViewById(R.id.total);
            holder.txtItemBonus = convertView.findViewById(R.id.bonus);
            holder.txtItemPeril = convertView.findViewById(R.id.peril);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.txtItemName.setText(listData.get(position).getName());
        holder.txtItemTotal.setText(String.valueOf(listData.get(position).getTotal()));
        holder.txtItemBonus.setText(String.valueOf(listData.get(position).getBonus()));
        holder.txtItemPeril.setText(String.valueOf(listData.get(position).getPeril()));

        return convertView;
    }

    /**
     * Static class for holding view data.
     */
    static class ViewHolder {
        TextView txtItemName;
        TextView txtItemTotal;
        TextView txtItemBonus;
        TextView txtItemPeril;
    }
}
