package io.github.tonimheinonen.whattodonext.voteactivity;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.Profile;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

import android.os.Bundle;

import java.util.ArrayList;


public class VoteTopActivity extends AppCompatActivity {

    private int topAmount;
    private ListOfItems selectedList;
    private ArrayList<Profile> selectedProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_top);

        topAmount = getIntent().getIntExtra("amount", -1);
        selectedList = VoteMaster.getSelectedList();
        selectedProfiles = VoteMaster.getSelectedProfiles();
    }
}
