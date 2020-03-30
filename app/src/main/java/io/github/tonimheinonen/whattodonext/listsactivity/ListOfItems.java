package io.github.tonimheinonen.whattodonext.listsactivity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.tonimheinonen.whattodonext.DatabaseValue;

@IgnoreExtraProperties
public class ListOfItems implements DatabaseValue, Parcelable {

    private String name;

    @Exclude
    private String dbID;

    @Exclude
    private ArrayList<ListItem> items = new ArrayList<>();

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

        return result;
    }

    @Override
    public String toString() {
        return "ListOfItems{" +
                "name='" + name + '\'' +
                ", dbID='" + dbID + '\'' +
                '}';
    }

    ////////////////////////// PARCELABLE //////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dbID);
        dest.writeString(name);
        dest.writeList(items);
    }

    public ListOfItems(Parcel in) {
        dbID = in.readString();
        name = in.readString();
        items = in.readArrayList(ListItem.class.getClassLoader());
    }

    public static final Parcelable.Creator<ListOfItems> CREATOR = new Parcelable.Creator<ListOfItems>() {
        public ListOfItems createFromParcel(Parcel in) {
            return new ListOfItems(in);
        }

        public ListOfItems[] newArray(int size) {
            return new ListOfItems[size];
        }
    };
}
