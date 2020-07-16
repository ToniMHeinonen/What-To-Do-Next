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
 * @version 1.3
 * @since 1.3
 */
@IgnoreExtraProperties
public class VoteRoom implements DatabaseValue, Parcelable {

    private String roomCode;
    private String listName;
    private String state;
    private int firstVoteSize, lastVoteSize, maxPerilPoints;
    private boolean ignoreUnselected, halveExtra, showExtra, showVotes;

    public static final String
            LOBBY = "lobby",
            VOTING_FIRST = "voting_first",
            RESULTS_FIRST = "results_first",
            VOTING_LAST = "voting_last",
            RESULTS_LAST = "results_last";

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
     * @param firstVoteSize size of the first vote
     * @param lastVoteSize size of the last vote
     * @param maxPerilPoints max peril points before item drops
     * @param ignoreUnselected whether to ignore unselected or not
     * @param halveExtra whether to halve extra or not
     * @param showExtra whether to show extra or not
     * @param showVotes whether to show votes or not
     */
    public VoteRoom(String roomCode, String listName, int firstVoteSize, int lastVoteSize,
                    int maxPerilPoints, boolean ignoreUnselected, boolean halveExtra, boolean showExtra,
                    boolean showVotes) {
        this.roomCode = roomCode;
        this.listName = listName;
        this.state = LOBBY;
        this.firstVoteSize = firstVoteSize;
        this.lastVoteSize = lastVoteSize;
        this.ignoreUnselected = ignoreUnselected;
        this.halveExtra = halveExtra;
        this.showExtra = showExtra;
        this.showVotes = showVotes;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getFirstVoteSize() {
        return firstVoteSize;
    }

    public void setFirstVoteSize(int firstVoteSize) {
        this.firstVoteSize = firstVoteSize;
    }

    public int getLastVoteSize() {
        return lastVoteSize;
    }

    public void setLastVoteSize(int lastVoteSize) {
        this.lastVoteSize = lastVoteSize;
    }

    public boolean isIgnoreUnselected() {
        return ignoreUnselected;
    }

    public void setIgnoreUnselected(boolean ignoreUnselected) {
        this.ignoreUnselected = ignoreUnselected;
    }

    public boolean isHalveExtra() {
        return halveExtra;
    }

    public void setHalveExtra(boolean halveExtra) {
        this.halveExtra = halveExtra;
    }

    public boolean isShowExtra() {
        return showExtra;
    }

    public void setShowExtra(boolean showExtra) {
        this.showExtra = showExtra;
    }

    public boolean isShowVotes() {
        return showVotes;
    }

    public void setShowVotes(boolean showVotes) {
        this.showVotes = showVotes;
    }

    public int getMaxPerilPoints() {
        return maxPerilPoints;
    }

    public void setMaxPerilPoints(int maxPerilPoints) {
        this.maxPerilPoints = maxPerilPoints;
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
        result.put("firstVoteSize", firstVoteSize);
        result.put("lastVoteSize", lastVoteSize);
        result.put("maxPerilPoints", maxPerilPoints);
        result.put("ignoreUnselected", ignoreUnselected);
        result.put("halveExtra", halveExtra);
        result.put("showExtra", showExtra);
        result.put("showVotes", showVotes);

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
        dest.writeString(state);
        dest.writeString(roomCode);
        dest.writeString(listName);
        dest.writeInt(firstVoteSize);
        dest.writeInt(lastVoteSize);
        dest.writeInt(maxPerilPoints);
        dest.writeInt(ignoreUnselected ? 1 : 0);
        dest.writeInt(halveExtra ? 1 : 0);
        dest.writeInt(showExtra ? 1 : 0);
        dest.writeInt(showVotes ? 1 : 0);
    }

    /**
     * Creates profile from parcel info.
     * @param in given parcel
     */
    public VoteRoom(Parcel in) {
        dbID = in.readString();
        state = in.readString();
        roomCode = in.readString();
        listName = in.readString();
        firstVoteSize = in.readInt();
        lastVoteSize = in.readInt();
        maxPerilPoints = in.readInt();
        ignoreUnselected = in.readInt() == 1;
        halveExtra = in.readInt() == 1;
        showExtra = in.readInt() == 1;
        showVotes = in.readInt() == 1;
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
