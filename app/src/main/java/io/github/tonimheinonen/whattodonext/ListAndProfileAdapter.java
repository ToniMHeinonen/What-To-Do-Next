package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Handles ListView with ListOfView and Profile.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class ListAndProfileAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<? extends DatabaseValue> listData;
    private LayoutInflater layoutInflater;
    private View.OnClickListener listener;

    /**
     * Adapter for handling ListView with list and profile.
     * @param aContext current context
     * @param listData data to be added to ListView
     * @param listener click listener
     */
    public ListAndProfileAdapter(Context aContext, ArrayList<? extends DatabaseValue> listData, View.OnClickListener listener) {
        this.context = aContext;
        this.listData = listData;
        this.listener = listener;
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

            if (item.isSelected())
                listName.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            else
                listName.setTextColor(context.getResources().getColor(R.color.profileAndListOfItemsTextColor));

            Button listDelete = view.findViewById(R.id.savedDelete);
            listDelete.setOnClickListener(listener);
            listDelete.setTag(position);
        }

        return view;
    }
}
