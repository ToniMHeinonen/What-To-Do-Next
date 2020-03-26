package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public abstract class Buddy {

    private static Context context;

    public static void initialize(Context host) {
        context = host;
    }

    /**
     * Hides keyboard, clears focus on view and clears it's text.
     *
     * Notice that in order for this to work, you need to add android:focusable="true" &
     * android:focusableInTouchMode="true" to current view's parent layout.
     * @param editTextView
     */
    public static void hideKeyboardAndClear(EditText editTextView) {
        editTextView.clearFocus();
        editTextView.setText("");
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextView.getWindowToken(), 0);
    }
}
