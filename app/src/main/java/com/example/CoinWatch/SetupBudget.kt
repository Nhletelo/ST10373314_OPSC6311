package com.example.CoinWatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.CoinWatch.data.AppDatabase
import com.example.CoinWatch.data.BudgetGoal
import com.example.CoinWatch.data.BudgetGoalDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetupBudget : AppCompatActivity() {
    private lateinit var budgetDao: BudgetGoalDao
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_budget)

        db = AppDatabase.getDatabase(applicationContext)
        budgetDao = db.budgetGoalDao()

        // UI Components
        val btnBack = findViewById<ImageButton>(R.id.imageButton25)
        val btnSave = findViewById<Button>(R.id.btnSaveBudget)

        val spinner = findViewById<Spinner>(R.id.spinner_month)
        val minGoalEditText = findViewById<EditText>(R.id.editTextMinGoal)
        val maxGoalEditText = findViewById<EditText>(R.id.editTextMaxGoal)

        // Back Navigation
        btnBack.setOnClickListener {
            startActivity(Intent(this, HomeScreen::class.java))
            finish()
        }

        btnSave.setOnClickListener {
            val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("USER_ID", -1)

            if (userId == -1) {
                Log.e("SetupBudget", "User ID not found")
                showToast("User ID not found. Please log in again.")
                return@setOnClickListener
            }

            val selectedMonth = spinner.selectedItem?.toString()?.trim()
            val minStr = minGoalEditText.text?.toString()?.trim()
            val maxStr = maxGoalEditText.text?.toString()?.trim()

            if (selectedMonth.isNullOrEmpty() || minStr.isNullOrEmpty() || maxStr.isNullOrEmpty()) {
                showToast("Please fill all fields.")
                return@setOnClickListener
            }

            val minGoal = minStr.toDoubleOrNull()
            val maxGoal = maxStr.toDoubleOrNull()

            if (minGoal == null || maxGoal == null) {
                showToast("Min and Max goal must be valid numbers.")
                return@setOnClickListener
            }

            if (minGoal > maxGoal) {
                showToast("Min goal can't be greater than Max goal.")
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val existingGoal = budgetDao.getGoalByUserAndMonth(userId, selectedMonth)
                    if (existingGoal != null) {
                        withContext(Dispatchers.Main) {
                            showToast("Budget for this month already exists.")
                        }
                        return@launch
                    }

                    val goal = BudgetGoal(
                        user_id = userId,
                        month = selectedMonth,
                        minGoal = minGoal,
                        maxGoal = maxGoal
                    )

                    budgetDao.insertGoal(goal)

                    withContext(Dispatchers.Main) {
                        showToast("Budget saved successfully!")
                    }

                } catch (e: Exception) {
                    Log.e("SetupBudget", "Insert failed: ${e.localizedMessage}", e)
                    withContext(Dispatchers.Main) {
                        showToast("Error saving budget. Please try again.")
                    }
                }
            }
        }
        val btnReset = findViewById<Button>(R.id.button11)

        btnReset.setOnClickListener {
            // Clear the EditText fields
            minGoalEditText.text.clear()
            maxGoalEditText.text.clear()

            // Reset the spinner to the first item
            spinner.setSelection(0)
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this@SetupBudget, message, Toast.LENGTH_SHORT).show()
    }
}
