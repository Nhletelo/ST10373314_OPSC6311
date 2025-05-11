package com.example.CoinWatch

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File

class FullscreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        val imagePath = intent.getStringExtra("imagePath")

        // Find the ImageView to display the image
        val fullscreenImageView = findViewById<ImageView>(R.id.fullscreenImageView)

        // Load the image using Glide
        if (!imagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(File(imagePath))
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(fullscreenImageView)
        }
    }
}
