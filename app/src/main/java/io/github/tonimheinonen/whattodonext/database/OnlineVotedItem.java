package io.github.tonimheinonen.whattodonext.database;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a voted item when voting online.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.3
 * @since 1.3
 */
@IgnoreExtraProperties
public class OnlineVotedItem implements DatabaseValue {

    private String userID;
    private String itemID;
    private int votePoints;

    /**
     * Default constructor.
     */
    public OnlineVotedItem() {
        // Default constructor required for calls to DataSnapshot.getValue(OnlineVotedItem.class)
    }

    public OnlineVotedItem(String userID, String itemID, int votePoints) {
        this.userID = userID;
        this.itemID = itemID;
        this.votePoints = votePoints;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    /**
     * Returns vote points.
     *
     * Used in VoteItemAdapter.
     * @return vote points
     */
    public int getVotePoints() {
        return votePoints;
    }

    /**
     * Sets vote points.
     *
     * Used in VoteTopActivity.
     * @param points
     */
    public void setVotePoints(int points) {
        votePoints = points;
    }

    /**
     * Maps values for database handling.
     * @return mapped values
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userID", userID);
        result.put("itemID", itemID);
        result.put("votePoints", votePoints);

        return result;
    }

    @Override
    public String toString() {
        return "OnlineVotedItem{" +
                "userID='" + userID + '\'' +
                ", itemID='" + itemID + '\'' +
                ", votePoints=" + votePoints +
                '}';
    }
}
