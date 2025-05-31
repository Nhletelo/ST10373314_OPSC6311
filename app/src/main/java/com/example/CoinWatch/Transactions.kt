package com.example.CoinWatch

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.widget.AdapterView
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.CoinWatch.data.AppDatabase
import com.example.CoinWatch.data.ExpenseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.ItemTouchHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat
import android.Manifest
import android.widget.TextView
import androidx.core.app.ActivityCompat

class Transactions : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var expenseDao: ExpenseDao
    private val REQUEST_CODE_PERMISSION = 101
    private val REQUEST_CODE_READ_STORAGE = 101
    private lateinit var totalTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transactions)

        totalTextView = findViewById(R.id.totalTextView)

        // ---- Navigation Buttons Setup ----
        val btnHome = findViewById<ImageButton>(R.id.imageButton13)
        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeScreen::class.java))
            highlightSelectedButton(R.id.imageButton13)
        }

        val btnTransact = findViewById<ImageButton>(R.id.imageButton14)
        btnTransact.setOnClickListener {
            highlightSelectedButton(R.id.imageButton14)
            // Optional: Show a toast instead of restarting the same screen
        }

        val btnFAB = findViewById<ImageButton>(R.id.imageButton17)
        btnFAB.setOnClickListener {
            startActivity(Intent(this, AddExpense::class.java))
        }

        val btnChart = findViewById<ImageButton>(R.id.imageButton15)
        btnChart.setOnClickListener {
            startActivity(Intent(this, Chart::class.java))
            highlightSelectedButton(R.id.imageButton15)
        }

        val btnMore = findViewById<ImageButton>(R.id.imageButton16)
        btnMore.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            highlightSelectedButton(R.id.imageButton16)
        }

        // Check if permission is granted or not
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // If not, request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_STORAGE
            )
        }
        // ---- RecyclerView Setup ----
        recyclerView = findViewById(R.id.recyclerView) // Make sure your XML uses this ID
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        // ---- Get Database DAO ----
        val db = AppDatabase.getDatabase(applicationContext)
        expenseDao = db.expenseDao()

        // ---- Get Logged-in User ID ----
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)

        // ---- Load Expenses for User ----
        lifecycleScope.launch {
            val expenses = withContext(Dispatchers.IO) {
                expenseDao.getExpensesByUser(userId)
            }

            if (expenses.isNotEmpty()) {
                transactionAdapter = TransactionAdapter(
                    expenses,
                    onItemClick = { selectedExpense ->
                        val intent = Intent(this@Transactions, TransactionDetailsActivity::class.java).apply {
                            putExtra("title", selectedExpense.expense.title)
                            putExtra("description", selectedExpense.expense.description)
                            putExtra("amount", selectedExpense.expense.amount.toString())
                            putExtra("date", selectedExpense.expense.date)
                            putExtra("startTime", selectedExpense.expense.startTime)
                            putExtra("category", selectedExpense.category.category_name)
                            putExtra("photoPath", selectedExpense.expense.photoPath)
                        }
                        startActivity(intent)
                    },

                    onEditClick = { selectedExpense ->
                        val intent = Intent(this@Transactions, EditTransactionActivity::class.java).apply {
                            putExtra("expense_id", selectedExpense.expense.expense_id)
                        }
                        startActivity(intent)
                    },
                    onDeleteClick = { selectedExpense ->
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                expenseDao.deleteExpenseById(selectedExpense.expense.expense_id)

                            }
                            // Refresh the list after deletion
                            val updatedExpenses = withContext(Dispatchers.IO) {
                                expenseDao.getExpensesByUser(userId)
                            }
                            transactionAdapter.updateData(updatedExpenses)
                        }
                    }
                )
                recyclerView.adapter = transactionAdapter

                // ---- Swipe-to-Delete Logic ----
                val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean = false

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val position = viewHolder.adapterPosition
                        val swipedExpense = transactionAdapter.getExpenseAt(position)

                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                expenseDao.deleteExpenseById(swipedExpense.expense.expense_id)
                            }

                            val updatedExpenses = withContext(Dispatchers.IO) {
                                expenseDao.getExpensesByUser(userId)
                            }

                            transactionAdapter.updateData(updatedExpenses)
                        }
                    }
                })
                itemTouchHelper.attachToRecyclerView(recyclerView)
            }
        }
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val dateSpinner = findViewById<Spinner>(R.id.dateSpinner)

        val categoryOptions = listOf("All", "Food", "Transport", "Entertainment","Health","Bills","Shopping","Custom")
        val dateOptions = listOf("All", "Today", "This Week", "This Month")

        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions)
        dateSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dateOptions)

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                filterTransactions(categorySpinner.selectedItem.toString(), dateSpinner.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        dateSpinner.onItemSelectedListener = categorySpinner.onItemSelectedListener
    }
    private fun filterTransactions(category: String, dateRange: String) {
        val userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("USER_ID", -1)

        lifecycleScope.launch {
            val allExpenses = withContext(Dispatchers.IO) {
                expenseDao.getExpensesByUser(userId)
            }

            val filtered = allExpenses.filter { expense ->
                val matchCategory = category == "All" || expense.category.category_name == category

                val now = System.currentTimeMillis()
                val expenseDateString = expense.expense.date

                val expenseDateMillis = if (!expenseDateString.isNullOrBlank()) {
                    try {
                        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(expenseDateString)?.time ?: 0L
                    } catch (e: Exception) {
                        0L // fallback if parse still fails
                    }
                } else {
                    0L
                }

                val matchDate = when (dateRange) {
                    "Today" -> DateUtils.isToday(expenseDateMillis)
                    "This Week" -> now - expenseDateMillis <= 7 * 24 * 60 * 60 * 1000
                    "This Month" -> {
                        val cal = Calendar.getInstance()
                        val currentMonth = cal.get(Calendar.MONTH)
                        cal.timeInMillis = expenseDateMillis
                        cal.get(Calendar.MONTH) == currentMonth
                    }
                    else -> true
                }

                matchCategory && matchDate
            }

            if (::transactionAdapter.isInitialized) {
                transactionAdapter.updateData(filtered)
            }

             // ✅ Calculate and update the total
            val totalAmount = filtered.sumOf { it.expense.amount }
            totalTextView.text = "Total: R${"%.2f".format(totalAmount)}"
        }
    }
    private fun highlightSelectedButton(selectedButtonId: Int) {
        val buttons = listOf(
            findViewById<ImageButton>(R.id.imageButton13),
            findViewById<ImageButton>(R.id.imageButton14),
            findViewById<ImageButton>(R.id.imageButton15),
            findViewById<ImageButton>(R.id.imageButton16)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_READ_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "READ_EXTERNAL_STORAGE permission granted")
            } else {
                Log.d("Permission", "READ_EXTERNAL_STORAGE permission denied")
            }
        }
    }
}



