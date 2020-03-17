package io.github.tonimheinonen.whattodonext.listsactivity;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class ListOfItems {

    private String name;
    private ArrayList<ListItem> items = new ArrayList<>();

    @Exclude
    private String dbID;

    public ListOfItems() {
        // Default constructor required for calls to DataSnapshot.getValue(ListOfItems.class)
    }

    public ListOfItems(String name) {
        this.name = name;
    }

    public ListOfItems(String name, ArrayList<ListItem> items) {
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ListItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<ListItem> items) {
        this.items = items;
    }

    public String getDbID() {
        return dbID;
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("items", items);

        return result;
    }
}
