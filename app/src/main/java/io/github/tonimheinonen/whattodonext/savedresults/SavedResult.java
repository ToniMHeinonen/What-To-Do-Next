package io.github.tonimheinonen.whattodonext.savedresults;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import io.github.tonimheinonen.whattodonext.database.DatabaseValue;
import io.github.tonimheinonen.whattodonext.database.Profile;

public class SavedResult implements Serializable, DatabaseValue {

    public final Date date;
    public final String listName;
    public final ArrayList<String> voterNames = new ArrayList<>();
    public final ArrayList<SavedResultItem> resultItems;

    public SavedResult(String listName, ArrayList<SavedResultItem> resultItems, ArrayList<Profile> voters) {
        this.date = new Date();
        this.listName = listName;
        this.resultItems = resultItems;

        for (Profile p : voters) {
            voterNames.add(p.getName());
        }
    }
}
