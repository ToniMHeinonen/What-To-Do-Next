package io.github.tonimheinonen.whattodonext.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class VoteSettings implements DatabaseValue, Parcelable {

    public static final int FIRST_VOTE_DEFAULT_POINTS = 7;
    public static final int LAST_VOTE_DEFAULT_POINTS = 5;
    public static final int MAX_PERIL_DEFAULT_POINTS = 5;

    private String listID;
    private int firstVote = 7;
    private int lastVote = 5;
    private int maxPeril = 5;
    private boolean ignoreUnselected = true;
    private boolean halveExtra = true;
    private boolean showExtra = true;
    private boolean showVoted = true;

    @Exclude
    private String dbID;

    /**
     * Default constructor.
     */
    public VoteSettings() {
        // Default constructor required for calls to DataSnapshot.getValue(VoteSettings.class)
    }

    //region GetSet


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

    public int getFirstVote() {
        return firstVote;
    }

    public void setFirstVote(int firstVote) {
        this.firstVote = firstVote;
    }

    public int getLastVote() {
        return lastVote;
    }

    public void setLastVote(int lastVote) {
        this.lastVote = lastVote;
    }

    public int getMaxPeril() {
        return maxPeril;
    }

    public void setMaxPeril(int maxPeril) {
        this.maxPeril = maxPeril;
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

    public boolean isShowVoted() {
        return showVoted;
    }

    public void setShowVoted(boolean showVoted) {
        this.showVoted = showVoted;
    }

    //endregion

    /**
     * Maps values for database handling.
     * @return mapped values
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("listID", listID);
        result.put("firstVote", firstVote);
        result.put("lastVote", lastVote);
        result.put("maxPeril", maxPeril);
        result.put("ignoreUnselected", ignoreUnselected);
        result.put("halveExtra", halveExtra);
        result.put("showExtra", showExtra);
        result.put("showVoted", showVoted);

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
        dest.writeString(listID);
        dest.writeInt(firstVote);
        dest.writeInt(lastVote);
        dest.writeInt(maxPeril);
        dest.writeInt(ignoreUnselected ? 1 : 0);
        dest.writeInt(halveExtra ? 1 : 0);
        dest.writeInt(showExtra ? 1 : 0);
        dest.writeInt(showVoted ? 1 : 0);
    }

    /**
     * Creates VoteSettings from parcel info.
     * @param in given parcel
     */
    public VoteSettings(Parcel in) {
        dbID = in.readString();
        listID = in.readString();
        firstVote = in.readInt();
        lastVote = in.readInt();
        maxPeril = in.readInt();
        ignoreUnselected = in.readInt() == 1;
        halveExtra = in.readInt() == 1;
        showExtra = in.readInt() == 1;
        showVoted = in.readInt() == 1;
    }

    /**
     * Creator object for VoteSettings.
     */
    public static final Creator<VoteSettings> CREATOR = new Creator<VoteSettings>() {
        /**
         * Creates from parcel.
         * @param in given parcel
         * @return profile
         */
        public VoteSettings createFromParcel(Parcel in) {
            return new VoteSettings(in);
        }

        /**
         * Creates new VoteSettings array.
         * @param size size of array
         * @return profile array
         */
        public VoteSettings[] newArray(int size) {
            return new VoteSettings[size];
        }
    };
}
