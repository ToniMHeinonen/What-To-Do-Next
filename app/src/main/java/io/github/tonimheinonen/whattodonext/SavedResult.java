package io.github.tonimheinonen.whattodonext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import io.github.tonimheinonen.whattodonext.database.ListItem;

public class SavedResult implements Serializable {

    private ArrayList<ResultItem> resultItems;
    private Date time;

    public SavedResult(ArrayList<ResultItem> resultItems) {
        this.resultItems = resultItems;
        this.time = new Date();
    }
}
