package io.github.tonimheinonen.whattodonext;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ListItemDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Activity activity;
    private ListItem item;
    private EditText name, bonusAmount, perilAmount;

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

        // Get item values
        name = findViewById(R.id.itemName);
        name.setText(item.getName());
        bonusAmount = findViewById(R.id.bonusPoints);
        perilAmount = findViewById(R.id.perilPoints);
        updatePointTexts();

        // Set listeners for + and - signs
        Button bonusMinus = findViewById(R.id.bonusMinus);
        Button bonusPlus = findViewById(R.id.bonusPlus);
        Button perilMinus = findViewById(R.id.perilMinus);
        Button perilPlus = findViewById(R.id.perilPlus);
        bonusMinus.setOnClickListener(this);
        bonusPlus.setOnClickListener(this);
        perilMinus.setOnClickListener(this);
        perilPlus.setOnClickListener(this);
    }

    private void getValuesFromEditTexts() {
        String name = this.name.getText().toString();
        item.setName(name);

        // If text is "", set value to 0, else get value from text
        String bonus = bonusAmount.getText().toString();
        item.setBonus(bonus.equals("") ? 0 : Integer.parseInt(bonus));

        String peril = perilAmount.getText().toString();
        item.setPeril(peril.equals("") ? 0 : Integer.parseInt(peril));
    }

    private void updatePointTexts() {
        bonusAmount.setText(String.valueOf(item.getBonus()));
        perilAmount.setText(String.valueOf(item.getPeril()));
    }

    @Override
    public void onClick(View v) {
        getValuesFromEditTexts();

        switch (v.getId()) {
            case R.id.bonusMinus:
                item.setBonus(item.getBonus() - 1);
                break;
            case R.id.bonusPlus:
                item.setBonus(item.getBonus() + 1);
                break;
            case R.id.perilMinus:
                item.setPeril(item.getPeril() - 1);
                break;
            case R.id.perilPlus:
                item.setPeril(item.getPeril() + 1);
                break;
            default:
                break;
        }
        updatePointTexts();
        //dismiss();
    }
}
