package io.github.tonimheinonen.whattodonext;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

public abstract class DatabaseHandler {

    private static FirebaseUser user;
    private static DatabaseReference dbLists;

    public static void initialize() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        dbLists = FirebaseDatabase.getInstance().getReference().child("users").
                child(user.getUid()).child("lists");
    }

    public static void addList(ListOfItems list) {
        String key = dbLists.push().getKey();

        Map<String, Object> listValues = list.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, listValues);

        dbLists.updateChildren(childUpdates);
    }
}
