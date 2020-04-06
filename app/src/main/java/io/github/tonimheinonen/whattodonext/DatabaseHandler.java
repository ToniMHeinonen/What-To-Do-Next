package io.github.tonimheinonen.whattodonext;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

/**
 * Handles controlling of Firebase database.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public abstract class DatabaseHandler {

    private static FirebaseUser user;
    private static DatabaseReference dbLists;
    private static DatabaseReference dbItems;
    private static DatabaseReference dbProfiles;
    private static boolean offlinePersistenceEnabled = false;

    /**
     * Initializes necessary values.
     */
    public static void initialize() {
        if (!offlinePersistenceEnabled) {
            offlinePersistenceEnabled = true;
            // This only needs to be called once, otherwise app crashes
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

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
     * Removes list.
     * @param list list to remove
     */
    public static void removeList(final ListOfItems list) {
        dbLists.child(list.getDbID()).removeValue();

        // Remove all items from database connected to this list
        dbItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ListItem> items = new ArrayList<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    ListItem item = dataSnapshot.getValue(ListItem.class);
                    item.setDbID(key);
                    if (item.getListID().equals(list.getDbID())) {
                        removeItem(item);
                    }
                }

                for(ListItem item : items) {

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
     * Loads lists from database.
     * @param listener listener to send data to
     */
    public static void getLists(final OnGetDataListener listener) {
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
    public static void getItems(final OnGetDataListener listener, final ListOfItems list) {
        dbItems.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    if (item.getListID().equals(list.getDbID())) {
                        items.add(item);
                    }
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
    public static void getProfiles(final OnGetDataListener listener) {
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
}
