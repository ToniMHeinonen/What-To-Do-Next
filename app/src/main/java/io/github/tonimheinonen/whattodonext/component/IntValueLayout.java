package io.github.tonimheinonen.whattodonext.component;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.github.tonimheinonen.whattodonext.R;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

public class IntValueLayout {

    private Activity a;
    private String title;
    private EditText points;
    private int min = -9999;
    private int max = 9999;
    private IntValueLayout minRelative, maxRelative;

    private int initialValue;
    private int value;

    private int underMinToastTextID, overMaxToastTextID;

    public IntValueLayout(Activity a, Dialog dialog, LinearLayout parent, String title, int value) {
        this.a = a;
        this.title = title;
        initialValue = value;

        // Create layout and add it to the parent object
        View child = dialog.getLayoutInflater().inflate(R.layout.int_value_layout, null);
        parent.addView(child);

        // Set title text
        TextView t = child.findViewById(R.id.title);
        t.setText(title);

        // Set value
        points = child.findViewById(R.id.points);
        setValue(value);

        // Set on click listeners
        child.findViewById(R.id.minus).setOnClickListener(this::minusClicked);
        child.findViewById(R.id.plus).setOnClickListener(this::plusClicked);

        underMinToastTextID = R.string.points_must_be_higher;
        overMaxToastTextID = R.string.points_must_be_lower;
    }

    private void minusClicked(View v) {
        int nextValue = value - 1;

        if (valueIsHigherThanMinOrEqual(nextValue))
            setValue(nextValue);
    }

    private void plusClicked(View v) {
        int nextValue = value + 1;

        if (valueIsLowerThanMaxOrEqual(nextValue))
            setValue(nextValue);
    }

    public boolean valueIsLegal() {
        // Get value from text if user has changed text manually
        int currentValue = Integer.parseInt(points.getText().toString());

        if (valueIsHigherThanMinOrEqual(currentValue) &&
            valueIsLowerThanMaxOrEqual(currentValue)) {
            return true;
        } else {
            setValue(initialValue);
            return false;
        }
    }

    private boolean valueIsHigherThanMinOrEqual(int value) {
        int minimum = minRelative == null ? min : minRelative.getValue() + 1;

        if (value < minimum) {
            String text = a.getString(underMinToastTextID, title, minimum);
            Buddy.showToast(text, Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    private boolean valueIsLowerThanMaxOrEqual(int value) {
        int maximum = maxRelative == null ? max : maxRelative.getValue() - 1;

        if (value > maximum) {
            String text = a.getString(overMaxToastTextID, title, maximum);
            Buddy.showToast(text, Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    private void checkValue() {
        int minimum = minRelative == null ? min : minRelative.getValue() + 1;
        int maximum = maxRelative == null ? max : maxRelative.getValue() - 1;

        if (value < minimum)
            setValue(minimum);
        else if (value > maximum)
            setValue(maximum);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        points.setText(String.valueOf(value));
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        if (min > max)
            throw new IllegalArgumentException("Min has to be lower than max!");

        this.min = min;
        checkValue();
    }

    public void setMin(IntValueLayout minRelative) {
        this.minRelative = minRelative;
        checkValue();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        if (max < min)
            throw new IllegalArgumentException("Max has to be higher than min!");

        this.max = max;
        checkValue();
    }

    public void setMax(IntValueLayout maxRelative) {
        this.maxRelative = maxRelative;
        checkValue();
    }

    public void setUnderMinToastTextID(int underMinToastTextID) {
        this.underMinToastTextID = underMinToastTextID;
    }

    public void setOverMaxToastTextID(int overMaxToastTextID) {
        this.overMaxToastTextID = overMaxToastTextID;
    }
}
