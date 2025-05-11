package com.example.CoinWatch

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SupportMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_message)

        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val phoneEditText = findViewById<EditText>(R.id.editTextPhone)
        val messageEditText = findViewById<EditText>(R.id.editTextMessage)
        val sendButton = findViewById<Button>(R.id.buttonSend)
        val backButton = findViewById<Button>(R.id.buttonBack)

        backButton.setOnClickListener {
            finish()
        }

        sendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val message = messageEditText.text.toString().trim()

            if (email.isBlank() || phone.isBlank() || message.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {


                Toast.makeText(
                    this,
                    "Thank you for trusting CoinWatch. We will assist you shortly.",
                    Toast.LENGTH_LONG
                ).show()


                finish()
            }
        }
    }
}
