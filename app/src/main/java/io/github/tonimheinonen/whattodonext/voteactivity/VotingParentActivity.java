package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import io.github.tonimheinonen.whattodonext.database.DatabaseHandler;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

/**
 * Handles all common vote related stuff which all vote activities need.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.31
 * @since 1.31
 */
public class VotingParentActivity extends AppCompatActivity implements LifecycleObserver {

    protected VoteMaster voteMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        Intent intent = getIntent();
        voteMaster = intent.getParcelableExtra(VoteMaster.VOTE_MASTER);
    }

    protected void parentStartActivity(Intent intent) {
        intent.putExtra(VoteMaster.VOTE_MASTER, voteMaster);

        startActivity(intent);
    }

    @Override
    protected void onStop() {
        Buddy.hideOnlineVoteLoadingBar(this);
        super.onStop();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        // Disconnect all listeners, so they do not get duplicated
        if (voteMaster.isOnline())
            DatabaseHandler.removeOnlineListeners();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        // App returns to foreground
    }
}
