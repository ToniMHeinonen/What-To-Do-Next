package io.github.tonimheinonen.whattodonext;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

public interface OnGetDataListener {
    void onDataGetLists(ArrayList<ListOfItems> lists);
    void onDataGetItems(ArrayList<ListItem> items);
    void onDataGetProfiles(ArrayList<Profile> profiles);
}
