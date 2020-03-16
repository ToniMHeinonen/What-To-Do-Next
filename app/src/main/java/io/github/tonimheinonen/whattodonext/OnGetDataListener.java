package io.github.tonimheinonen.whattodonext;

import java.util.HashMap;

import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

public interface OnGetDataListener {
    public void onDataGetSuccess(HashMap<String, ListOfItems> lists);
}
