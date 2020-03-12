package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.database.LoginActivity;
import io.github.tonimheinonen.whattodonext.database.UpdatePasswordActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Already logged in", Toast.LENGTH_LONG).show();
        }

        // Write a message to the database
        /*FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Debug.print("MainActivity", "onDataChange",
                        "Value is: " + value, 1);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("MainActivity", "Failed to read value.", error.toException());
            }
        });*/
    }

    /**
     * Starts selected activity.
     * @param v clicked button
     */
    public void menuButtonClicked(View v) {
        Intent intent;

        if (v.getId() == R.id.voteButton) {
            intent = new Intent(this, VoteActivity.class);
        } else if (v.getId() == R.id.listButton) {
            intent = new Intent(this, ListsActivity.class);
        } else if (v.getId() == R.id.settingsButton) {
            intent = new Intent(this, SettingsActivity.class);
        } else if (v.getId() == R.id.updatePassWordButton) {
            FirebaseAuth.getInstance().signOut();
            intent = new Intent(this, UpdatePasswordActivity.class);
        } else if (v.getId() == R.id.logOutButton) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            // Add else statement to fix possibility for null intent
            return;
        }

        startActivity(intent);
    }
}
