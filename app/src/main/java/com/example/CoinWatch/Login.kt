package com.example.CoinWatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.CoinWatch.data.AppDatabase
import com.example.CoinWatch.data.UserDao
import kotlinx.coroutines.launch
import java.security.MessageDigest

class Login : AppCompatActivity() {
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        val btnLogin = findViewById<Button>(R.id.button)
        val usernameField = findViewById<EditText>(R.id.editTextText2)
        val passwordField = findViewById<EditText>(R.id.editTextTextPassword)
        val goBackButton = findViewById<ImageButton>(R.id.imageButton)

        btnLogin.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hashedPassword = hashPassword(password)

            lifecycleScope.launch {
                val user = userDao.login(username, hashedPassword)
                if (user != null) {
                    val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                    prefs.edit().putInt("USER_ID", user.user_id).apply()

                    val intent = Intent(this@Login, HomeScreen::class.java)
                    intent.putExtra("username", username)
                    startActivity(intent)
                    finish()
                } else {
                    runOnUiThread {
                        Toast.makeText(this@Login, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        goBackButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(password.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
