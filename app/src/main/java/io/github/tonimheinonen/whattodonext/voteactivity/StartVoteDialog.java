package io.github.tonimheinonen.whattodonext.voteactivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.Debug;
import io.github.tonimheinonen.whattodonext.ListsActivity;
import io.github.tonimheinonen.whattodonext.Profile;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.VoteActivity;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

public class StartVoteDialog extends Dialog implements
        View.OnClickListener {

    private VoteActivity activity;
    private ArrayList<ListOfItems> lists;
    private ArrayList<Profile> profiles;

    public StartVoteDialog(VoteActivity a, ArrayList<ListOfItems> lists, ArrayList<Profile> profiles) {
        super(a);
        this.activity = a;
        this.lists = lists;
        this.profiles = profiles;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.start_vote_dialog);

        // Set listeners for confirm and cancel
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.addProfile).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }
}
