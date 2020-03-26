package io.github.tonimheinonen.whattodonext.listsactivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.R;

public class ListAdapter extends BaseAdapter {

    private ArrayList<ListOfItems> listData;
    private LayoutInflater layoutInflater;
    private ListDialog dialog;

    public ListAdapter(Context aContext, ArrayList<ListOfItems> listData, ListDialog dialog) {
        this.listData = listData;
        this.dialog = dialog;
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
            convertView = layoutInflater.inflate(R.layout.saved_list, null);
            holder = new ViewHolder();
            holder.listName = convertView.findViewById(R.id.savedListName);
            holder.listDelete = convertView.findViewById(R.id.savedListDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.listName.setText(listData.get(position).getName());
        holder.listName.setOnClickListener(dialog);
        holder.listName.setTag(position);

        holder.listDelete.setOnClickListener(dialog);
        holder.listDelete.setTag(position);

        return convertView;
    }

    static class ViewHolder {
        Button listName;
        Button listDelete;
    }
}
