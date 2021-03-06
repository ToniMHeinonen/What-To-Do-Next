package io.github.tonimheinonen.whattodonext.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a list of items.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
@IgnoreExtraProperties
public class ListOfItems implements DatabaseValue, Parcelable {

    private String name;

    @Exclude
    private String dbID;

    @Exclude
    private boolean selected;

    @Exclude
    private ArrayList<ListItem> items = new ArrayList<>();

    /**
     * Default constructor.
     *
     * Required for calls to DataSnapshot.getValue(ListOfItems.class).
     */
    public ListOfItems() {}

    /**
     * Initializes necessary values.
     * @param name name of the list
     */
    public ListOfItems(String name) {
        this.name = name;
    }

    /**
     * Return name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns items.
     * @return items
     */
    public ArrayList<ListItem> getItems() {
        return items;
    }

    /**
     * Sets items
     * @param items items
     */
    public void setItems(ArrayList<ListItem> items) {
        this.items = items;
    }

    /**
     * Return database ID for list.
     * @return database ID
     */
    public String getDbID() {
        return dbID;
    }

    /**
     * Sets database ID for list.
     * @param dbID database ID
     */
    public void setDbID(String dbID) {
        this.dbID = dbID;
    }

    /**
     * Returns if list has been selected or not.
     * @return true if selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets if list is selected or not.
     * @param selected true if selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Maps values for database handling.
     * @return mapped values
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);

        return result;
    }

    /**
     * Prints this instead of pointer.
     * @return string to print
     */
    @Override
    public String toString() {
        return "ListOfItems{" +
                "name='" + name + '\'' +
                ", dbID='" + dbID + '\'' +
                '}';
    }

    ////////////////////////// PARCELABLE //////////////////////////

    /**
     * Describes contents
     * @return value
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes necessary values to parcel.
     * @param dest destination
     * @param flags flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dbID);
        dest.writeString(name);
        dest.writeList(items);
    }

    /**
     * Creates list from parcel info.
     * @param in given parcel
     */
    public ListOfItems(Parcel in) {
        dbID = in.readString();
        name = in.readString();
        items = in.readArrayList(ListItem.class.getClassLoader());
    }

    /**
     * Creator object for list.
     */
    public static final Parcelable.Creator<ListOfItems> CREATOR = new Parcelable.Creator<ListOfItems>() {
        /**
         * Creates from parcel.
         * @param in given parcel
         * @return list
         */
        public ListOfItems createFromParcel(Parcel in) {
            return new ListOfItems(in);
        }

        /**
         * Creates new list array.
         * @param size size of array
         * @return list array
         */
        public ListOfItems[] newArray(int size) {
            return new ListOfItems[size];
        }
    };
}
