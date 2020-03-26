package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.listsactivity.ListDialog;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

public class ListAndProfileAdapter extends BaseAdapter {

    private ArrayList<? extends DatabaseValue> listData;
    private LayoutInflater layoutInflater;
    private ListDialog dialog;

    public ListAndProfileAdapter(Context aContext, ArrayList<? extends DatabaseValue> listData, ListDialog dialog) {
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
            convertView = layoutInflater.inflate(R.layout.saved_list_and_profile, null);
            holder = new ViewHolder();
            holder.listName = convertView.findViewById(R.id.savedName);
            holder.listDelete = convertView.findViewById(R.id.savedDelete);
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
