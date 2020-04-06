package io.github.tonimheinonen.whattodonext.database

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import io.github.tonimheinonen.whattodonext.R

/**
 * Handles resetting forgotten password.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var emailEt: EditText

    private lateinit var resetPasswordBtn: Button
    private lateinit var back: Button

    /**
     * Initializes ForgotPasswordActivity.
     * @param savedInstanceState previous instance state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        emailEt = findViewById(R.id.email_edt_text)

        resetPasswordBtn = findViewById(R.id.reset_pass_btn)
        back = findViewById(R.id.back_btn)

        back.setOnClickListener {
            finish()
        }

        resetPasswordBtn.setOnClickListener {
            var email: String = emailEt.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter email id", Toast.LENGTH_LONG).show()
            } else {
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(this, OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG)
                                        .show()
                                startActivity(Intent(this, LoginActivity::class.java));
                            } else {
                                Toast.makeText(this, "Unable to send reset mail", Toast.LENGTH_LONG)
                                        .show()
                            }
                        })
            }
        }
    }
}