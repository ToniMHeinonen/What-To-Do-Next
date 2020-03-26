package io.github.tonimheinonen.whattodonext;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.listsactivity.ListItem;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;
import io.github.tonimheinonen.whattodonext.voteactivity.StartVoteDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class VoteActivity extends AppCompatActivity implements OnGetDataListener {

    private VoteActivity _this = this;

    private ArrayList<ListOfItems> lists = new ArrayList<>();
    private ArrayList<Profile> profiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseHandler.getLists(this);
    }

    @Override
    public void onDataGetLists(ArrayList<ListOfItems> lists) {
        Debug.print(this, "onDataGetLists", "", 1);
        this.lists = lists;

        if (lists.isEmpty()) {
            Debug.print(this, "onDataGetLists", "isEmpty", 1);
            new AlertDialog.Builder(this)
                    .setTitle("No lists found!")
                    .setMessage("Create a new list before voting.")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton("Move to lists view", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(_this, ListsActivity.class));
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton("Go back",  new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(_this, MainActivity.class));
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            DatabaseHandler.getProfiles(this);
        }
    }

    @Override
    public void onDataGetProfiles(ArrayList<Profile> profiles) {
        this.profiles = profiles;

        showStartVoteDialog();
    }

    @Override
    public void onDataGetItems(ArrayList<ListItem> items) {
        if (items.size() < GlobalPrefs.loadListVoteSize()) {
            Buddy.showToast(Buddy.getString(R.string.toast_not_enough_activities));

            showStartVoteDialog();
            return;
        }

        // Start voting
    }

    private void showStartVoteDialog() {
        StartVoteDialog dialog = new StartVoteDialog(this, lists, profiles);
        dialog.show();
    }
}
