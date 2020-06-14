package io.github.tonimheinonen.whattodonext.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a vote room.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
@IgnoreExtraProperties
public class VoteRoom implements DatabaseValue, Parcelable {

    private String roomCode;
    private String state;

    public static final String LOBBY = "lobby", VOTING = "voting", RESULTS = "results";

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
     * Initializes vote room with the given name.
     * @param roomCode code for the room
     */
    public VoteRoom(String roomCode) {
        this.roomCode = roomCode;
        this.state = LOBBY;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Maps values for database handling.
     * @return mapped values
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("roomCode", roomCode);
        result.put("state", state);

        return result;
    }

    @Override
    public String toString() {
        return "VoteRoom{" +
                "roomCode='" + roomCode + '\'' +
                ", state='" + state + '\'' +
                ", dbID='" + dbID + '\'' +
                ", list=" + list +
                '}';
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
        dest.writeString(roomCode);
        dest.writeParcelable(list, flags);
    }

    /**
     * Creates profile from parcel info.
     * @param in given parcel
     */
    public VoteRoom(Parcel in) {
        dbID = in.readString();
        roomCode = in.readString();
        list = in.readParcelable(ListOfItems.class.getClassLoader());
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
}
