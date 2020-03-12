package io.github.tonimheinonen.whattodonext.listsactivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import io.github.tonimheinonen.whattodonext.ListsActivity;
import io.github.tonimheinonen.whattodonext.R;

public class ListItemDialog extends Dialog implements
        android.view.View.OnClickListener {

    private ListsActivity activity;
    private ListItem item;
    private EditText name, bonusAmount, perilAmount;
    private final int BONUS_INDEX = 0, PERIL_INDEX = 1;

    public ListItemDialog(ListsActivity a, ListItem item) {
        super(a);
        this.activity = a;
        this.item = item;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.list_item_dialog);

        // Get item values
        name = findViewById(R.id.itemName);
        name.setText(item.getName());
        bonusAmount = findViewById(R.id.bonusPoints);
        bonusAmount.setText(String.valueOf(item.getBonus()));
        perilAmount = findViewById(R.id.perilPoints);
        perilAmount.setText(String.valueOf(item.getPeril()));

        // Set listeners for + and - signs
        findViewById(R.id.bonusMinus).setOnClickListener(this);
        findViewById(R.id.bonusPlus).setOnClickListener(this);
        findViewById(R.id.perilMinus).setOnClickListener(this);
        findViewById(R.id.perilPlus).setOnClickListener(this);

        // Set listeners for confirm and cancel
        findViewById(R.id.confirm).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
    }

    private int[] getPointsFromEditTexts() {
        int[] points = new int[2];

        // If text is "", set value to 0, else get value from text
        String bonusStr = bonusAmount.getText().toString();
        int bonus = bonusStr.equals("") ? 0 : Integer.parseInt(bonusStr);

        String perilStr = perilAmount.getText().toString();
        int peril = perilStr.equals("") ? 0 : Integer.parseInt(perilStr);

        points[BONUS_INDEX] = bonus;
        points[PERIL_INDEX] = peril;

        return points;
    }

    @Override
    public void onClick(View v) {
        int[] points = getPointsFromEditTexts();
        int bonus = points[BONUS_INDEX];
        int peril = points[PERIL_INDEX];

        switch (v.getId()) {
            case R.id.bonusMinus:
                bonusAmount.setText(String.valueOf(bonus - 1));
                break;
            case R.id.bonusPlus:
                bonusAmount.setText(String.valueOf(bonus + 1));
                break;
            case R.id.perilMinus:
                perilAmount.setText(String.valueOf(peril - 1));
                break;
            case R.id.perilPlus:
                perilAmount.setText(String.valueOf(peril + 1));
                break;
            case R.id.confirm:
                confirmChanges();
                break;
            case R.id.cancel:
                cancel();
                break;
            default:
                break;
        }
    }

    private void confirmChanges() {
        String n = name.getText().toString();
        item.setName(n);

        int[] points = getPointsFromEditTexts();
        item.setBonus(points[BONUS_INDEX]);
        item.setPeril(points[PERIL_INDEX]);

        activity.itemDialogConfirmed();

        dismiss();
    }
}
