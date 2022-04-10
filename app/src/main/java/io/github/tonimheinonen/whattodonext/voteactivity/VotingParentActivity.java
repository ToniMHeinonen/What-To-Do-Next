package io.github.tonimheinonen.whattodonext.voteactivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;
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
}
