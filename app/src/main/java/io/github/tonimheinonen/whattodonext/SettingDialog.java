package io.github.tonimheinonen.whattodonext;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

/**
 * Handles user changing app settings.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class SettingDialog extends Dialog implements
        View.OnClickListener {

    private Activity activity;

    public static final int MAX_PERIL = 0, FIRST_VOTE = 1, LAST_VOTE = 2, IGNORE_UNSELECTED = 3,
    HALF_EXTRA = 4;
    private int setting;

    /**
     * Initializes necessary values.
     * @param a current activity
     * @param setting selected setting
     */
    public SettingDialog(Activity a, int setting) {
        super(a);
        this.activity = a;
        this.setting = setting;
    }

    /**
     * Initializes views.
     * @param savedInstanceState previous Activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting_dialog);

        // Set listeners for confirm and cancel
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.confirm).setOnClickListener(this);
    }

    /**
     * Listens for view clicks.
     * @param v clicked view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                cancel();
                break;
            case R.id.confirm:
                break;
            default:
                break;
        }
    }
}
