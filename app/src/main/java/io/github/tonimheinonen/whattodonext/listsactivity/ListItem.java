package io.github.tonimheinonen.whattodonext.listsactivity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

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
public class ListItem implements Parcelable {

    private String listID;
    private String name;
    private int bonus;
    private int peril;
    private boolean fallen;

    @Exclude
    private String dbID;
    @Exclude
    private int total;

    @Exclude
    private int votePoints;

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

        return result;
    }

    /**
     * Prints this instead of pointer.
     * @return string to print
     */
    @Override
    public String toString() {
        return "ListItem{" +
                "listID='" + listID + '\'' +
                ", name='" + name + '\'' +
                ", bonus=" + bonus +
                ", peril=" + peril +
                ", dbID='" + dbID + '\'' +
                ", total=" + total +
                '}';
    }

    ////////////////////////// VOTING //////////////////////////

    /**
     * Modifies given vote points to item.
     * @param points points to be added
     * @return -1 if added successfully, else return previous points
     */
    public int modifyVotePoint(int points) {
        int previous = votePoints;

        // If current voter has not yet voted this
        if (previous == 0) {
            // If all points has not been given yet, add points
            if (points != -1)
                votePoints = points;

            return -1;
        }

        // Else reset value and return previously voted amount back
        votePoints = 0;
        return previous;
    }

    /**
     * Adds vote points.
     * @param amount vote points
     */
    public void addVotePoints(int amount) {
        votePoints += amount;
    }

    /**
     * Returns vote points.
     * @return vote points
     */
    public int getVotePoints() {
        return votePoints;
    }

    /**
     * Clears vote points.
     */
    public void clearVotePoints() {
        votePoints = 0;
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
}
