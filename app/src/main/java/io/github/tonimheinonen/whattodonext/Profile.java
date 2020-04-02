package io.github.tonimheinonen.whattodonext;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;

@IgnoreExtraProperties
public class Profile implements DatabaseValue, Parcelable {

    private String name;

    @Exclude
    private String dbID;

    @Exclude
    private int[] votedItems;

    public Profile() {
        // Default constructor required for calls to DataSnapshot.getValue(ListOfItems.class)
    }

    public Profile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "Profile{" +
                "name='" + name + '\'' +
                ", dbID='" + dbID + '\'' +
                '}';
    }

    ////////////////////////// VOTING //////////////////////////

    public void initVoteSize(int amount) {
        votedItems = new int[amount];
    }

    public void addVoteItem(int index, int itemIndex) {
        votedItems[index] = itemIndex;
    }

    public void removeVoteItem(int index) {
        votedItems[index] = -1;
    }

    public int getVoteItem(int index) {
        return votedItems[index];
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
        dest.writeIntArray(votedItems);
    }

    public Profile(Parcel in) {
        dbID = in.readString();
        name = in.readString();
        votedItems = in.createIntArray();
    }

    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}
