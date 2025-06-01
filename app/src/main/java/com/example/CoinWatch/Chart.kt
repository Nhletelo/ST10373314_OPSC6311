package com.example.CoinWatch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.example.CoinWatch.data.AppDatabase
import com.example.CoinWatch.data.ExpenseDao

import kotlinx.coroutines.launch

class Chart : AppCompatActivity() {

    private lateinit var anyChartView: AnyChartView
    private val categories = arrayOf("Food", "Transport", "Entertainment", "Utilities", "Other")
    private val expenses = intArrayOf(2500, 1800, 1200, 800, 700)
    private lateinit var db: AppDatabase // Your RoomDB class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chart)

        db = AppDatabase.getDatabase(this)

        // ✅ Retrieve userId like you do in HomeScreen
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)
        Log.d("ChartActivity", "User ID: $userId")

        val expenseDao = db.expenseDao()

        lifecycleScope.launch {
            val expensesByCategory = expenseDao.getTotalExpensesByCategory(userId)
            if (expensesByCategory.isEmpty()) {
                Log.d("ChartActivity", "No expenses found for the user.")
            } else {
                setupChartView(expensesByCategory)
            }
        }

        anyChartView = findViewById(R.id.anyChartView)
        //setupChartView()

        val btnHome = findViewById<ImageButton>(R.id.imageButton9)
        btnHome.setOnClickListener {
            highlightSelectedButton(R.id.imageButton8)
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
        }

        val btnTransact = findViewById<ImageButton>(R.id.imageButton10)
        btnTransact.setOnClickListener {
            highlightSelectedButton(R.id.imageButton10)
            val intent = Intent(this, Transactions::class.java)
            startActivity(intent)
        }

        val btnFAB = findViewById<ImageButton>(R.id.imageButton8)
        btnFAB.setOnClickListener {
            highlightSelectedButton(R.id.imageButton8)
            val intent = Intent(this, AddExpense::class.java)
            startActivity(intent)
        }



// Highlight the Chart button on this screen by default
        highlightSelectedButton(R.id.imageButton11)
        val btnChart = findViewById<ImageButton>(R.id.imageButton11)
        btnChart.setOnClickListener {
            // Chart screen action is redundant since we're already here
            highlightSelectedButton(R.id.imageButton11)

        }

        val btnMore = findViewById<ImageButton>(R.id.imageButton12)
        btnMore.setOnClickListener {
            highlightSelectedButton(R.id.imageButton12)
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)

        }
    }
    private fun highlightSelectedButton(selectedButtonId: Int) {
        val buttons = listOf(
            findViewById<ImageButton>(R.id.imageButton8),
            findViewById<ImageButton>(R.id.imageButton9),
            findViewById<ImageButton>(R.id.imageButton10),
            findViewById<ImageButton>(R.id.imageButton11),
            findViewById<ImageButton>(R.id.imageButton12),
        )

        for (button in buttons) {
            if (button.id == selectedButtonId) {
                // Apply highlight background (e.g., selected state)
                button.setBackgroundResource(R.drawable.button_border_selected)
            } else {
                // Reset to default background
                button.setBackgroundResource(R.drawable.button_border_default)
            }
        }
    }
    private fun setupChartView(data: List<ExpenseDao.CategoryExpenseTotal>) {
        val pie = AnyChart.pie()
        val dataEntries = data.map {
            ValueDataEntry(it.category_name, it.total)
        }

        pie.data(dataEntries)
        pie.title("Expenses by Category")
        pie.labels().position("outside")
        pie.legend().title().enabled(true)
        pie.legend().title().text("Categories")
        pie.legend().position("center-bottom")
        pie.legend().itemsLayout("horizontal")
        pie.legend().align("center")
        pie.palette(arrayOf("#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0"))

        anyChartView.setChart(pie)
    }
}

