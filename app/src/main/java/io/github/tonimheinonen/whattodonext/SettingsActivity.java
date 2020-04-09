package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

/**
 * Handles controlling of global values.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void settingSelected(View v) {
        switch (v.getId()) {
            case R.id.maxPeril:

                break;
            case R.id.firstVote:

                break;
            case R.id.lastVote:

                break;
            case R.id.ignoreUnselected:

                break;
            case R.id.halfExtra:

                break;
        }
    }

    public void backSelected(View v) {
        super.onBackPressed();
    }
}
