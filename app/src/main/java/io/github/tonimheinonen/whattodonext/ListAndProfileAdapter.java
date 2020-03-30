package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

public class ListAndProfileAdapter extends BaseAdapter {

    private ArrayList<? extends DatabaseValue> listData;
    private LayoutInflater layoutInflater;
    private View.OnClickListener listener;

    public ListAndProfileAdapter(Context aContext, ArrayList<? extends DatabaseValue> listData, View.OnClickListener listener) {
        this.listData = listData;
        this.listener = listener;
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
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.saved_list_and_profile, null);
        }

        DatabaseValue item = (DatabaseValue) getItem(position);

        if (item != null) {
            Button listName = view.findViewById(R.id.savedName);
            listName.setText(listData.get(position).getName());
            listName.setOnClickListener(listener);
            listName.setTag(position);

            Button listDelete = view.findViewById(R.id.savedDelete);
            listDelete.setOnClickListener(listener);
            listDelete.setTag(position);
        }

        return view;
    }
}
