package io.github.tonimheinonen.whattodonext;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Profile implements DatabaseValue, Parcelable {

    private String name;

    @Exclude
    private String dbID;

    @Exclude
    private int[] votedItems;

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
        votedItems = new int[amount];
    }

    /**
     * Adds new itemIndex to given index.
     * @param index amount of points index
     * @param itemIndex position of the item
     */
    public void addVoteItem(int index, int itemIndex) {
        votedItems[index] = itemIndex;
    }

    /**
     * Removes voted item index from given position.
     * @param index amount of points index
     */
    public void removeVoteItem(int index) {
        votedItems[index] = -1;
    }

    /**
     * Returns voted item index from given position.
     * @param index vote points index
     * @return
     */
    public int getVoteItem(int index) {
        return votedItems[index];
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
        dest.writeIntArray(votedItems);
    }

    /**
     * Creates profile from parcel info.
     * @param in given parcel
     */
    public Profile(Parcel in) {
        dbID = in.readString();
        name = in.readString();
        votedItems = in.createIntArray();
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
