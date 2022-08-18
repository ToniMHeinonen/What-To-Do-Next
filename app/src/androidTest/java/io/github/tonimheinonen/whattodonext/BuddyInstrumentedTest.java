package io.github.tonimheinonen.whattodonext;

import android.content.Context;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Tests functions in Buddy, which require context and other Android specific classes.
 */
@RunWith(AndroidJUnit4.class)
public class BuddyInstrumentedTest {
    @Test
    public void returns_correct_input_from_edit_text() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        EditText editText = new EditText(context);
        editText.setText("2");
        int value = Buddy.getIntFromEditText(editText, 0);

        assertEquals(2, value);
    }

    @Test
    public void returns_correct_default_value_from_edit_text() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        EditText editText = new EditText(context);
        editText.setText("");
        int value = Buddy.getIntFromEditText(editText, 0);

        assertEquals(0, value);
    }

    @Test
    public void force_upper_case_edit_text_abc() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        EditText editText = new EditText(context);
        Buddy.forceUpperCaseEditText(editText);
        editText.setText("abc");
        assertEquals("ABC", editText.getText().toString());
    }
}
