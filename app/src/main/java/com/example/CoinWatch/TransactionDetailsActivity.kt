package com.example.CoinWatch

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File

class TransactionDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

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

        // Load image with Glide
        if (!photoPath.isNullOrEmpty()) {
            Glide.with(this)
                .load(File(photoPath))
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.gallery)
                .into(photoImageView)
        } else {
            photoImageView.setImageResource(R.drawable.placeholder_image)
        }
        back.setOnClickListener {
            startActivity(Intent(this, Transactions::class.java))
        }
        photoImageView.setOnClickListener {
            if (!photoPath.isNullOrEmpty()) {
                val intent = Intent(this, FullscreenImageActivity::class.java)
                intent.putExtra("imagePath", photoPath)
                startActivity(intent)
            }
        }

    }
}
