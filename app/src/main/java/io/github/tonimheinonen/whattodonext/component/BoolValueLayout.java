package io.github.tonimheinonen.whattodonext.component;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import io.github.tonimheinonen.whattodonext.R;

public class BoolValueLayout {

    private SwitchCompat onOffSwitch;

    public BoolValueLayout(Dialog dialog, LinearLayout parent, String title, boolean value) {
        // Create layout and add it to the parent object
        View child = dialog.getLayoutInflater().inflate(R.layout.bool_value_layout, null);
        parent.addView(child);

        // Set title text
        TextView t = child.findViewById(R.id.title);
        t.setText(title);

        // Set value
        onOffSwitch = child.findViewById(R.id.onOffSwitch);
        onOffSwitch.setChecked(value);
    }

    public boolean getValue() {
        return onOffSwitch.isChecked();
    }

    public void setValue(boolean value) { onOffSwitch.setChecked(value); }
}
