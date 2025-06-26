package com.example.CoinWatch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.CircularProgressIndicator

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DURATION = 5000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val spinner = findViewById<CircularProgressIndicator>(R.id.circularSpinner)
        val blinkingText = findViewById<TextView>(R.id.blinkingText)

        // Set spinner color
        spinner.setIndicatorColor(
            ContextCompat.getColor(this, R.color.capitec_blue),
            ContextCompat.getColor(this, R.color.capitec_teal),
            ContextCompat.getColor(this, R.color.capitec_purple)
        )

        // Animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        val blink = AnimationUtils.loadAnimation(this, R.anim.blink)

        // Start animations
        spinner.startAnimation(fadeIn)
        blinkingText.startAnimation(blink)

        Handler(mainLooper).postDelayed({
            spinner.startAnimation(fadeOut)
            blinkingText.startAnimation(fadeOut)

            spinner.postDelayed({
                spinner.visibility = View.GONE
                blinkingText.visibility = View.GONE

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 700) // fade-out duration
        }, SPLASH_DURATION)
    }
}
