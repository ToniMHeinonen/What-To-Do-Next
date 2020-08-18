package io.github.tonimheinonen.whattodonext.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a online profile.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
@IgnoreExtraProperties
public class OnlineProfile implements DatabaseValue, Parcelable {

    private String nickName;
    private boolean isHost;
    private boolean ready;

    @Exclude
    private String dbID;

    /**
     * Default constructor.
     */
    public OnlineProfile() {
        // Default constructor required for calls to DataSnapshot.getValue(OnlineProfile.class)
    }

    public OnlineProfile(String nickName, boolean isHost) {
        this.nickName = nickName;
        this.isHost = isHost;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public String getDbID() {
        return dbID;
    }

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
        result.put("nickName", nickName);
        result.put("isHost", isHost);
        result.put("ready", ready);

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
        dest.writeString(nickName);
        dest.writeInt(isHost ? 1 : 0);
    }

    /**
     * Creates profile from parcel info.
     * @param in given parcel
     */
    public OnlineProfile(Parcel in) {
        dbID = in.readString();
        nickName = in.readString();
        isHost = in.readInt() == 1;
    }

    /**
     * Creator object for profile.
     */
    public static final Creator<OnlineProfile> CREATOR = new Creator<OnlineProfile>() {
        /**
         * Creates from parcel.
         * @param in given parcel
         * @return profile
         */
        public OnlineProfile createFromParcel(Parcel in) {
            return new OnlineProfile(in);
        }

        /**
         * Creates new profile array.
         * @param size size of array
         * @return profile array
         */
        public OnlineProfile[] newArray(int size) {
            return new OnlineProfile[size];
        }
    };
}
