package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListsActivity extends AppCompatActivity {

    private ListsActivity _this = this;
    private ArrayList<ListItem> items = new ArrayList<>();

    private final int NAME = 0, TOTAL = 1, BONUS = 2, PERIL = 3;
    private int curSort = NAME;
    private boolean ascending;
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

        sortList(curSort);

        showListItems();
    }

    private void setupTestItems() {
        items.add(new ListItem("Abyss Odyssey", 1, 0));
        items.add(new ListItem("Pacman", 3, 0));
        items.add(new ListItem("Kingdom Come", 3, 0));
        items.add(new ListItem("Super Smash Bros. Melee", 3, 0));
        items.add(new ListItem("Rocket League", 3, 0));
        items.add(new ListItem("Party Panic", 3, 0));
        items.add(new ListItem("Batman: Arkham City", 3, 0));
        items.add(new ListItem("Super Mario", 3, 0));
        items.add(new ListItem("Flappy Birds", 3, 0));
        items.add(new ListItem("Angry Birds", 3, 0));
        items.add(new ListItem("Think of the Children", 3, 0));
        items.add(new ListItem("Wii Sports", 3, 0));
    }

    public void showListItems() {
        final ListView list = findViewById(R.id.list);

        list.setAdapter(new ListItemAdapter(this, items));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item = (ListItem) list.getItemAtPosition(position);
                ListItemDialog cdd = new ListItemDialog(_this, item);
                cdd.show();
            }
        });
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

        sortList(sort);
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

    private void sortList(final int sort) {
        changeColor(sort);

        // If new sort is same as current, reverse order
        if (sort == curSort) {
            ascending = !ascending;
        }

        switch (sort) {
            case NAME:
                Collections.sort(items, new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                o1.getName().compareTo(o2.getName()) :
                                o2.getName().compareTo(o1.getName());
                    }
                });
                break;
            case TOTAL:
                Collections.sort(items, new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                ((Integer)o1.getTotal()).compareTo(o2.getTotal()) :
                                ((Integer)o2.getTotal()).compareTo(o1.getTotal());
                    }
                });
                break;
            case BONUS:
                Collections.sort(items, new Comparator<ListItem>() {
                    public int compare(ListItem o1, ListItem o2) {
                        return ascending ?
                                ((Integer)o1.getBonus()).compareTo(o2.getBonus()) :
                                ((Integer)o2.getBonus()).compareTo(o1.getBonus());
                    }
                });
                break;
            case PERIL:
                Collections.sort(items, new Comparator<ListItem>() {
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
