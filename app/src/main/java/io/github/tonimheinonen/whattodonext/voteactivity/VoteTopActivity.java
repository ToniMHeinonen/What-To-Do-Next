package io.github.tonimheinonen.whattodonext.voteactivity;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.Debug;
import io.github.tonimheinonen.whattodonext.Profile;
import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemAdapter;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItemDialog;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class VoteTopActivity extends AppCompatActivity {

    private int topAmount;
    private ListOfItems selectedList;
    private ArrayList<Profile> selectedProfiles;

    private Button nextButton;
    private TextView profileView, infoView;
    private VoteItemAdapter adapter;

    private HashMap<String, ListItem> votedItems = new HashMap<>();
    private ArrayList<Integer> votePoints = new ArrayList<>();
    private int currentVotePoint, currentProfileIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_top);

        Intent intent = getIntent();
        topAmount = intent.getIntExtra("topAmount", -1);
        selectedList = intent.getExtras().getParcelable("selectedList");
        selectedProfiles = intent.getParcelableArrayListExtra("selectedProfiles");

        profileView = findViewById(R.id.profileName);
        infoView = findViewById(R.id.voteInfoText);
        nextButton = findViewById(R.id.nextButton);

        setupVoteItems();

        startVoting();
    }

    private void setupVoteItems() {
        final ListView list = findViewById(R.id.voteItems);
        adapter = new VoteItemAdapter(this, selectedList.getItems());

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item = (ListItem) list.getItemAtPosition(position);

                int value = item.modifyVotePoint(currentVotePoint);

                if (value == -1)
                    removeVotePoint();
                else
                    addVotePoint(value);
            }
        });
    }

    private void sortVotePoints() {
        Collections.sort(votePoints, Collections.<Integer>reverseOrder());
        currentVotePoint = votePoints.get(0);
    }

    private void addVotePoint(int value) {
        votePoints.add(value);
        sortVotePoints();
        updateVoteItems();
    }

    private void removeVotePoint() {
        votePoints.remove(0);

        if (!votePoints.isEmpty()) {
            currentVotePoint = votePoints.get(0);
        }

        updateVoteItems();
    }

    private void updateVoteItems() {
        if (!votePoints.isEmpty()) {
            infoView.setText(getString(R.string.give_points_to, currentVotePoint));
            nextButton.setVisibility(View.INVISIBLE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
            infoView.setText(getString(R.string.vote_ready, getString(R.string.vote_next)));
        }
        adapter.notifyDataSetChanged();
    }

    private void startVoting() {
        profileView.setText(selectedProfiles.get(currentProfileIndex).getName());

        for (int i = topAmount; i > 0; i--) {
            votePoints.add(i);
        }

        currentVotePoint = votePoints.get(0);

        for (ListItem item : selectedList.getItems()) {
            item.clearVotePoints();
        }

        updateVoteItems();
    }

    public void backPressed(View v) {
        currentProfileIndex--;

        if (currentProfileIndex == -1) {
            super.onBackPressed();
        }
    }

    public void nextPressed(View v) {
        currentProfileIndex++;

        if (currentProfileIndex == selectedProfiles.size()) {
            // Move to next acitivity
        } else {
            startVoting();
        }
    }
}
