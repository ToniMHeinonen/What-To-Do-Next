package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        setupListItemElements();
    }

    private void setupListItemElements() {
        final ListView list = findViewById(R.id.list);
        ArrayList<String> items = new ArrayList<>();
        items.add("Abyss Odyssey");
        items.add("Pacman");
        items.add("Kingdom Come");
        items.add("Super Smash Bros. Melee");
        items.add("Rocket League");
        items.add("Party Panic");
        items.add("Batman: Arkham City");
        items.add("Super Mario");
        items.add("Flappy Birds");
        items.add("Angry Birds");
        items.add("Think of the Children");
        items.add("Wii Sports");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                R.layout.list_item, items);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedItem=(String) list.getItemAtPosition(position);
                System.out.println(clickedItem);
            }
        });
    }
}
