package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ListsActivity extends AppCompatActivity {

    private Activity _this = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        setupListItemElements();
    }

    private void setupListItemElements() {
        final ListView list = findViewById(R.id.list);
        ArrayList<ListItem> items = new ArrayList<>();
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

        list.setAdapter(new ListItemAdapter(this, items));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item = (ListItem) list.getItemAtPosition(position);
                ListItemDialog cdd=new ListItemDialog(_this, item);
                cdd.show();
            }
        });
    }
}
