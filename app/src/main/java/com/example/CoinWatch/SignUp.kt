package com.example.CoinWatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.CoinWatch.data.AppDatabase
import com.example.CoinWatch.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

class SignUp : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        db = AppDatabase.getDatabase(this)

        val btnSignup = findViewById<Button>(R.id.button2)
        val goBackButton = findViewById<ImageButton>(R.id.imageButton2)

        btnSignup.setOnClickListener {
            val editTextUsername = findViewById<EditText>(R.id.editTextText)
            val editTextPassword = findViewById<EditText>(R.id.editTextTextPassword2)
            val editConfirmPassword = findViewById<EditText>(R.id.editTextTextPassword3)

            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString()
            val confirmPassword = editConfirmPassword.text.toString()

            if (username.isEmpty()) {
                editTextUsername.error = "Username cannot be empty"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                editTextPassword.error = "Password cannot be empty"
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                editConfirmPassword.error = "Confirm Password cannot be empty"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                editConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val existingUser = db.userDao().getUserByUsername(username)
                if (existingUser != null) {
                    runOnUiThread {
                        Toast.makeText(this@SignUp, "Username already exists", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val hashedPassword = hashPassword(password)
                    val newUser = User(username = username, password = hashedPassword)
                    db.userDao().insertUser(newUser)

                    runOnUiThread {
                        Toast.makeText(this@SignUp, "Account created successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignUp, Login::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

        goBackButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(password.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
