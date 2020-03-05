package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListItemAdapter extends BaseAdapter {

    private ArrayList<ListItem> listData;
    private LayoutInflater layoutInflater;

    public ListItemAdapter(Context aContext, ArrayList<ListItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.txtItemName = convertView.findViewById(R.id.name);
            holder.txtItemBonus = convertView.findViewById(R.id.bonus);
            holder.txtItemPeril = convertView.findViewById(R.id.peril);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.txtItemName.setText(listData.get(position).getName());
        holder.txtItemBonus.setText(String.valueOf(listData.get(position).getBonus()));
        holder.txtItemPeril.setText(String.valueOf(listData.get(position).getPeril()));

        return convertView;
    }

    static class ViewHolder {
        TextView txtItemName;
        TextView txtItemBonus;
        TextView txtItemPeril;
    }
}
