package io.github.tonimheinonen.whattodonext.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class GlobalSettings implements DatabaseValue, Parcelable {

    private int resultStyle = 0;

    @Exclude
    private String dbID;

    /**
     * Default constructor.
     */
    public GlobalSettings() {
        // Default constructor required for calls to DataSnapshot.getValue(GlobalSettings.class)
    }

    //region GetSet


    public String getDbID() {
        return dbID;
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;
    }

    public int getResultStyle() {
        return resultStyle;
    }

    public void setResultStyle(int resultStyle) {
        this.resultStyle = resultStyle;
    }

    //endregion

    /**
     * Maps values for database handling.
     * @return mapped values
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("resultStyle", resultStyle);

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
        dest.writeInt(resultStyle);
    }

    /**
     * Creates VoteSettings from parcel info.
     * @param in given parcel
     */
    public GlobalSettings(Parcel in) {
        dbID = in.readString();
        resultStyle = in.readInt();
    }

    /**
     * Creator object for VoteSettings.
     */
    public static final Creator<GlobalSettings> CREATOR = new Creator<GlobalSettings>() {
        /**
         * Creates from parcel.
         * @param in given parcel
         * @return profile
         */
        public GlobalSettings createFromParcel(Parcel in) {
            return new GlobalSettings(in);
        }

        /**
         * Creates new VoteSettings array.
         * @param size size of array
         * @return profile array
         */
        public GlobalSettings[] newArray(int size) {
            return new GlobalSettings[size];
        }
    };
}
