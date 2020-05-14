package io.github.tonimheinonen.whattodonext.database;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.github.tonimheinonen.whattodonext.tools.Buddy;

@IgnoreExtraProperties
public class SavedResult implements DatabaseValue {

    private String date;
    private String listName;
    private String voters;

    @Exclude
    private String dbID;

    /**
     * Default constructor.
     *
     * Required for calls to DataSnapshot.getValue(SavedResult.class).
     */
    public SavedResult() {}

    public SavedResult(String listName, ArrayList<Profile> voters) {
        SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat(Buddy.FIREBASE_DATE_FORMAT);

        this.date = ISO_8601_FORMAT.format(new Date());
        this.listName = listName;

        // Create string from voters (example: "Jack, Phil & Rose")
        StringBuilder voterNames = new StringBuilder();
        for (int i = 0; i < voters.size(); i++) {
            voterNames.append(voters.get(i).getName());
            if (i + 2 == voters.size())
                voterNames.append(" & ");
            else if (i + 1 < voters.size())
                voterNames.append(", ");
        }
        this.voters = voterNames.toString();
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getDbID() {
        return dbID;
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;
    }

    public String getVoters() {
        return voters;
    }

    public void setVoters(String voters) {
        this.voters = voters;
    }

    /**
     * Maps values for database handling.
     * @return mapped values
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("listName", listName);
        result.put("voters", voters);

        return result;
    }
}
