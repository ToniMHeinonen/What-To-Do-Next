package io.github.tonimheinonen.whattodonext.registration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import io.github.tonimheinonen.whattodonext.R
import io.github.tonimheinonen.whattodonext.tools.Buddy

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
                Toast.makeText(this, getString(R.string.email_empty), Toast.LENGTH_LONG).show()
            } else {
                Buddy.registrationShowLoading(this)

                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(this, OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, getString(R.string.password_reset_success), Toast.LENGTH_LONG)
                                        .show()
                                startActivity(Intent(this, LoginActivity::class.java));
                            }
                        }).addOnFailureListener(this, OnFailureListener { exception ->
                            if (exception is FirebaseNetworkException) {
                                Toast.makeText(this, getString(R.string.firebase_no_internet), Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, getString(R.string.password_reset_failed), Toast.LENGTH_LONG).show()
                            }
                            Buddy.registrationHideLoading(this)
                        })
            }
        }
    }
}