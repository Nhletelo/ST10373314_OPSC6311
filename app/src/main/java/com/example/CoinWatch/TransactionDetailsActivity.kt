package com.example.CoinWatch

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import java.io.File

class TransactionDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        // Request storage permission (only for older Android versions)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            100
        )

        // Get data from Intent
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val amount = intent.getStringExtra("amount")
        val date = intent.getStringExtra("date")
        val startTime = intent.getStringExtra("startTime")
        val category = intent.getStringExtra("category")
        val photoPath = intent.getStringExtra("photoPath")
        val iconResId = intent.getIntExtra("icon", R.drawable.ic_launcher_foreground)

        // Find Views
        val titleTextView = findViewById<TextView>(R.id.transactionTitle)
        val descriptionTextView = findViewById<TextView>(R.id.transactionDescription)
        val amountTextView = findViewById<TextView>(R.id.transactionAmount)
        val dateTextView = findViewById<TextView>(R.id.transactionDate)
        val timeTextView = findViewById<TextView>(R.id.transactionTime)
        val categoryTextView = findViewById<TextView>(R.id.transactionCategory)
        val iconImageView = findViewById<ImageView>(R.id.transactionIcon)
        val photoImageView = findViewById<ImageView>(R.id.transactionPhotoImageView)
        val back = findViewById<ImageView>(R.id.imageButton27)

        // Set Data
        titleTextView.text = title
        descriptionTextView.text = description
        amountTextView.text = "R $amount"
        dateTextView.text = "Date: $date"
        timeTextView.text = "Time: $startTime"
        categoryTextView.text = "Category: $category"
        iconImageView.setImageResource(iconResId)

        // Debug: log photo path and file existence
        Log.d("TransactionDetails", "Photo path: $photoPath")

        // Load image with Glide safely
        if (!photoPath.isNullOrEmpty()) {
            val file = File(photoPath)
            if (file.exists()) {
                Log.d("TransactionDetails", "File exists, loading with Glide")
                Glide.with(this)
                    .load(file)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.gallery)
                    .into(photoImageView)
            } else {
                Log.e("TransactionDetails", "File does NOT exist: $photoPath")
                photoImageView.setImageResource(R.drawable.gallery)
            }
        } else {
            Log.e("TransactionDetails", "photoPath is null or empty")
            photoImageView.setImageResource(R.drawable.placeholder_image)
        }

        // Handle Back Button
        back.setOnClickListener {
            startActivity(Intent(this, Transactions::class.java))
        }

        // Handle image click to show fullscreen
        photoImageView.setOnClickListener {
            if (!photoPath.isNullOrEmpty()) {
                val intent = Intent(this, FullscreenImageActivity::class.java)
                intent.putExtra("imagePath", photoPath)
                startActivity(intent)
            }
        }
    }
}
