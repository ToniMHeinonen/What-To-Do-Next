package io.github.tonimheinonen.whattodonext.voteactivity;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.database.GlobalSettings;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.database.OnlineProfile;
import io.github.tonimheinonen.whattodonext.database.Profile;
import io.github.tonimheinonen.whattodonext.database.VoteRoom;
import io.github.tonimheinonen.whattodonext.database.VoteSettings;

public class VoteMaster implements Parcelable {

    // Static
    public final static String VOTE_MASTER = "voteMaster";

    // Common
    private GlobalSettings globalSettings;
    private VoteSettings voteSettings;
    private ListOfItems selectedList;

    // Online
    private boolean online;
    private boolean reconnect;
    private OnlineProfile onlineProfile;
    private VoteRoom voteRoom;

    // Offline
    private int topAmount = -1;
    private ArrayList<Profile> selectedProfiles;

    public VoteMaster() {}

    public void setupCommon(GlobalSettings globalSettings, VoteSettings voteSettings, ListOfItems selectedList) {
        this.globalSettings = globalSettings;
        this.voteSettings = voteSettings;
        this.selectedList = selectedList;
    }

    public void setupOnline(boolean reconnect, OnlineProfile onlineProfile, VoteRoom voteRoom) {
        this.online = true;
        this.reconnect = reconnect;
        this.onlineProfile = onlineProfile;
        this.voteRoom = voteRoom;
    }

    public void setupOffline(int topAmount, ArrayList<Profile> selectedProfiles) {
        this.topAmount = topAmount;
        this.selectedProfiles = selectedProfiles;
    }

    //region GetSet
    public GlobalSettings getGlobalSettings() {
        return globalSettings;
    }

    public void setGlobalSettings(GlobalSettings globalSettings) {
        this.globalSettings = globalSettings;
    }

    public VoteSettings getVoteSettings() {
        return voteSettings;
    }

    public void setVoteSettings(VoteSettings voteSettings) {
        this.voteSettings = voteSettings;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public OnlineProfile getOnlineProfile() {
        return onlineProfile;
    }

    public void setOnlineProfile(OnlineProfile onlineProfile) {
        this.onlineProfile = onlineProfile;
    }

    public VoteRoom getVoteRoom() {
        return voteRoom;
    }

    public void setVoteRoom(VoteRoom voteRoom) {
        this.voteRoom = voteRoom;
    }

    public int getTopAmount() {
        return topAmount;
    }

    public void setTopAmount(int topAmount) {
        this.topAmount = topAmount;
    }

    public ArrayList<Profile> getSelectedProfiles() {
        return selectedProfiles;
    }

    public void setSelectedProfiles(ArrayList<Profile> selectedProfiles) {
        this.selectedProfiles = selectedProfiles;
    }

    public ListOfItems getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(ListOfItems selectedList) {
        this.selectedList = selectedList;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    //endregion

    @Override
    public String toString() {
        return "VoteIntentHolder{" +
                "globalSettings=" + globalSettings +
                ", voteSettings=" + voteSettings +
                ", online=" + online +
                ", reconnect=" + reconnect +
                ", onlineProfile=" + onlineProfile +
                ", voteRoom=" + voteRoom +
                ", topAmount=" + topAmount +
                ", selectedProfiles=" + selectedProfiles +
                ", selectedList=" + selectedList +
                '}';
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
        dest.writeParcelable(globalSettings, 0);
        dest.writeParcelable(voteSettings, 0);
        dest.writeInt(online ? 1 : 0);
        dest.writeInt(reconnect ? 1 : 0);
        dest.writeParcelable(onlineProfile, 0);
        dest.writeParcelable(voteRoom, 0);
        dest.writeInt(topAmount);
        dest.writeList(selectedProfiles);
        dest.writeParcelable(selectedList, 0);
    }

    /**
     * Creates list from parcel info.
     * @param in given parcel
     */
    public VoteMaster(Parcel in) {
        globalSettings = in.readParcelable(GlobalSettings.class.getClassLoader());
        voteSettings = in.readParcelable(VoteSettings.class.getClassLoader());
        online = in.readInt() == 1;
        reconnect = in.readInt() == 1;
        onlineProfile = in.readParcelable(OnlineProfile.class.getClassLoader());
        voteRoom = in.readParcelable(VoteRoom.class.getClassLoader());
        topAmount = in.readInt();
        selectedProfiles = in.readArrayList(Profile.class.getClassLoader());
        selectedList = in.readParcelable(ListOfItems.class.getClassLoader());
    }

    /**
     * Creator object for list.
     */
    public static final Parcelable.Creator<VoteMaster> CREATOR = new Parcelable.Creator<VoteMaster>() {
        /**
         * Creates from parcel.
         * @param in given parcel
         * @return list
         */
        public VoteMaster createFromParcel(Parcel in) {
            return new VoteMaster(in);
        }

        /**
         * Creates new list array.
         * @param size size of array
         * @return list array
         */
        public VoteMaster[] newArray(int size) {
            return new VoteMaster[size];
        }
    };
}
