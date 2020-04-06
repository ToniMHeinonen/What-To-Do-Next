package io.github.tonimheinonen.whattodonext;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

/**
 * Listens data retrieval from Firebase database.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
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
