package io.github.tonimheinonen.whattodonext.database;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.tonimheinonen.whattodonext.MainActivity;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.Debug;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

/**
 * Handles controlling of Firebase database.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0.2
 * @since 1.0
 */
public abstract class DatabaseHandler {

    private static FirebaseUser user;
    private static DatabaseReference dbLists;
    private static DatabaseReference dbItems;
    private static DatabaseReference dbProfiles;
    private static DatabaseReference dbSavedResults;
    private static DatabaseReference dbResultItems;
    private static DatabaseReference dbVoteSettings;
    private static DatabaseReference dbGlobalSettings;

    // Online voting
    private static DatabaseReference dbVoteRooms;
    private static final String ONLINE_PROFILES = "profiles";
    private static final String ONLINE_DISCONNECTED_PROFILES = "disconnected_profiles";
    private static final String ONLINE_ITEMS = "items";
    private static final String ONLINE_VOTED_ITEMS_FIRST = "voted_items_first";
    private static final String ONLINE_VOTED_ITEMS_LAST = "voted_items_last";
    private static final String ONLINE_STATE = "state";
    private static final String ONLINE_TIMESTAMP = "timestamp";
    private static final String ONLINE_SETTINGS = "vote_settings";
    private static final String ONLINE_LIST = "vote_list";

    private final static String DEFAULT_VOTE_SETTINGS = "default";

    private static final int MAX_SAVED_RESULTS = 7;
    // Note: If you alter this, remember to change R.string.start_vote_expiration_note
    private static final int VOTE_ROOM_EXPIRE_TIME = 4; // Hours

    // listen for vote room removal if host disconnects from the vote room
    public static ChildEventListener voteRoomRemovalListener;
    // Listen for if user leaves the room
    public static DatabaseReference userLeftRoomRef;
    public static ChildEventListener userLeftRoomListener;

