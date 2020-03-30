package io.github.tonimheinonen.whattodonext.voteactivity;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.Debug;
import io.github.tonimheinonen.whattodonext.Profile;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

import android.content.Intent;
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

        Intent i = getIntent();
        topAmount = i.getIntExtra("topAmount", -1);
        selectedList = i.getExtras().getParcelable("selectedList");
        selectedProfiles = i.getParcelableArrayListExtra("selectedProfiles");

        Debug.print(this, "onCreate", "List: " + selectedList.toString(), 1);
        Debug.print(this, "onCreate", "Items: " + selectedList.getItems().toString(), 1);
        Debug.print(this, "onCreate", "Profiles: " + selectedProfiles.toString(), 1);
    }
}
