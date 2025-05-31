package com.example.CoinWatch

import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ExploreMoreActivity : AppCompatActivity() {

    private lateinit var tipTextView: TextView
    private var currentIndex = 0

    private val challengeTips = listOf(
        Pair(
            "Challenge: Overspending on eating out and takeout.",
            "Tip: Try meal prepping for the week to save money and eat healthier."
        ),
        Pair(
            "Challenge: Impulse purchases leading to budget blowouts.",
            "Tip: Wait 24 hours before buying to avoid impulsive spending."
        ),
        Pair(
            "Challenge: Difficulty tracking daily expenses.",
            "Tip: Use CoinWatch to log every expense daily and review weekly reports."
        ),
        Pair(
            "Challenge: Frequent credit card debt accumulation.",
            "Tip: Use cash envelopes for discretionary spending to stay within limits."
        ),
        Pair(
            "Challenge: Overlooking small, frequent purchases that add up.",
            "Tip: Use the 'Round-Up' feature in CoinWatch to save spare change automatically."
        ),
        Pair(
            "Challenge: Falling victim to marketing emails and ads.",
            "Tip: Unsubscribe from promotional emails and avoid browsing shopping sites unnecessarily."
        ),
        Pair(
            "Challenge: Struggling to save regularly.",
            "Tip: Set automatic monthly savings in CoinWatch and treat savings like a bill to pay first."
        ),
        Pair(
            "Challenge: No clear budget categories leading to overspending.",
            "Tip: Create clear spending limits in CoinWatch for each category and stick to them."
        ),
        Pair(
            "Challenge: Weekend splurges ruining monthly budgets.",
            "Tip: Take on the 'No-Spend Weekend' challenge to reset your spending habits."
        ),
        Pair(
            "Challenge: Forgetting financial goals.",
            "Tip: Set and review your monthly goals in CoinWatch regularly to stay motivated."
        )
    )

    private val handler = Handler()
    private val runnable = object : Runnable {
        override fun run() {
            showNextChallengeTip()
            handler.postDelayed(this, 10_000) // every 10 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore_more)

        tipTextView = findViewById(R.id.text_ai_advice)
        val backButton = findViewById<Button>(R.id.button_Back)

        backButton.setOnClickListener {
            finish()
        }

        showNextChallengeTip()
        handler.postDelayed(runnable, 10_000)
    }

    private fun showNextChallengeTip() {
        val (challenge, tip) = challengeTips[currentIndex]
        tipTextView.text = "🔥 Challenge:\n$challenge\n\n💡 CoinWatch Tip:\n$tip"
        currentIndex = (currentIndex + 1) % challengeTips.size
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