    /**
     * Initializes necessary user database values.
     */
    public static void initializeUserDatabase() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Init database paths and keep them synced when coming from offline to online
        dbLists = FirebaseDatabase.getInstance().getReference().child("users").
                child(user.getUid()).child("lists");
        dbLists.keepSynced(true);
        dbItems = FirebaseDatabase.getInstance().getReference().child("users").
                child(user.getUid()).child("items");
        dbItems.keepSynced(true);
        dbProfiles = FirebaseDatabase.getInstance().getReference().child("users").
                child(user.getUid()).child("profiles");
        dbProfiles.keepSynced(true);
        dbSavedResults = FirebaseDatabase.getInstance().getReference().child("users").
                child(user.getUid()).child("saved_results");
        dbSavedResults.keepSynced(true);
        dbResultItems = FirebaseDatabase.getInstance().getReference().child("users").
                child(user.getUid()).child("result_items");
        dbResultItems.keepSynced(true);
        dbVoteSettings = FirebaseDatabase.getInstance().getReference().child("users").
                child(user.getUid()).child("vote_settings");
        dbVoteSettings.keepSynced(true);
        dbGlobalSettings = FirebaseDatabase.getInstance().getReference().child("users").
                child(user.getUid()).child("global_settings");
        dbGlobalSettings.keepSynced(true);
        // If you add something in here, remember to add it to the logOutOfUser()
    }

    public static void logOutOfUser() {
        user = null;
        dbLists = null;
        dbItems = null;
        dbProfiles = null;
        dbSavedResults = null;
        dbResultItems = null;
        dbVoteSettings = null;
        dbGlobalSettings = null;
    }

    /**
     * Initializes necessary vote room database values.
     *
     * Called in InitializeActivity so vote rooms can work for unregistered
     * folk too and vote rooms do not change even if user is changed.
     */
    public static void initializeVoteRoomDatabase() {
        dbVoteRooms = FirebaseDatabase.getInstance().getReference().child("vote_rooms");
        dbVoteRooms.keepSynced(true);
    }

    public static void getUserDbID(UserDatabaseIDListener listener) {
        if (Buddy.isRegistered) {
            listener.onUserIDRetrieved(user.getUid());
            return;
        }

        FirebaseInstallations.getInstance().getId().addOnSuccessListener(listener::onUserIDRetrieved);
    }

    //region Interfaces
    /////////////////////* LISTENER INTERFACES *////////////////////

    public interface UserDatabaseIDListener {
        /**
         * Gets user database id from database.
         * @param id loaded id from database
         */
        void onUserIDRetrieved(String id);
    }

    public interface ListsListener {
        /**
         * Gets loaded lists from database.
         * @param lists loaded lists from database
         */
        void onDataGetLists(ArrayList<ListOfItems> lists);
    }

    public interface ItemsListener {
        /**
         * Gets loaded items from database.
         * @param items loaded items from database
         */
        void onDataGetItems(ArrayList<ListItem> items);
    }

    public interface ProfilesListener {
        /**
         * Gets loaded profiles from database.
         * @param profiles loaded profiles from database
         */
        void onDataGetProfiles(ArrayList<Profile> profiles);
    }

    public interface SavedResultsListener {
        /**
         * Gets loaded results from database.
         * @param results loaded results from database
         */
        void onDataGetResults(ArrayList<SavedResult> results);
    }

    public interface ResultItemsListener {
        /**
         * Gets loaded result items from database.
         * @param resultItems loaded result items from database
         */
        void onDataGetResultItems(ArrayList<SavedResultItem> resultItems);
    }

    public interface VoteSettingsListener {
        /**
         * Gets loaded vote settings from database.
         * @param voteSettings loaded vote settings from database
         */
        void onDataGetVoteSettings(VoteSettings voteSettings);
    }

    public interface GlobalSettingsListener {
        /**
         * Gets loaded global settings from database.
         * @param settings loaded global settings from database
         */
        void onDataGetGlobalSettings(GlobalSettings settings);
    }

    /////////////////////* VOTE ROOM INTERFACES *////////////////////

    public interface VoteRoomAddListener {
        /**
         * Adds a vote room.
         *
         * @param added true if adding was successful
         */
        void onDataAddVoteRoom(boolean added);
    }

    public interface VoteRoomGetListener {
        /**
         * Gets vote room based on the room code.
         *
         * @param voteRoom retrieved vote room
         */
        void onDataGetVoteRoom(VoteRoom voteRoom);
    }

    public interface VoteRoomGetListListener {
        /**
         * Gets list from vote room.
         * @param listOfItems loaded list from database
         */
        void onDataGetLists(ListOfItems listOfItems);
    }

    public interface VoteRoomGetItemsListener {
        /**
         * Gets vote room items.
         *
         * @param items retrieved items
         */
        void onDataGetVoteRoomItems(ArrayList<ListItem> items);
    }

    public interface VoteRoomGetVotedItemsListener {
        /**
         * Gets vote room voted items.
         *
         * @param items retrieved items
         */
        void onDataGetVoteRoomVotedItems(ArrayList<OnlineVotedItem> items);
    }

    public interface VoteRoomGetOnlineProfilesListener {
        /**
         * Gets vote room online profiles
         *
         * @param onlineProfiles retrieved profiles
         */
        void onDataGetOnlineProfiles(ArrayList<OnlineProfile> onlineProfiles);
    }

    public interface DatabaseAddListener {
        /**
         * Listens when adding data is complete.
         */
        void onDataAddedComplete();
    }

    public interface DatabaseGetStringValueListener {
        /**
         * Gets a single string value from database.
         *
         * @param value retrieved value
         */
        void onDataGetString(String value);
    }

    public interface DatabaseGetIntValueListener {
        /**
         * Gets a single int value from database.
         *
         * @param value retrieved value
         */
        void onDataGetString(int value);
    }
    //endregion

    //region Listener control

    /**
     * Removes all online vote room listeners.
     */
    public static void removeOnlineListeners() {
        stopListeningForVoteRoomExpiration();
        stopListeningForUserLeavingRoom();
    }
    //endregion

    //region Lists
    /////////////////////* LISTS *////////////////////

    /**
     * Adds new list.
     * @param list list to add
     */
    public static void addList(ListOfItems list) {
        String key = dbLists.push().getKey();   // Add new key to lists
        GlobalPrefs.saveCurrentList(key);       // Save this list to be latest used

        list.setDbID(key);
        Map<String, Object> listValues = list.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, listValues);

        dbLists.updateChildren(childUpdates);
    }

    /**
     * Modifies list values.
     * @param list list to modify
     */
    public static void modifyList(ListOfItems list) {
        Map<String, Object> listValues = list.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(list.getDbID(), listValues);

        dbLists.updateChildren(childUpdates);
    }

    /**
     * Removes list and its items.
     * @param list list to remove
     */
    public static void removeList(final ListOfItems list) {
        dbLists.child(list.getDbID()).removeValue();

        // Remove all items from database connected to this list
        Query query = dbItems.orderByChild("listID").equalTo(list.getDbID());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> childRemoval = new HashMap<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    childRemoval.put(key, null);
                }

                // Remove all items with single call
                dbItems.updateChildren(childRemoval);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });

        // Remove list settings for this list
        getVoteSettings((settings) -> {
            if (settings != null)
                dbVoteSettings.child(settings.getDbID()).removeValue();
        }, list);
    }

    /**
     * Loads lists from database.
     * @param listener listener to send data to
     */
    public static void getLists(final ListsListener listener) {
        dbLists.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "onDataChange",
                        "Lists count: " + snapshot.getChildrenCount(), 1);

                ArrayList<ListOfItems> lists = new ArrayList<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    Debug.print("DatabaseHandler", "onDataChange",
                            "Snapshot: " + snapshot.toString(), 1);
                    ListOfItems list = dataSnapshot.getValue(ListOfItems.class);
                    list.setDbID(key);
                    lists.add(list);
                }

                listener.onDataGetLists(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }
    //endregion

    //region Items
    /////////////////////* ITEMS *////////////////////

    /**
     * Adds new item.
     * @param list list to add item to
     * @param item item to add
     */
    public static void addItem(ListOfItems list, ListItem item) {
        String key = dbItems.push().getKey();   // Add new key to path

        item.setDbID(key);
        item.setListID(list.getDbID());
        Map<String, Object> listValues = item.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, listValues);

        dbItems.updateChildren(childUpdates);
    }

    /**
     * Adds multiple new items to a list.
     * @param list list to add the items to
     * @param items items to add
     */
    public static void addMultipleItems(ListOfItems list, ArrayList<ListItem> items) {
        Map<String, Object> childUpdates = new HashMap<>();

        for (ListItem item : items) {
            String key = dbItems.push().getKey();   // Add new key to path

            item.setDbID(key);
            item.setListID(list.getDbID());
            Map<String, Object> listValues = item.toMap();

            childUpdates.put(key, listValues);
        }

        dbItems.updateChildren(childUpdates);
    }

    /**
     * Modifies item values.
     * @param item item to modify
     */
    public static void modifyItem(ListItem item) {
        Map<String, Object> listValues = item.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(item.getDbID(), listValues);

        dbItems.updateChildren(childUpdates);
    }

    /**
     * Removes item.
     * @param item item to remove
     */
    public static void removeItem(ListItem item) {
        Debug.print("DatabaseHandler", "removeItem",
                "Removed: " + item.getName(), 1);
        dbItems.child(item.getDbID()).removeValue();
    }

    /**
     * Loads items which are part of the given list from database.
     * @param listener listener to send data to
     * @param list list where items must belong to
     */
    public static void getItems(final ItemsListener listener, final ListOfItems list) {
        Query query = dbItems.orderByChild("listID").equalTo(list.getDbID());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "onDataChange",
                        "Items count: " + snapshot.getChildrenCount(), 1);

                ArrayList<ListItem> items = new ArrayList<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    ListItem item = dataSnapshot.getValue(ListItem.class);
                    Debug.print("DatabaseHandler", "onDataChange",
                            "Item name: " + item.getName(), 1);
                    item.setDbID(key);
                    items.add(item);
                }

                listener.onDataGetItems(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }
    //endregion

    //region Profiles
    /////////////////////* PROFILES *////////////////////

    /**
     * Adds new profile.
     * @param profile profile to add
     */
    public static void addProfile(Profile profile) {
        String key = dbProfiles.push().getKey();

        profile.setDbID(key);
        Map<String, Object> listValues = profile.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, listValues);

        dbProfiles.updateChildren(childUpdates);
    }

    /**
     * Removes profile.
     * @param profile profile to remove
     */
    public static void removeProfile(Profile profile) {
        dbProfiles.child(profile.getDbID()).removeValue();
    }

    /**
     * Loads all profiles from database.
     * @param listener listener to send data to
     */
    public static void getProfiles(final ProfilesListener listener) {
        dbProfiles.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "onDataChange",
                        "Profiles count: " + snapshot.getChildrenCount(), 1);

                ArrayList<Profile> profiles = new ArrayList<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    Debug.print("DatabaseHandler", "onDataChange",
                            "Snapshot: " + snapshot.toString(), 1);
                    Profile profile = dataSnapshot.getValue(Profile.class);
                    profile.setDbID(key);
                    profiles.add(profile);
                }

                listener.onDataGetProfiles(profiles);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }
    //endregion

    //region SavedResults
    /////////////////////* SAVED RESULTS *////////////////////

    /**
     * Adds new result.
     * @param result result to add
     */
    public static void addResult(final SavedResult result, final ArrayList<SavedResultItem> items,
                                    DatabaseAddListener onCompleteListener) {
        String key = dbSavedResults.push().getKey();   // Add new key to results

        result.setDbID(key);
        Map<String, Object> values = result.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        dbSavedResults.updateChildren(childUpdates)
                .addOnCompleteListener(complete -> addResultItems(result, items, onCompleteListener));
    }

    /**
     * Checks if there are more than maximum allowed amount of results.
     *
     * If there are more, removes the oldest result.
     * @param results saved results
     */
    private static void checkResultsAmount(ArrayList<SavedResult> results) {
        if (results.size() > MAX_SAVED_RESULTS) {
            removeResult(results.get(0));   // Remove oldest, which is in index 0
        }
    }

    /**
     * Loads results from database.
     * @param listener listener to send data to
     */
    public static void getResults(final SavedResultsListener listener) {
        dbSavedResults.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<SavedResult> results = new ArrayList<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    SavedResult result = dataSnapshot.getValue(SavedResult.class);
                    result.setDbID(key);
                    results.add(result);
                }

                listener.onDataGetResults(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    /**
     * Removes result and its items.
     * @param result result to remove
     */
    private static void removeResult(final SavedResult result) {
        dbSavedResults.child(result.getDbID()).removeValue();

        // Remove all result items from database connected to this result
        Query query = dbResultItems.orderByChild("resultID").equalTo(result.getDbID());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> childRemoval = new HashMap<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    childRemoval.put(key, null);
                }

                // Remove all items with single call
                dbResultItems.updateChildren(childRemoval);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }
    //endregion

    //region ResultItems
    /////////////////////* RESULT ITEMS *////////////////////

    /**
     * Adds result items from saved result to database.
     * @param result result which holds result items
     */
    private static void addResultItems(SavedResult result, ArrayList<SavedResultItem> items,
                                      DatabaseAddListener onCompleteListener) {
        Map<String, Object> childUpdates = new HashMap<>();

        for (SavedResultItem item : items) {
            String key = dbResultItems.push().getKey();   // Add new key to path

            // Set result db id for item
            item.setDbID(key);
            item.setResultID(result.getDbID());
            Map<String, Object> listValues = item.toMap();

            childUpdates.put(key, listValues);
        }

        dbResultItems.updateChildren(childUpdates)
                .addOnCompleteListener(complete -> {
                    getResults(DatabaseHandler::checkResultsAmount);
                    onCompleteListener.onDataAddedComplete();
                });
    }

    /**
     * Loads result items which are part of the given result from database.
     * @param listener listener to send data to
     * @param result result where items must belong to
     */
    public static void getResultItems(final ResultItemsListener listener, final SavedResult result) {
        Query query = dbResultItems.orderByChild("resultID").equalTo(result.getDbID());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<SavedResultItem> items = new ArrayList<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    SavedResultItem item = dataSnapshot.getValue(SavedResultItem.class);
                    item.setDbID(key);
                    items.add(item);
                }

                listener.onDataGetResultItems(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }
    //endregion

    //region VoteRooms
    /////////////////////* VOTE ROOMS *////////////////////

    /**
     * Adds vote room if room does not exist yet.
     * @param listener listens for vote room add completion
     * @param voteRoom vote room to add
     */
    public static void addVoteRoom(final VoteRoomAddListener listener, final VoteRoom voteRoom) {
        // Check if room code is already taken
        getVoteRoom((room) -> {
            // If room code is not taken, add new room
            if (room == null) {
                String key = dbVoteRooms.push().getKey();   // Add new key to vote rooms

                voteRoom.setDbID(key);
                Map<String, Object> values = voteRoom.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(key, values);

                // Add new vote room
                dbVoteRooms.updateChildren(childUpdates)
                        // Listen for if someone else added a voteroom
                        // with the same code at the same time
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                checkDuplicateVoteRooms((duplicate) -> {
                                    // If there are duplicate room codes, return false
                                    if (duplicate) {
                                        listener.onDataAddVoteRoom(false);
                                        // Remove the vote room
                                        removeVoteRoom(voteRoom);
                                    } else {
                                        // Else there is only this newly created room, return true
                                        listener.onDataAddVoteRoom(true);
                                        // Set timestamp to the voteroom
                                        dbVoteRooms.child(key).child(ONLINE_TIMESTAMP).setValue(ServerValue.TIMESTAMP);
                                    }

                                }, voteRoom.getRoomCode());
                            }
                        });
            } else {
                // Room code is already taken, return false
                listener.onDataAddVoteRoom(false);
            }
        }, voteRoom.getRoomCode());
    }

    /**
     * Retrieves a vote room with the provided room code if it exists.
     * @param listener listener for retrieving the vote room, null if room does not exist
     * @param roomCode code for the room to retrieve
     */
    public static void getVoteRoom(VoteRoomGetListener listener, String roomCode) {
        Query query = dbVoteRooms.orderByChild("roomCode").equalTo(roomCode);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "getVoteRoom",
                        "rooms: " + snapshot.getChildrenCount(), 1);
                VoteRoom voteRoom = null;

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    VoteRoom room = dataSnapshot.getValue(VoteRoom.class);

                    if (room.getRoomCode().equals(roomCode)) {
                        String key = dataSnapshot.getKey();
                        voteRoom = room;
                        voteRoom.setDbID(key);
                        break;
                    }
                }

                // Returns null if room is not found
                listener.onDataGetVoteRoom(voteRoom);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    /**
     * Checks if multiple users have created a vote room with the same code at the same time.
     * @param listener listens if duplicate rooms are created
     * @param roomCode room code to check
     */
    public static void checkDuplicateVoteRooms(VoteRoomAddListener listener, String roomCode) {
        Query query = dbVoteRooms.orderByChild("roomCode").equalTo(roomCode);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "checkDuplicate",
                        "rooms: " + snapshot.getChildrenCount(), 1);
                boolean duplicate = snapshot.getChildrenCount() > 1;

                // Returns true if there are duplicate values
                listener.onDataAddVoteRoom(duplicate);
        }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    /**
     * Removes the given vote room from the database.
     * @param voteRoom vote room to remove
     */
    public static void removeVoteRoom(VoteRoom voteRoom) {
        dbVoteRooms.child(voteRoom.getDbID()).removeValue();
    }

    /**
     * Removes all vote room which have surpassed the maximum time limit from creation.
     */
    public static void removeExpiredVoteRooms() {
        long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(VOTE_ROOM_EXPIRE_TIME, TimeUnit.HOURS);
        Query oldVoteRooms = dbVoteRooms.orderByChild(ONLINE_TIMESTAMP).endAt(cutoff);
        oldVoteRooms.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot: snapshot.getChildren()) {
                    itemSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

    /**
     * Changes the state for the given vote room.
     * @param voteRoom vote room to change the state from
     * @param state new state
     */
    private static void changeVoteRoomState(VoteRoom voteRoom, int state, DatabaseAddListener listener) {
        // Set state to local voteRoom
        voteRoom.setState(state);

        // Set state to firebase
        dbVoteRooms
                .child(voteRoom.getDbID())
                .child(ONLINE_STATE).setValue(state)
                .addOnCompleteListener(complete ->  {
                    if(listener != null)
                        listener.onDataAddedComplete();
                });
    }

    /**
     * Listens for the vote room state changes.
     * @param voteRoom vote room to listen for
     * @param listener listener for vote room state changes
     */
    public static void listenForVoteRoomState(VoteRoom voteRoom, ValueEventListener listener) {
        DatabaseReference state = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_STATE);
        state.addValueEventListener(listener);
    }

    /**
     * Stops listening for the vote room state changes.
     * @param voteRoom vote room to stop listening
     * @param listener listener for vote room state changes
     */
    public static void stopListeningForVoteRoomState(VoteRoom voteRoom, ValueEventListener listener) {
        DatabaseReference state = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_STATE);
        state.removeEventListener(listener);
    }

    /**
     * Retrieves the current state of the vote room.
     * @param voteRoom vote room to listen
     * @param listener listens for the state of the vote room
     */
    public static void getVoteRoomState(VoteRoom voteRoom, DatabaseGetIntValueListener listener) {
        DatabaseReference state = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_STATE);
        state.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int value = dataSnapshot.getValue(Integer.class);
                listener.onDataGetString(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    /**
     * Listens for the vote room removal.
     *
     * If host disconnects before the last results, the vote room will be removed. If that
     * happens, move to MainActivity and show a toast message.
     * @param activity current activity
     * @param roomCode code for the vote room to listen for
     */
    public static void listenForVoteRoomExpiration(Activity activity, String roomCode) {
        voteRoomRemovalListener = new ChildEventListener() {
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                VoteRoom room = dataSnapshot.getValue(VoteRoom.class);

                if (room.getRoomCode().equals(roomCode)) {
                    activity.startActivity(new Intent(activity, MainActivity.class));
                    Buddy.showToast(activity.getString(R.string.vote_room_expired), Toast.LENGTH_LONG);
                    removeOnlineListeners();
                }
            }

            // Unused
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        dbVoteRooms.addChildEventListener(voteRoomRemovalListener);
    }

    private static void stopListeningForVoteRoomExpiration() {
        if (voteRoomRemovalListener == null)
            return;

        Debug.print("DatabaseHandler", "stopListeningForVoteRoomExpiration",
                "", 1);

        dbVoteRooms.removeEventListener(voteRoomRemovalListener);
        voteRoomRemovalListener = null;
    }
    //endregion

    //region VoteRoomSettings
    /////////////////////* VOTE ROOM SETTINGS *////////////////////

    public static void addVoteRoomSettings(VoteRoom voteRoom, VoteSettings settings, final DatabaseAddListener listener) {
        DatabaseReference dbSettings = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_SETTINGS);

        String key = dbSettings.push().getKey();   // Add new key to vote settings

        Map<String, Object> values = settings.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        // Listen for completion
        dbSettings.updateChildren(childUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        listener.onDataAddedComplete();
                    }
                });
    }

    public static void getVoteRoomSettings(final VoteSettingsListener listener, VoteRoom voteRoom) {
        DatabaseReference dbSettings = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_SETTINGS);

        dbSettings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "getVoteRoomSettings",
                        "settings: " + snapshot.getChildrenCount(), 1);

                VoteSettings settings = null;

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    VoteSettings value = dataSnapshot.getValue(VoteSettings.class);
                    settings = value;
                    break;
                }

                listener.onDataGetVoteSettings(settings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    //endregion

    //region Vote Room List
    /////////////////////* VOTE ROOM LIST *////////////////////

    public static void addVoteRoomList(VoteRoom voteRoom, ListOfItems listOfItems, final DatabaseAddListener listener) {
        DatabaseReference dbRef = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_LIST);

        dbRef.push();   // Add new key to online vote
        dbRef.setValue(listOfItems.getDbID());

        Map<String, Object> values = listOfItems.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(listOfItems.getDbID(), values);

        // Listen for completion
        dbRef.updateChildren(childUpdates)
                .addOnCompleteListener(task -> listener.onDataAddedComplete());
    }

    public static void getVoteRoomList(VoteRoom voteRoom, final VoteRoomGetListListener listener) {
        DatabaseReference dbRef = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_LIST);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "getVoteRoomList",
                        "list: " + snapshot.getChildrenCount(), 1);

                ListOfItems listOfItems = null;

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    ListOfItems value = dataSnapshot.getValue(ListOfItems.class);
                    listOfItems = value;
                    listOfItems.setDbID(snapshot.getKey());
                    break;
                }

                listener.onDataGetLists(listOfItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }
    //endregion

    //region VoteRoomItems
    /////////////////////* VOTE ROOM ITEMS *////////////////////

    /**
     * Adds items which users will vote to the provided vote room.
     * @param voteRoom vote room to add the items to
     * @param items items to add
     * @param listener items added listener
     */
    public static void addItemsToVoteRoom(final VoteRoom voteRoom, ArrayList<ListItem> items,
                                          DatabaseAddListener listener) {
        DatabaseReference dbOnlineItems = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_ITEMS);
        Map<String, Object> childUpdates = new HashMap<>();

        for (ListItem item : items) {
            item.setInOnlineLastVote(false); // Reset in online last vote state
            dbOnlineItems.push();
            dbOnlineItems.setValue(item.getDbID());

            Map<String, Object> listValues = item.toMap();

            childUpdates.put(item.getDbID(), listValues);
        }

        // Inform that items have been added
        dbOnlineItems.updateChildren(childUpdates)
                .addOnCompleteListener((complete) -> listener.onDataAddedComplete());
    }

    /**
     * Retrieves the items to vote in the provided vote room.
     * @param voteRoom vote room to retrieve the items from
     * @param listener listens for the items in the vote room
     */
    public static void getVoteRoomItems(VoteRoom voteRoom, VoteRoomGetItemsListener listener) {
        DatabaseReference dbOnlineItems = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_ITEMS);

        dbOnlineItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "getVoteRoomItems",
                        "items: " + snapshot.getChildrenCount(), 1);

                ArrayList<ListItem> items = new ArrayList<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    ListItem item = dataSnapshot.getValue(ListItem.class);
                    item.setDbID(dataSnapshot.getKey());
                    items.add(item);
                }

                listener.onDataGetVoteRoomItems(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    /**
     * Adds voted items to the vote room.
     * @param voteRoom vote room to add the voted items to
     * @param items voted items to add
     * @param listener listens for completion of adding the items
     */
    public static void addVoteRoomVotedItems(VoteRoom voteRoom, OnlineProfile onlineProfile,
                                             ArrayList<OnlineVotedItem> items,
                                             DatabaseAddListener listener) {

        DatabaseReference dbOnlineVotedItems = dbVoteRooms.child(voteRoom.getDbID()).child(getVoteRoomItemsState(onlineProfile));
        Map<String, Object> childUpdates = new HashMap<>();

        for (OnlineVotedItem item : items) {
            String key = dbOnlineVotedItems.push().getKey();

            Map<String, Object> listValues = item.toMap();

            childUpdates.put(key, listValues);
        }

        // When items are added, change room state to inform other users so they can load items
        dbOnlineVotedItems.updateChildren(childUpdates)
                .addOnCompleteListener(complete -> listener.onDataAddedComplete());
    }

    /**
     * Retrieves all of the voted items in provided vote room.
     * @param voteRoom vote room to retrieve the items from
     * @param listener listens for added vote items on the vote room
     */
    public static void getVoteRoomVotedItems(VoteRoom voteRoom, OnlineProfile onlineProfile, VoteRoomGetVotedItemsListener listener) {
        DatabaseReference dbOnlineItems = dbVoteRooms.child(voteRoom.getDbID()).child(getVoteRoomItemsState(onlineProfile));

        dbOnlineItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "getVoteRoomVotedItems",
                        "items: " + snapshot.getChildrenCount(), 1);

                ArrayList<OnlineVotedItem> items = new ArrayList<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    OnlineVotedItem item = dataSnapshot.getValue(OnlineVotedItem.class);
                    items.add(item);
                }

                listener.onDataGetVoteRoomVotedItems(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    /**
     * Selects the correct items for last vote.
     * @param voteRoom vote room to set the items
     * @param items items which should be in last vote
     */
    public static void setVoteRoomLastVoteItems(VoteRoom voteRoom, ArrayList<ListItem> items) {
        DatabaseReference dbOnlineItems = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_ITEMS);

        dbOnlineItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> childUpdate = new HashMap<>();

                for(ListItem item : items)
                    Debug.print("DatabaseHandler", "setVoteRoomLastVoteItems", "Items: " + item, 1);

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    ListItem item = dataSnapshot.getValue(ListItem.class);
                    item.setDbID(dataSnapshot.getKey());

                    Debug.print("DatabaseHandler", "setVoteRoomLastVoteItems", "Checking item: " + item, 1);

                    if (items.contains(item)) {
                        Debug.print("DatabaseHandler", "setVoteRoomLastVoteItems", "Items contains the item", 1);
                        ListItem selectedItem = items.get(items.indexOf(item));
                        Map<String, Object> values = selectedItem.toMap();
                        childUpdate.put(item.getDbID(), values);
                    }
                }

                // Set last vote boolean to all objects with same call
                dbOnlineItems.updateChildren(childUpdate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    /**
     * Retrieves correct state for vote items.
     * @param onlineProfile current online profile
     * @return correct path for vote items
     */
    private static String getVoteRoomItemsState(OnlineProfile onlineProfile) {
        return onlineProfile.getState() <= VoteRoom.RESULTS_FIRST ?
                ONLINE_VOTED_ITEMS_FIRST :
                ONLINE_VOTED_ITEMS_LAST;
    }
    //endregion

    //region OnlineProfile
    /////////////////////* ONLINE PROFILE *////////////////////
    /**
     * Connects a online profile to the vote room.
     * @param voteRoom vote room to connect to
     * @param profile profile to connect
     */
    public static void connectOnlineProfile(VoteRoom voteRoom, OnlineProfile profile) {
        DatabaseReference dbProfiles = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES);
        executeAddOnlineProfile(dbProfiles, profile, null);
    }

    private static void executeAddOnlineProfile(DatabaseReference ref, OnlineProfile profile,
                                                DatabaseAddListener listener) {
        String key = profile.getDbID(); // Use user id as key
        profile.setDbID(key);

        Map<String, Object> values = profile.toMap();
        values.put(ONLINE_TIMESTAMP, ServerValue.TIMESTAMP);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        ref.updateChildren(childUpdates).addOnCompleteListener((complete) -> {
            if (listener != null)
                listener.onDataAddedComplete();
        });
    }

    /**
     * Listens for changes in online profiles.
     * @param voteRoom vote room to listen for
     * @param listener listens for online profile changes
     */
    public static void listenForOnlineProfiles(VoteRoom voteRoom, ChildEventListener listener) {
        DatabaseReference dbProfiles = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES);
        Query orderedRef = dbProfiles.orderByChild(ONLINE_TIMESTAMP);
        orderedRef.addChildEventListener(listener);
    }

    /**
     * Stops listening for changes in online profiles.
     * @param voteRoom vote room to stop listen for
     * @param listener listens for online profile changes
     */
    public static void stopListeningForOnlineProfiles(VoteRoom voteRoom, ChildEventListener listener) {
        DatabaseReference dbProfiles = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES);
        dbProfiles.removeEventListener(listener);
    }

    /**
     * Listens for changes in online profiles.
     * @param voteRoom vote room to listen for
     * @param listener listens for online profile changes
     */
    public static void listenForOnlineProfiles(VoteRoom voteRoom, ValueEventListener listener) {
        DatabaseReference dbProfiles = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES);
        Query orderedRef = dbProfiles.orderByChild(ONLINE_TIMESTAMP);
        orderedRef.addValueEventListener(listener);
    }

    /**
     * Stops listening for changes in online profiles.
     * @param voteRoom vote room to stop listen for
     * @param listener listens for online profile changes
     */
    public static void stopListeningForOnlineProfiles(VoteRoom voteRoom, ValueEventListener listener) {
        DatabaseReference dbProfiles = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES);
        dbProfiles.removeEventListener(listener);
    }

    /**
     * Return all current online profiles connected to the provided vote room.
     * @param voteRoom vote room to get the profiles from
     * @param listener listens for connected online profiles
     */
    public static void getOnlineProfiles(VoteRoom voteRoom, VoteRoomGetOnlineProfilesListener listener) {
        DatabaseReference dbProfiles = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES);
        Query orderedRef = dbProfiles.orderByChild(ONLINE_TIMESTAMP);
        executeGetOnlineProfiles(orderedRef, listener);
    }

    private static void executeGetOnlineProfiles(Query ref, VoteRoomGetOnlineProfilesListener listener) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "executeGetOnlineProfiles, ref: " + ref,
                        "profiles: " + snapshot.getChildrenCount(), 1);

                ArrayList<OnlineProfile> onlineProfiles = new ArrayList<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);
                    onlineProfile.setDbID(dataSnapshot.getKey());
                    onlineProfiles.add(onlineProfile);
                }

                listener.onDataGetOnlineProfiles(onlineProfiles);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    /**
     * Removes profile.
     * @param profile profile to remove
     */
    public static void disconnectOnlineProfile(VoteRoom voteRoom, OnlineProfile profile) {
        // Stop listening for vote room expiration and user leaving the room
        removeOnlineListeners();

        // If host disconnects when vote room was in lobby, remove the vote room
        if (profile.isHost() && voteRoom.getState() == VoteRoom.LOBBY) {
            removeVoteRoom(voteRoom);
        } else {
            // Add profile to disconnected profiles if not in lobby
            if (voteRoom.getState() != VoteRoom.LOBBY) {
                addDisconnectedOnlineProfile(voteRoom, profile,
                        () -> executeRemoveOnlineProfile(voteRoom, profile));
            } else {
                executeRemoveOnlineProfile(voteRoom, profile);
            }
        }
    }

    /**
     * Removes the online profile from vote room.
     * @param voteRoom vote room to remove from
     * @param profile database reference path to profile
     */
    private static void executeRemoveOnlineProfile(VoteRoom voteRoom, OnlineProfile profile) {
        DatabaseReference ref = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES);
        ref.child(profile.getDbID()).removeValue()
                .addOnCompleteListener(complete -> {
                    // Remove vote room if all users disconnect from the room
                    getOnlineProfiles(voteRoom, (onlineProfiles -> {
                        if (onlineProfiles.isEmpty())
                            removeVoteRoom(voteRoom);
                    }));
                });
    }

    /**
     * Changes the state to next state.
     * @param voteRoom vote room to change the online profile state
     * @param onlineProfile online profile to change the ready state
     * @param listener listens for state change completion
     */
    public static void changeOnlineProfileState(VoteRoom voteRoom, OnlineProfile onlineProfile,
                                             DatabaseAddListener listener) {
        onlineProfile.setState(onlineProfile.getState() + 1);

        final int profileState = onlineProfile.getState();

        // Check if vote room is behind in states
        getVoteRoomState(voteRoom, (state) -> {
            // Set vote room state to profile state
            if (state < profileState) {
                changeVoteRoomState(voteRoom, profileState, () -> {
                    executeOnlineProfileStateChange(voteRoom, onlineProfile, listener);
                });
            } else {
                voteRoom.setState(profileState);
                executeOnlineProfileStateChange(voteRoom, onlineProfile, listener);
            }
        });
    }

    /**
     * Executes the online profile state change on firebase.
     * @param voteRoom current vote room
     * @param onlineProfile current profile
     * @param listener complete listener
     */
    private static void executeOnlineProfileStateChange(VoteRoom voteRoom, OnlineProfile onlineProfile,
                                                        DatabaseAddListener listener) {
        dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES).
                child(onlineProfile.getDbID()).child(ONLINE_STATE).setValue(onlineProfile.getState())
                .addOnCompleteListener(complete ->  {
                    if(listener != null)
                        listener.onDataAddedComplete(); });
    }

    /**
     * Add disconnected online profile to vote room.
     *
     * Reference to disconnected profile is required so that VoteResults
     * can retrieve the name and vote points of profile.
     * @param voteRoom vote room to add the profile
     * @param onlineProfile profile to add
     * @param listener on complete listener
     */
    private static void addDisconnectedOnlineProfile(VoteRoom voteRoom, OnlineProfile onlineProfile,
                                                     DatabaseAddListener listener) {
        DatabaseReference dbDisconnectedProfiles = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_DISCONNECTED_PROFILES);
        executeAddOnlineProfile(dbDisconnectedProfiles, onlineProfile, listener);
    }

    /**
     * Returns all profiles, which disconnected after vote room lobby.
     * @param voteRoom vote room to get profiles from
     * @param listener listener for retrieving the profiles
     */
    public static void getDisconnectedOnlineProfiles(VoteRoom voteRoom, VoteRoomGetOnlineProfilesListener listener) {
        DatabaseReference dbProfiles = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_DISCONNECTED_PROFILES);
        executeGetOnlineProfiles(dbProfiles, listener);
    }

    /**
     * Listens for if user leaves the room before vote is finished.
     * @param activity calling activity
     * @param voteRoom vote room to listen to
     */
    public static void listenForUserLeavingRoom(Activity activity, VoteRoom voteRoom) {
        userLeftRoomListener = new ChildEventListener() {
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                OnlineProfile onlineProfile = dataSnapshot.getValue(OnlineProfile.class);

                // If user was not in lobby and not in last room, inform that he left during vote
                if (onlineProfile.getState() != VoteRoom.LOBBY &&
                    onlineProfile.getState() != VoteRoom.RESULTS_LAST)
                    Buddy.showToast(activity.getString(R.string.profile_left_room, onlineProfile.getNickName()), Toast.LENGTH_LONG);
            }

            // Unused
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        userLeftRoomRef = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES);
        userLeftRoomRef.addChildEventListener(userLeftRoomListener);
    }

    private static void stopListeningForUserLeavingRoom() {
        if (userLeftRoomListener == null)
            return;

        Debug.print("DatabaseHandler", "stopListeningForUserLeavingRoom",
                "", 1);

        userLeftRoomRef.removeEventListener(userLeftRoomListener);
        userLeftRoomListener = null;
    }
    //endregion

    //region VoteSettings
    /////////////////////* VOTE SETTINGS *////////////////////

    /**
     * Adds new VoteSettings.
     * @param list list to add settings to
     * @param settings settings to add
     */
    public static void addVoteSettings(ListOfItems list, VoteSettings settings) {
        String key = dbVoteSettings.push().getKey();   // Add new key to path

        settings.setDbID(key);
        settings.setListID(list == null ? DEFAULT_VOTE_SETTINGS : list.getDbID());
        Map<String, Object> listValues = settings.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, listValues);

        dbVoteSettings.updateChildren(childUpdates);
    }

    /**
     * Loads VoteSettings which are part of the given list from database.
     * @param listener listener to send data to
     * @param list list where items must belong to
     */
    public static void getVoteSettings(final VoteSettingsListener listener, final ListOfItems list) {
        String listId = list == null ? DEFAULT_VOTE_SETTINGS : list.getDbID();
        Query query = dbVoteSettings.orderByChild("listID").equalTo(listId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "onDataChange",
                        "Settings count: " + snapshot.getChildrenCount(), 1);

                VoteSettings settings = null;

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    VoteSettings value = dataSnapshot.getValue(VoteSettings.class);
                    Debug.print("DatabaseHandler", "onDataChange",
                            "ListID: " + value.getListID(), 1);
                    value.setDbID(key);
                    settings = value;
                    break;
                }

                listener.onDataGetVoteSettings(settings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    /**
     * Modifies vote settings values.
     * @param settings vote settings to modify
     */
    public static void modifyVoteSettings(VoteSettings settings) {
        Map<String, Object> listValues = settings.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(settings.getDbID(), listValues);

        dbVoteSettings.updateChildren(childUpdates);
    }

    //endregion

    //region GlobalSettings
    /**
     * Adds new GlobalSettings.
     * @param listener settings to add
     */
    private static void addGlobalSettings(DatabaseAddListener listener) {
        String key = dbGlobalSettings.push().getKey();   // Add new key to path

        // Create new settings with default values
        GlobalSettings settings = new GlobalSettings();

        settings.setDbID(key);
        Map<String, Object> listValues = settings.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, listValues);

        dbGlobalSettings.updateChildren(childUpdates)
                .addOnCompleteListener((complete) -> listener.onDataAddedComplete());
    }

    /**
     * Loads GlobalSettings which connected to user database.
     * @param listener listener to send data to
     */
    public static void getGlobalSettings(final GlobalSettingsListener listener) {
        dbGlobalSettings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GlobalSettings settings = null;

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    GlobalSettings value = dataSnapshot.getValue(GlobalSettings.class);
                    value.setDbID(key);
                    settings = value;
                    break;
                }

                // If settings found, return them
                if (settings != null) {
                    listener.onDataGetGlobalSettings(settings);
                } else {
                    // Add new settings and retrieve them
                    addGlobalSettings(() -> getGlobalSettings(listener));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    /**
     * Modifies global settings values.
     * @param settings global settings to modify
     */
    public static void modifyGlobalSettings(GlobalSettings settings) {
        Map<String, Object> listValues = settings.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(settings.getDbID(), listValues);

        dbGlobalSettings.updateChildren(childUpdates);
    }
    //endregion
}
