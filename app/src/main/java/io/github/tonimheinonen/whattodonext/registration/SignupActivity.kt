package io.github.tonimheinonen.whattodonext.registration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.github.tonimheinonen.whattodonext.MainActivity
import io.github.tonimheinonen.whattodonext.R
import io.github.tonimheinonen.whattodonext.tools.Buddy

/**
 * Handles signing up to Firebase database.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0
 * @since 1.0
 */
class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText

    private lateinit var signUpBtn: Button
    private lateinit var loginBtn: Button

    private lateinit var showPasswordView: CheckBox

    /**
     * Initializes SignupActivity.
     * @param savedInstanceState previous instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        emailEt = findViewById(R.id.email_edt_text)
        passwordEt = findViewById(R.id.pass_edt_text)

        loginBtn = findViewById(R.id.login_btn)
        signUpBtn = findViewById(R.id.signup_btn)

        showPasswordView = findViewById(R.id.showPassword)

        signUpBtn.setOnClickListener{
            var email: String = emailEt.text.toString()
            var password: String = passwordEt.text.toString()

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Buddy.showToast( getString(R.string.fields_empty), Toast.LENGTH_LONG)
            } else{
                Buddy.showLoadingBar(this, R.id.informationBox)

                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener{ task ->
                    if(task.isSuccessful){
                        Buddy.showToast( getString(R.string.registration_success), Toast.LENGTH_LONG)
                        onAuthSuccess(task.result?.user!!)
                    }
                }).addOnFailureListener(this, OnFailureListener { exception ->
                    if (exception is FirebaseAuthUserCollisionException) {
                        Buddy.showToast(getString(R.string.registration_user_collision), Toast.LENGTH_LONG)
                    } else if (exception is FirebaseAuthWeakPasswordException) {
                        Buddy.showToast( getString(R.string.registration_weak_password), Toast.LENGTH_LONG)
                    } else if (exception is FirebaseNetworkException) {
                        Buddy.showToast( getString(R.string.firebase_no_internet), Toast.LENGTH_LONG)
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        Buddy.showToast( getString(R.string.registration_invalid_credentials), Toast.LENGTH_LONG)
                    } else {
                        Buddy.showToast( getString(R.string.firebase_unusual_error), Toast.LENGTH_LONG)
                    }
                    Buddy.hideLoadingBar(this, R.id.informationBox)
                })
            }
        }

        loginBtn.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Creates new user.
     * @param user new user
     */
    private fun onAuthSuccess(user: FirebaseUser) {
        val username = usernameFromEmail(user.email!!)

        // Write new user
        writeNewUser(user.uid, username, user.email)

        // Go to MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Creates user name from email.
     * @param email user's email
     */
    private fun usernameFromEmail(email: String): String {
        return if (email.contains("@")) {
            email.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            email
        }
    }

    /**
     * Writes new user to database.
     * @param userId id of the user
     * @param name name of the user
     * @param email email of the user
     */
    private fun writeNewUser(userId: String, name: String, email: String?) {
        val user = User(name, email)
        database.child("users").child(userId).setValue(user)
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
