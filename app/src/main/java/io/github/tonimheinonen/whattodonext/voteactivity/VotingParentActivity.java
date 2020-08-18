package io.github.tonimheinonen.whattodonext.voteactivity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

/**
 * Handles all common vote related stuff which all vote activities need.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.31
 * @since 1.31
 */
public class VotingParentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStop() {
        Buddy.hideOnlineVoteLoadingBar(this);
        super.onStop();
    }
}
