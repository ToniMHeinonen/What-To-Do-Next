package io.github.tonimheinonen.whattodonext.voteactivity;

public abstract class VoteIntents {

    // Both
    public final static String TOP_AMOUNT = "topAmount";
    public final static String LIST = "selectedList";
    public final static String PROFILES = "selectedProfiles";
    public final static String IS_ONLINE = "isOnline";
    public final static String ITEMS_TO_RESET = "itemsToReset";

    // Online
    public final static String ROOM = "voteRoom";
    public final static String ONLINE_PROFILE = "onlineProfile";
    public final static String RECONNECT = "reconnect";

    // Offline
    public final static String SETTINGS = "voteSettings";
}
