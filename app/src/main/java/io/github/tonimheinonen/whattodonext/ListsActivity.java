package io.github.tonimheinonen.whattodonext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.listsactivity.ListDialog;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemAdapter;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemDialog;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListsActivity extends AppCompatActivity {

    private ListsActivity _this = this;
    private ListOfItems curList = new ListOfItems("Test");

    private final int NAME = 0, TOTAL = 1, BONUS = 2, PERIL = 3;
    private int curSort = NAME;
    private boolean ascending = true;
    private TextView vName, vTotal, vBonus, vPeril;
    private int originalTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        // Find topics so that color can be changed
        vName = findViewById(R.id.name);
        vTotal = findViewById(R.id.total);
        vBonus = findViewById(R.id.bonus);
        vPeril = findViewById(R.id.peril);
        originalTextColor = vName.getCurrentTextColor();

        setupTestItems();

        sortList(curSort, false);

        showListItems();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users").
                child(user.getUid()).child("lists");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Debug.print("ListsActivity", "onDataChange",
                        "Count: " + snapshot.getChildrenCount(), 1);

                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    ListOfItems list = postSnapshot.getValue(ListOfItems.class);
                    Debug.print("ListsActivity", "onDataChange",
                            "Get Data: " + list.getName(), 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Debug.print("ListsActivity", "onCancelled", "", 1);
                databaseError.toException().printStackTrace();
            }
        });
    }

    private void setupTestItems() {
        curList.getItems().add(new ListItem("Abyss Odyssey", 1, 0));
        curList.getItems().add(new ListItem("Pacman", 2, 0));
        curList.getItems().add(new ListItem("Kingdom Come", 10, 6));
        curList.getItems().add(new ListItem("Super Smash Bros. Melee", 3, 0));
        curList.getItems().add(new ListItem("Rocket League", 1, 9));
        curList.getItems().add(new ListItem("Party Panic", 6, 0));
        curList.getItems().add(new ListItem("Batman: Arkham City", 3, 0));
        curList.getItems().add(new ListItem("Super Mario", 7, 0));
        curList.getItems().add(new ListItem("Flappy Birds", 3, 0));
        curList.getItems().add(new ListItem("Angry Birds", 1, 5));
        curList.getItems().add(new ListItem("Think of the Children", 3, 0));
        curList.getItems().add(new ListItem("Wii Sports", 0, 2));
    }

    public void showListItems() {
        final ListView list = findViewById(R.id.list);

        list.setAdapter(new ListItemAdapter(this, curList.getItems()));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item = (ListItem) list.getItemAtPosition(position);
                ListItemDialog dialog = new ListItemDialog(_this, item);
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        showListItems();
                    }
                });
            }
        });
    }

    public void itemDialogConfirmed() {
        sortList(curSort, false);
        showListItems();
    }

    public void addList(String name) {
        ListOfItems list = new ListOfItems(name);
        curList = list;
        showListItems();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String key = db.child("users").child(user.getUid()).child("lists").push().getKey();

        Map<String, Object> listValues = list.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + user.getUid() + "/lists/" + key, listValues);

        db.updateChildren(childUpdates);
    }

    public void loadList(String name) {

    }

    public void addClicked(View v) {
        final ListItem item = new ListItem("", 0, 0);
        curList.getItems().add(item);
        ListItemDialog dialog = new ListItemDialog(this, item);
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                curList.getItems().remove(item);
            }
        });
    }

    public void listClicked(View v) {
        ListDialog dialog = new ListDialog(this);
        dialog.show();
    }

    public void topicClicked(View v) {
        int sort = -1;

        switch (v.getId()) {
            case R.id.name:
                sort = NAME;
                break;
            case R.id.total:
                sort = TOTAL;
                break;
            case R.id.bonus:
                sort = BONUS;
                break;
            case R.id.peril:
                sort = PERIL;
                break;
        }

        sortList(sort, true);
    }

    private void changeColor(int selected) {
        vName.setTextColor(selected == NAME ?
                getResources().getColor(R.color.textColorPrimary) :
                originalTextColor);

        vTotal.setTextColor(selected == TOTAL ?
                getResources().getColor(R.color.textColorPrimary) :
                originalTextColor);

        vBonus.setTextColor(selected == BONUS ?
                getResources().getColor(R.color.textColorPrimary) :
                originalTextColor);

        vPeril.setTextColor(selected == PERIL ?
                getResources().getColor(R.color.textColorPrimary) :
                originalTextColor);
    }

    private void sortList(final int sort, boolean topicClicked) {
        changeColor(sort);

        // If new sort is same as current, reverse order
        if (sort == curSort && topicClicked) {
            ascending = !ascending;
        }

        switch (sort) {
            case NAME:
                Collections.sort(curList.getItems(), new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                o1.getName().compareTo(o2.getName()) :
                                o2.getName().compareTo(o1.getName());
                    }
                });
                break;
            case TOTAL:
                Collections.sort(curList.getItems(), new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                ((Integer)o1.getTotal()).compareTo(o2.getTotal()) :
                                ((Integer)o2.getTotal()).compareTo(o1.getTotal());
                    }
                });
                break;
            case BONUS:
                Collections.sort(curList.getItems(), new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                ((Integer)o1.getBonus()).compareTo(o2.getBonus()) :
                                ((Integer)o2.getBonus()).compareTo(o1.getBonus());
                    }
                });
                break;
            case PERIL:
                Collections.sort(curList.getItems(), new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                ((Integer)o1.getPeril()).compareTo(o2.getPeril()) :
                                ((Integer)o2.getPeril()).compareTo(o1.getPeril());
                    }
                });
                break;
        }

        curSort = sort;
        showListItems();
    }
}
