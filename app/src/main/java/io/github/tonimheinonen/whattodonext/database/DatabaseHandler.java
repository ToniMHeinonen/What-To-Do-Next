package io.github.tonimheinonen.whattodonext.database;

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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
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

    // Online voting
    private static DatabaseReference dbVoteRooms;
    private static String ONLINE_PROFILES = "profiles";
    private static String ONLINE_ITEMS = "items";
    private static String ONLINE_VOTED_ITEMS = "voted_items";

    private static int MAX_SAVED_RESULTS = 7;

    /**
     * Initializes necessary values.
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

        // Init online voting references
        dbVoteRooms = FirebaseDatabase.getInstance().getReference().child("vote_rooms");
        dbVoteRooms.keepSynced(true);
    }

    /////////////////////* LISTENER INTERFACES *////////////////////

    public interface ListsListener {
        /**
         * Gets loaded lists from database.
         * @param lists loaded lists from database
         */
        void onDataGetLists(ArrayList<ListOfItems> lists);
    }

    public interface ItemsListener {
        /**
         * gets loaded items from database.
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

    public interface DatabaseAddListener {

        /**
         * Listens when adding data is complete.
         */
        void onDataAddedComplete();
    }

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

    /////////////////////* SAVED RESULTS *////////////////////

    /**
     * Adds new result.
     * @param result result to add
     */
    public static void addResult(SavedResult result) {
        String key = dbSavedResults.push().getKey();   // Add new key to results

        result.setDbID(key);
        Map<String, Object> values = result.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        dbSavedResults.updateChildren(childUpdates);

        getResults(DatabaseHandler::checkResultsAmount);
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

    /////////////////////* RESULT ITEMS *////////////////////

    /**
     * Adds result items from saved result to database.
     * @param result result which holds result items
     */
    public static void addResultItems(SavedResult result, ArrayList<SavedResultItem> items) {
        Map<String, Object> childUpdates = new HashMap<>();

        for (SavedResultItem item : items) {
            String key = dbResultItems.push().getKey();   // Add new key to path

            // Set result db id for item
            item.setDbID(key);
            item.setResultID(result.getDbID());
            Map<String, Object> listValues = item.toMap();

            childUpdates.put(key, listValues);
        }

        dbResultItems.updateChildren(childUpdates);
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

    /////////////////////* VOTE ROOMS *////////////////////

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

                dbVoteRooms.updateChildren(childUpdates);
                listener.onDataAddVoteRoom(true);
            } else {
                listener.onDataAddVoteRoom(false);
            }
        }, voteRoom.getRoomCode());
    }

    public static void getVoteRoom(VoteRoomGetListener listener, String roomCode) {
        dbVoteRooms.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public static void removeVoteRoom(VoteRoom voteRoom) {
        dbVoteRooms.child(voteRoom.getDbID()).removeValue();
    }

    public static void removeAllVoteRooms() {
        dbVoteRooms.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "getVoteRoom",
                        "rooms: " + snapshot.getChildrenCount(), 1);
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    dataSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public static void changeVoteRoomState(VoteRoom voteRoom, String state) {
        dbVoteRooms.child(voteRoom.getDbID()).child("state").setValue(state);
    }

    public static void getVoteRoomState(VoteRoom voteRoom, ValueEventListener listener) {
        DatabaseReference state = dbVoteRooms.child(voteRoom.getDbID()).child("state");
        state.addValueEventListener(listener);
    }

    public static void connectOnlineProfile(VoteRoom voteRoom, OnlineProfile profile) {
        DatabaseReference dbProfiles = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES);

        String key = dbProfiles.push().getKey();   // Add new key to profiles
        profile.setDbID(key);

        Map<String, Object> values = profile.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        dbProfiles.updateChildren(childUpdates);
    }

    public static void getOnlineProfiles(VoteRoom voteRoom, ChildEventListener listener) {
        DatabaseReference dbProfiles = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES);
        dbProfiles.addChildEventListener(listener);
    }

    /**
     * Removes profile.
     * @param profile profile to remove
     */
    public static void disconnectOnlineProfile(VoteRoom voteRoom, OnlineProfile profile) {
        DatabaseReference dbProfiles = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES);
        dbProfiles.child(profile.getDbID()).removeValue();
    }

    public static void addItemsToVoteRoom(final VoteRoom voteRoom, ArrayList<ListItem> items) {
        DatabaseReference dbOnlineItems = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_ITEMS);
        Map<String, Object> childUpdates = new HashMap<>();

        for (ListItem item : items) {
            dbOnlineItems.push();
            dbOnlineItems.setValue(item.getDbID());

            Map<String, Object> listValues = item.toMap();

            childUpdates.put(item.getDbID(), listValues);
        }

        // When items are added, change room state to inform other users so they can load items
        dbOnlineItems.updateChildren(childUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        changeVoteRoomState(voteRoom, VoteRoom.VOTING_FIRST);
                    }
                });
    }

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

    public static void addVoteRoomVotedItems(VoteRoom voteRoom, ArrayList<OnlineVotedItem> items,
                                             DatabaseAddListener listener) {
        DatabaseReference dbOnlineVotedItems = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_VOTED_ITEMS);
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

    public static void getVoteRoomVotedItems(VoteRoom voteRoom, VoteRoomGetVotedItemsListener listener) {
        DatabaseReference dbOnlineItems = dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_VOTED_ITEMS);

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

    public static void setOnlineProfileReady(VoteRoom voteRoom, OnlineProfile onlineProfile,
                                             boolean isReady) {
        dbVoteRooms.child(voteRoom.getDbID()).child(ONLINE_PROFILES).
                child(onlineProfile.getDbID()).child("ready").setValue(isReady);
    }
}
