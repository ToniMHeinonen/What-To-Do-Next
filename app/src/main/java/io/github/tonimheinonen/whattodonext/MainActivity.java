package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void menuButtonClicked(View v) {
        if (v.getId() == R.id.voteButton) {

        } else if (v.getId() == R.id.listButton) {

        } else if (v.getId() == R.id.settingsButton) {

        }
    }
}
