package com.example.CoinWatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.CoinWatch.data.AppDatabase
import com.example.CoinWatch.data.BudgetGoalDao
import com.example.CoinWatch.data.ExpenseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class HomeScreen : AppCompatActivity() {
    private lateinit var db: AppDatabase // Your RoomDB class
    private lateinit var expenseDao: ExpenseDao
    private lateinit var budgetGoalDao: BudgetGoalDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_screen)

        db = AppDatabase.getDatabase(applicationContext)

        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val username = prefs.getString("username", null)
        val userId = prefs.getInt("USER_ID", -1)
        expenseDao = db.expenseDao()

        CoroutineScope(Dispatchers.IO).launch {
            val totalSpent = expenseDao.getTotalSpentByUser(userId) ?: 0.0

            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.spentAmountTextView).text = "R%.2f".format(totalSpent)
            }
        }

        Log.d("HomeScreen", "User ID: $userId")  // Log user ID
        val welcomeText = findViewById<TextView>(R.id.textView21)
        welcomeText.text = if (username != null) "Welcome, $username" else "Welcome"

        if (username == null && intent.hasExtra("username")) {
            val editor = prefs.edit()
            val newUsername = intent.getStringExtra("username") ?: "Guest"
            editor.putString("username", newUsername)
            editor.apply()
        }

        val balanceTextView = findViewById<TextView>(R.id.spentAmountTextView)

        if (userId != -1) {
            val db = AppDatabase.getDatabase(this)  // Ensure you have this method in your DB singleton
            val budgetDao = db.budgetGoalDao()
            //val currentMonth = getCurrentMonth()
            //Log.d("HomeScreen", "Current Month: $currentMonth")  // Log current month
        }
        findViewById<ImageButton>(R.id.imageButton29).setOnClickListener {
            val intent = Intent(this, SupportMessageActivity::class.java)
            startActivity(intent)
        }


        val exploreButton = findViewById<Button>(R.id.button5)
        exploreButton.setOnClickListener {
            val intent = Intent(this, ExploreMoreActivity::class.java)
            startActivity(intent)
        }



        // Set up button listeners
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            // Home screen action is redundant since we're already here
        }
        findViewById<ImageButton>(R.id.imageButton21).setOnClickListener {
            startActivity(Intent(this, SetupBudget::class.java))
        }
        findViewById<ImageButton>(R.id.imageButton22).setOnClickListener {
            startActivity(Intent(this, BudgetGoalSetup::class.java))
        }
        findViewById<ImageButton>(R.id.imageButton4).setOnClickListener {
            startActivity(Intent(this, Transactions::class.java))
        }
        findViewById<ImageButton>(R.id.imageButton5).setOnClickListener {
            startActivity(Intent(this, AddExpense::class.java))
        }
        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            startActivity(Intent(this, Chart::class.java))
        }
        findViewById<ImageButton>(R.id.imageButton7).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        val btnLocateATM = findViewById<ImageButton>(R.id.btnLocateATM)
        btnLocateATM.setOnClickListener {
            val intent = Intent(this, ATMLocatorActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)

        if (userId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                val totalSpent = expenseDao.getTotalSpentByUser(userId) ?: 0.0

                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.spentAmountTextView).text = "R%.2f".format(totalSpent)
                }
            }
        }
    }
}
