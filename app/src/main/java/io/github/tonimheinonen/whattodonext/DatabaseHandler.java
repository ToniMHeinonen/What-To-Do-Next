package io.github.tonimheinonen.whattodonext;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
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

    public static void getLists(final OnGetDataListener listener) {
        dbLists.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("DatabaseHandler", "onDataChange",
                        "Count: " + snapshot.getChildrenCount(), 1);

                HashMap<String, ListOfItems> lists = new HashMap<>();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
                    ListOfItems list = dataSnapshot.getValue(ListOfItems.class);
                    lists.put(key, list);
                }

                listener.onDataGetSuccess(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("DatabaseHandler", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }
}
