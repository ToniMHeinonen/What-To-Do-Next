package io.github.tonimheinonen.whattodonext.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents profile.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
@IgnoreExtraProperties
public class Profile implements DatabaseValue, Parcelable {

    private String name;

    @Exclude
    private String dbID;

    @Exclude
    private boolean selected;

    @Exclude
    private ListItem[] votedItems;

    /**
     * Default constructor.
     */
    public Profile() {
        // Default constructor required for calls to DataSnapshot.getValue(ListOfItems.class)
    }

    /**
     * Initializes profile with the given name.
     * @param name profile name
     */
    public Profile(String name) {
        this.name = name;
    }

    /**
     * Returns if profile has been selected or not.
     * @return true if selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets if profile is selected or not.
     * @param selected true if selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Returns name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns database ID.
     * @return database ID
     */
    public String getDbID() {
        return dbID;
    }

    /**
     * Sets database ID.
     * @param dbID database ID
     */
    public void setDbID(String dbID) {
        this.dbID = dbID;
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
        return "Profile{" +
                "name='" + name + '\'' +
                ", dbID='" + dbID + '\'' +
                '}';
    }

    ////////////////////////// VOTING //////////////////////////

    /**
     * Initializes voting item amount.
     * @param amount voting item amount
     */
    public void initVoteSize(int amount) {
        votedItems = new ListItem[amount];
    }

    /**
     * Adds new itemIndex to given index.
     * @param index amount of points index
     * @param votedItem voted item
     */
    public void addVoteItem(int index, ListItem votedItem) {
        votedItems[index] = votedItem;
    }

    /**
     * Removes voted item index from given position.
     * @param index amount of points index
     */
    public void removeVoteItem(int index) {
        votedItems[index] = null;
    }

    /**
     * Returns voted item index from given position.
     * @param index vote points index
     * @return voted item at index
     */
    public ListItem getVoteItem(int index) {
        return votedItems[index];
    }

    /**
     * Returns the amount of vote points this profile gave to provided List Item.
     * @param item item to check
     * @return amount of vote points
     */
    public int votePointsOfItem(ListItem item) {
        for (int i = 0; i < votedItems.length; i++) {
            ListItem voteItem = votedItems[i];
            if (voteItem.equalsTo(item)) {
                return i + 1;
            }
        }

        return 0;
    }

    ////////////////////////// PARCELABLE //////////////////////////

    /**
     * Describes contents.
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
        dest.writeTypedArray(votedItems, 0);
    }

    /**
     * Creates profile from parcel info.
     * @param in given parcel
     */
    public Profile(Parcel in) {
        dbID = in.readString();
        name = in.readString();
        votedItems = in.createTypedArray(ListItem.CREATOR);
    }

    /**
     * Creator object for profile.
     */
    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        /**
         * Creates from parcel.
         * @param in given parcel
         * @return profile
         */
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        /**
         * Creates new profile array.
         * @param size size of array
         * @return profile array
         */
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}
