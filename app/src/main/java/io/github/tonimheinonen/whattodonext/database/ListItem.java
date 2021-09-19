package io.github.tonimheinonen.whattodonext.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an list item activity.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
@IgnoreExtraProperties
public class ListItem implements DatabaseValue, Parcelable, Comparable<ListItem> {

    private String listID;
    private String name;
    private int bonus;
    private int peril;
    private boolean fallen;
    private boolean inOnlineLastVote;

    @Exclude
    private String dbID;
    @Exclude
    private int total;

    // These values do not need to be reset since they are always recreated
    @Exclude
    private int votePoints;
    @Exclude
    private int voterAmount = 0;
    @Exclude
    private boolean selected;

    /**
     * Default constructor.
     */
    public ListItem() {
        // Default constructor required for calls to DataSnapshot.getValue(ListItem.class)
    }

    /**
     * Initializes necessary values.
     * @param name name of the item
     * @param bonus item bonus amount
     * @param peril item peril amount
     */
    public ListItem(String name, int bonus, int peril) {
        this.name = name;
        this.bonus = bonus;
        this.peril = peril;
        this.total = bonus + peril;
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
     * Returns total amount of points.
     * @return total points
     */
    public int getTotal() {
        return total;
    }

    /**
     * Sets total amount of points.
     *
     * Mainly used in VoteResultsActivity to halve points.
     * @param total total amount of extra points
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * Returns bonus points.
     * @return bonus points
     */
    public int getBonus() {
        return bonus;
    }

    /**
     * Sets bonus amount.
     * @param bonus bonus amount
     */
    public void setBonus(int bonus) {
        this.bonus = bonus;
        this.total = this.bonus + this.peril;
    }

    /**
     * Returns the amount of voters who voted this item.
     * @return the amount of voters who voted this item
     */
    public int getVoterAmount() {
        return voterAmount;
    }

    /**
     * Returns peril amount.
     * @return peril amount
     */
    public int getPeril() {
        return peril;
    }

    /**
     * Sets peril amount.
     * @param peril peril amount
     */
    public void setPeril(int peril) {
        this.peril = peril;
        this.total = this.bonus + this.peril;
    }

    /**
     * Returns fallen status.
     * @return fallen status
     */
    public boolean isFallen() {
        return fallen;
    }

    /**
     * Sets fallen status
     * @param fallen fallen status
     */
    public void setFallen(boolean fallen) {
        this.fallen = fallen;
    }

    /**
     * Returns list ID.
     * @return list ID
     */
    public String getListID() {
        return listID;
    }

    /**
     * Sets list ID.
     * @param listID list ID
     */
    public void setListID(String listID) {
        this.listID = listID;
    }

    /**
     * Returns database ID for item.
     * @return databse ID for item
     */
    public String getDbID() {
        return dbID;
    }

    /**
     * Sets database ID for item.
     * @param dbID database ID for item
     */
    public void setDbID(String dbID) {
        this.dbID = dbID;
    }

    public boolean isInOnlineLastVote() {
        return inOnlineLastVote;
    }

    public void setInOnlineLastVote(boolean inOnlineLastVote) {
        this.inOnlineLastVote = inOnlineLastVote;
    }

    public static void filterOutOnlineLastVote(ArrayList<ListItem> items) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (!items.get(i).isInOnlineLastVote())
                items.remove(i);
        }
    }

    /**
     * Maps values for database handling.
     * @return mapped values
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("listID", listID);
        result.put("name", name);
        result.put("bonus", bonus);
        result.put("peril", peril);
        result.put("fallen", fallen);
        result.put("inOnlineLastVote", inOnlineLastVote);

        return result;
    }

    /**
     * Compares if items are the same.
     * @param otherItem other item
     * @return true if items are same
     */
    public boolean equalsTo(ListItem otherItem) {
        if (dbID.equals(otherItem.getDbID()))
            return true;

        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ListItem) {
            String otherDbID = ((ListItem) object).getDbID();

            // If item has not been added to database, return false
            if (dbID == null || otherDbID == null)
                return false;

            return dbID.equals(otherDbID);
        }

        return false;
    }

    /**
     * Prints this instead of pointer.
     *
     * @return string to print
     */
    @Override
    public String toString() {
        return "ListItem{" +
                "listID='" + listID + '\'' +
                ", name='" + name + '\'' +
                ", bonus=" + bonus +
                ", peril=" + peril +
                ", fallen=" + fallen +
                ", inOnlineLastVote=" + inOnlineLastVote +
                ", dbID='" + dbID + '\'' +
                ", total=" + total +
                ", votePoints=" + votePoints +
                ", voterAmount=" + voterAmount +
                ", selected=" + selected +
                '}';
    }

    ////////////////////////// VOTING //////////////////////////

    /**
     * Adds vote points.
     *
     * Used when calculating overall vote points in VoteResultsActivity.
     * @param amount vote points
     */
    public void addVotePoints(int amount) {
        votePoints += amount;
    }

    /**
     * Add + 1 to voterAmount.
     *
     * Used when calculating what items profile has voted
     * in VoteResultsActivity.
     */
    public void addVoterAmount() {
        voterAmount ++;
    }

    /**
     * Returns vote points and resets value.
     *
     * Used in VoteTopActivity.
     * @return vote points
     */
    public int retrieveVotePoints() {
        int points = votePoints;
        votePoints = 0;

        return points;
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
     * Returns true is item is currently selected.
     *
     * Mainly used in VoteResultsActivity.
     * @return true if selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets if item is currently selected.
     *
     * Mainly used in VoteResultsActivity.
     * @param selected true if selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
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
        dest.writeString(listID);
        dest.writeString(name);
        dest.writeInt(bonus);
        dest.writeInt(peril);
        dest.writeInt(total);
    }

    /**
     * Creates item from parcel info.
     * @param in given parcel
     */
    public ListItem(Parcel in) {
        dbID = in.readString();
        listID = in.readString();
        name = in.readString();
        bonus = in.readInt();
        peril = in.readInt();
        total = in.readInt();
    }

    /**
     * Creator object for item.
     */
    public static final Parcelable.Creator<ListItem> CREATOR = new Parcelable.Creator<ListItem>() {
        /**
         * Creates from parcel.
         * @param in given parcel
         * @return item
         */
        public ListItem createFromParcel(Parcel in) {
            return new ListItem(in);
        }

        /**
         * Creates new item array.
         * @param size size of array
         * @return item array
         */
        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };

    /**
     * Compares 2 list items based on their values.
     *
     * 1. Check which one has the most vote points, including bonus points.
     * 2. If equal, check which one has the most number of players voted.
     * 3. If equal, check which one has the most vote points, excluding bonus points.
     * @param other other list item
     * @return -1 if less than, 0 if equal, 1 if greater than
     */
    @Override
    public int compareTo(ListItem other) {
        if (this.votePoints == other.getVotePoints()) {
            if (this.voterAmount == other.getVoterAmount()) {
                return Integer.compare(this.votePoints - this.total, other.getVotePoints() - other.getTotal());
            } else {
                return Integer.compare(this.voterAmount, other.getVoterAmount());
            }
        } else {
            return Integer.compare(this.votePoints, other.getVotePoints());
        }
    }
}
