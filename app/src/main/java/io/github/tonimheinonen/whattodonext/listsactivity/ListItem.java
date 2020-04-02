package io.github.tonimheinonen.whattodonext.listsactivity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

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

    public ListItem() {
        // Default constructor required for calls to DataSnapshot.getValue(ListItem.class)
    }

    public ListItem(String name, int bonus, int peril) {
        this.name = name;
        this.bonus = bonus;
        this.peril = peril;
        this.total = bonus + peril;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotal() {
        return total;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
        this.total = this.bonus + this.peril;
    }

    public int getPeril() {
        return peril;
    }

    public void setPeril(int peril) {
        this.peril = peril;
        this.total = this.bonus + this.peril;
    }

    public boolean isFallen() {
        return fallen;
    }

    public void setFallen(boolean fallen) {
        this.fallen = fallen;
    }

    public String getListID() {
        return listID;
    }

    public void setListID(String listID) {
        this.listID = listID;
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
        result.put("listID", listID);
        result.put("name", name);
        result.put("bonus", bonus);
        result.put("peril", peril);
        result.put("fallen", fallen);

        return result;
    }

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

    public void addVotePoints(int amount) {
        votePoints += amount;
    }

    public int getVotePoints() {
        return votePoints;
    }

    public void clearVotePoints() {
        votePoints = 0;
    }

////////////////////////// PARCELABLE //////////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dbID);
        dest.writeString(listID);
        dest.writeString(name);
        dest.writeInt(bonus);
        dest.writeInt(peril);
        dest.writeInt(total);
    }

    public ListItem(Parcel in) {
        dbID = in.readString();
        listID = in.readString();
        name = in.readString();
        bonus = in.readInt();
        peril = in.readInt();
        total = in.readInt();
    }

    public static final Parcelable.Creator<ListItem> CREATOR = new Parcelable.Creator<ListItem>() {
        public ListItem createFromParcel(Parcel in) {
            return new ListItem(in);
        }

        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };
}
