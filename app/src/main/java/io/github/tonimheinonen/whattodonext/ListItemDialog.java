package io.github.tonimheinonen.whattodonext;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class ListItemDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Activity activity;
    private ListItem item;

    public ListItemDialog(Activity a, ListItem item) {
        super(a);
        this.activity = a;
        this.item = item;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.list_item_dialog);
        Button bonusMinus = findViewById(R.id.bonusMinus);
        Button bonusPlus = findViewById(R.id.bonusPlus);
        bonusMinus.setOnClickListener(this);
        bonusPlus.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bonusMinus:
                activity.finish();
                break;
            case R.id.bonusPlus:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
