package io.github.tonimheinonen.whattodonext;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

public class ClickListenerDialog extends Dialog implements
        android.view.View.OnClickListener {

    public ClickListenerDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public void onClick(View v) {

    }
}
