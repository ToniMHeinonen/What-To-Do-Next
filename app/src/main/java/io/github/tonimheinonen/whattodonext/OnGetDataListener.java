package io.github.tonimheinonen.whattodonext;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

public interface OnGetDataListener {
    /**
     * Sends loaded lists from database.
     * @param lists loaded lists from database
     */
    void onDataGetLists(ArrayList<ListOfItems> lists);

    /**
     * Sends loaded items from database.
     * @param items loaded lists from database
     */
    void onDataGetItems(ArrayList<ListItem> items);

    /**
     * Sends loaded profiles from database.
     * @param profiles loaded lists from database
     */
    void onDataGetProfiles(ArrayList<Profile> profiles);
}
