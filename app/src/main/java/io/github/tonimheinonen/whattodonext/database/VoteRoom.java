package io.github.tonimheinonen.whattodonext.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import io.github.tonimheinonen.whattodonext.BuildConfig;

/**
 * Represents a vote room.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.3
 * @since 1.3
 */
@IgnoreExtraProperties
public class VoteRoom implements DatabaseValue, Parcelable {

    private String roomCode;
    private String listName;
    private int state;
    private int versionCode;

    public static final int
        LOBBY = 0,
        VOTING_FIRST = 1,
        WAITING_FIRST = 2,
        RESULTS_FIRST = 3,
        VOTING_LAST = 4,
        WAITING_LAST = 5,
        RESULTS_LAST = 6;

    @Exclude
    private String dbID;

    @Exclude
    private ListOfItems list;

    /**
     * Default constructor.
     */
    public VoteRoom() {
        // Default constructor required for calls to DataSnapshot.getValue(ListOfItems.class)
    }

    /**
     * Initializes vote room with necessary values.
     * @param roomCode room code
     * @param listName name of the list
     */
    public VoteRoom(String roomCode, String listName) {
        this.roomCode = roomCode;
        this.listName = listName;
        this.state = LOBBY;

        versionCode = BuildConfig.VERSION_CODE;
    }

    /**
     * Returns the room code.
     * @return room code
     */
    public String getRoomCode() {
        return roomCode;
    }

    /**
     * Sets the room code.
     * @param roomCode room code
     */
    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    /**
     * Returns the name of the voted list.
     * @return name of the voted list
     */
    public String getListName() {
        return listName;
    }

    /**
     * Sets the name of the voted list.
     * @param listName name of the voted list
     */
    public void setListName(String listName) {
        this.listName = listName;
    }

    /**
     * Returns the list which holds the items.
     * @return list holding the items
     */
    public ListOfItems getList() {
        return list;
    }

    /**
     * Sets the list which holds the items.
     * @param list list holding the items
     */
    public void setList(ListOfItems list) {
        this.list = list;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getVersionCode() {
        return versionCode;
    }

    /**
     * Maps values for database handling.
     * @return mapped values
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("roomCode", roomCode);
        result.put("listName", listName);
        result.put("state", state);
        result.put("versionCode", versionCode);

        return result;
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
        dest.writeInt(state);
        dest.writeString(roomCode);
        dest.writeString(listName);
        dest.writeInt(versionCode);
    }

    /**
     * Creates profile from parcel info.
     * @param in given parcel
     */
    public VoteRoom(Parcel in) {
        dbID = in.readString();
        state = in.readInt();
        roomCode = in.readString();
        listName = in.readString();
        versionCode = in.readInt();
    }

    /**
     * Creator object for profile.
     */
    public static final Creator<VoteRoom> CREATOR = new Creator<VoteRoom>() {
        /**
         * Creates from parcel.
         * @param in given parcel
         * @return profile
         */
        public VoteRoom createFromParcel(Parcel in) {
            return new VoteRoom(in);
        }

        /**
         * Creates new profile array.
         * @param size size of array
         * @return profile array
         */
        public VoteRoom[] newArray(int size) {
            return new VoteRoom[size];
        }
    };

    @Override
    public String toString() {
        return "VoteRoom{" +
                "roomCode='" + roomCode + '\'' +
                ", listName='" + listName + '\'' +
                ", state=" + state +
                ", versionCode=" + versionCode +
                ", dbID='" + dbID + '\'' +
                ", list=" + list +
                '}';
    }
}
