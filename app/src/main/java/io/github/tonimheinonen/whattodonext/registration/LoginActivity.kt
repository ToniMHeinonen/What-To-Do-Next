package io.github.tonimheinonen.whattodonext.registration

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import io.github.tonimheinonen.whattodonext.MainActivity
import io.github.tonimheinonen.whattodonext.R
import io.github.tonimheinonen.whattodonext.tools.Buddy

/**
 * Handles logging in to Firebase database.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText

    private lateinit var signupBtn: Button
    private lateinit var loginBtn: Button

    private lateinit var resetPasswordTv: TextView
    private lateinit var showPasswordView: CheckBox

    /**
     * Initializes LoginActivity.
     * @param savedInstanceState previous instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEt = findViewById(R.id.email_edt_text)
        passwordEt = findViewById(R.id.pass_edt_text)

        signupBtn = findViewById(R.id.signup_btn)
        loginBtn = findViewById(R.id.login_btn)

        resetPasswordTv = findViewById(R.id.reset_pass_tv)
        showPasswordView = findViewById(R.id.showPassword)

        auth = FirebaseAuth.getInstance()

        loginBtn.setOnClickListener {
            var email: String = emailEt.text.toString()
            var password: String = passwordEt.text.toString()

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Buddy.showToast(getString(R.string.fields_empty), Toast.LENGTH_LONG)
            } else{
                Buddy.showLoadingBar(this, R.id.informationBox)

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                    if(task.isSuccessful) {
                        Buddy.showToast(getString(R.string.login_success), Toast.LENGTH_LONG)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }).addOnFailureListener(this, OnFailureListener { exception ->
                    if (exception is FirebaseAuthInvalidUserException) {
                        // For some reason this only checks valid email addresses
                        Buddy.showToast( getString(R.string.login_invalid_user), Toast.LENGTH_LONG)
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        Buddy.showToast( getString(R.string.login_invalid_password), Toast.LENGTH_LONG)
                    } else if (exception is FirebaseNetworkException) {
                        Buddy.showToast( getString(R.string.firebase_no_internet), Toast.LENGTH_LONG)
                    } else {
                        Buddy.showToast(getString(R.string.firebase_unusual_error), Toast.LENGTH_LONG)
                    }
                    Buddy.hideLoadingBar(this, R.id.informationBox)
                })
            }
        }

        signupBtn.setOnClickListener{
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }

        resetPasswordTv.setOnClickListener{
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Shows and hides the password.
     */
    fun showPassword(v : View) {
        if (showPasswordView.isChecked)
            passwordEt.transformationMethod = HideReturnsTransformationMethod.getInstance()
        else
            passwordEt.transformationMethod = PasswordTransformationMethod.getInstance()
    }
}
