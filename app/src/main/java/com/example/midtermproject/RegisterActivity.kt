package com.example.midtermproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * RegisterActivity allows new users to create an account using email and password.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var registerButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    /**
     * Initializes the activity and sets up UI components.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        registerButton = findViewById(R.id.registerButton)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        // Set click listener for register button
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            registerUser(email, password)
        }
    }

    /**
     * Registers a new user with Firebase Authentication.
     * @param email User's email address.
     * @param password User's chosen password.
     */
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful, navigate to MainActivity
                    val user = auth.currentUser
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Registration failed, display a message to the user
                    Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}