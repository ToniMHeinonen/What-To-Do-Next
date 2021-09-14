package io.github.tonimheinonen.whattodonext;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.tools.Buddy;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;
import io.github.tonimheinonen.whattodonext.tools.HTMLDialog;

public class StartVoteActivity extends AppCompatActivity
        implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_vote);

        // If tutorial has not been confirmed yet, show it
        /*
        (If online vote crashes and it points to this, ignore the error, it just means
        that the app restarted because of some unknown error and for some reason prefs
         do not work instantly. Error is somewhere else. Use try and catch for easier
         debugging.)
         */
        if (GlobalPrefs.loadPopupInfo(GlobalPrefs.TUTORIAL_VOTE_TYPE))
            new HTMLDialog(this, HTMLDialog.HTMLText.TUTORIAL_VOTE_TYPE).show();

        // Set listeners for buttons
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.local).setOnClickListener(this);
        findViewById(R.id.online).setOnClickListener(this);
    }

    /**
     * Listens clicks of views.
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.local:
                Buddy.moveToVoteSetup(this, false);
                break;
            case R.id.online:
                Buddy.moveToVoteSetup(this, true);
                break;
            default:
                break;
        }
    }
}
