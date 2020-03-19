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

public abstract class DatabaseHandler {

    private static FirebaseUser user;
    private static DatabaseReference dbLists;
    private static DatabaseReference dbItems;
    private static DatabaseReference dbProfiles;
    private static boolean offlinePersistenceEnabled = false;

    public static void initialize() {

        if (!offlinePersistenceEnabled) {
            offlinePersistenceEnabled = true;
            // This only needs to be called once, otherwise app crashes
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
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

    public static void addList(ListOfItems list) {
        String key = dbLists.push().getKey();
        GlobalPrefs.saveCurrentList(key);

        list.setDbID(key);
        Map<String, Object> listValues = list.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, listValues);

        dbLists.updateChildren(childUpdates);
    }

    public static void removeList(ListOfItems list) {
        dbLists.child(list.getDbID()).removeValue();

        // Remove all items from database connected to this list
        for (ListItem item : list.getItems()) {
            removeItem(item);
        }
    }

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

    public static void addItem(ListOfItems list, ListItem item) {
        String key = dbItems.push().getKey();

        item.setDbID(key);
        item.setListID(list.getDbID());
        Map<String, Object> listValues = item.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, listValues);

        dbItems.updateChildren(childUpdates);
    }

    public static void modifyItem(ListItem item) {
        Map<String, Object> listValues = item.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(item.getDbID(), listValues);

        dbItems.updateChildren(childUpdates);
    }

    public static void removeItem(ListItem item) {
        dbItems.child(item.getDbID()).removeValue();
    }

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

    public static void addProfile(Profile profile) {
        String key = dbProfiles.push().getKey();

        profile.setDbID(key);
        Map<String, Object> listValues = profile.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, listValues);

        dbProfiles.updateChildren(childUpdates);
    }

    public static void getProfiles(final OnGetDataListener listener) {
        dbLists.addListenerForSingleValueEvent(new ValueEventListener() {
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
