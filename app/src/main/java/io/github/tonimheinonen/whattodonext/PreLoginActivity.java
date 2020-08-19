package io.github.tonimheinonen.whattodonext;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import io.github.tonimheinonen.whattodonext.registration.LoginActivity;
import io.github.tonimheinonen.whattodonext.tools.HTMLDialog;

public class PreLoginActivity extends AppCompatActivity
        implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_login);

        // Set listeners for buttons
        findViewById(R.id.loginOrRegister).setOnClickListener(this);
        findViewById(R.id.noRegister).setOnClickListener(this);
    }

    /**
     * Listens clicks of views.
     * @param v view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginOrRegister:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.noRegister:
                new HTMLDialog(this, HTMLDialog.HTMLText.NO_REGISTRATION).show();
                break;
            default:
                break;
        }
    }
}
