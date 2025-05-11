package com.example.CoinWatch

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ExploreMoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore_more)


        val backButton = findViewById<Button>(R.id.button_Back)
        backButton.setOnClickListener {
            finish()
        }
    }
}
