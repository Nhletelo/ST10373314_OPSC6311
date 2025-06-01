package com.example.CoinWatch

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.CoinWatch.data.AppDatabase
import com.example.CoinWatch.data.Category
import com.example.CoinWatch.data.Expense
import com.example.CoinWatch.data.ExpenseDao
import com.example.CoinWatch.databinding.ActivityAddExpenseBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpense : AppCompatActivity() {

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var fileNameDisplay: EditText
    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var db: AppDatabase //  RoomDB class
    private lateinit var expenseDao: ExpenseDao
    private var capturedPhotoPath: String? = null
    lateinit var editTextDate: TextView
    lateinit var btnShowDatePicker: Button
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_expense)

        db = AppDatabase.getDatabase(this)
        expenseDao = db.expenseDao()

        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Camera setup and launcher
        val cameraProviderResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                val bitmap = it.data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    binding.imgCameraImage.setImageBitmap(bitmap)
                    capturedPhotoPath = saveImageToInternalStorage(bitmap)
                } else {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.imageButton20.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraProviderResult.launch(intent)
        }

        // File picker setup
        val btnAttach = findViewById<ImageButton>(R.id.imageButton23)
        fileNameDisplay = findViewById(R.id.textView19)
        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val fileUri: Uri? = result.data?.data
                val fileName = getFileName(fileUri)
                fileNameDisplay.setText(fileName)
            }
        }

        btnAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            filePickerLauncher.launch(intent)
        }

        // Add Expense button click handler
        binding.button6.setOnClickListener {
            val title = binding.editTextText3.text.toString()
            val description = binding.editTextText4.text.toString()
            val amount = binding.editTextNumberDecimal.text.toString().toDoubleOrNull() ?: 0.0
            val date = binding.editTextDate.text.toString()
            val time = binding.editTextTime.text.toString()
            val fileName = binding.textView19.text.toString()
            val selectedCategory = binding.spinner2.selectedItem?.toString() ?: "Other"
            val customCategoryText = findViewById<EditText>(R.id.editTextCustomCategory).text.toString().trim()

            if (date.isBlank()) {
                Toast.makeText(this, "Please pick a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (time.isBlank()) {
                Toast.makeText(this, "Please pick a time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val userId = prefs.getInt("USER_ID", -1)

            if (userId == -1) {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get the month from the selected date (important!)
            val selectedMonth = getMonthFromDateString(date)

            lifecycleScope.launch(Dispatchers.IO) {
                val budgetGoalDao = db.budgetGoalDao()

                // Fetch the user's budget for the selected month
                val userBudget = budgetGoalDao.getBudgetForUserAndMonth(userId, selectedMonth)

                if (userBudget == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddExpense, "Please set your budget before adding expenses", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                // Calculate total expenses for the user for the selected month
                val totalExpenses = expenseDao.getTotalExpensesForUserAndMonth(userId, selectedMonth) ?: 0.0

                // Check if adding this expense exceeds the budget
                if ((totalExpenses + amount) > userBudget.maxGoal) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddExpense, "Expense exceeds your monthly budget", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                // Insert expense only if all checks pass
                val categoryDao = db.categoryDao()
                var categoryId: Int? = null

                if (customCategoryText.isNotEmpty()) {
                    val existing = categoryDao.getCategoryIdByNameAndUserId(customCategoryText, userId)
                    categoryId = existing ?: categoryDao.insert(
                        Category(category_name = customCategoryText, user_id = userId)
                    ).toInt()
                } else {
                    categoryId = categoryDao.getCategoryIdByNameAndUserId(selectedCategory, userId)
                    if (categoryId == null) {
                        categoryId = categoryDao.insert(
                            Category(category_name = selectedCategory, user_id = userId)
                        ).toInt()
                    }
                }

                val expense = Expense(
                    title = title,
                    description = description,
                    amount = amount,
                    date = date,
                    startTime = time,
                    photoPath = capturedPhotoPath,
                    filePath = if (fileName.isEmpty()) null else fileName,
                    category_id = categoryId,
                    user_id = userId
                )

                expenseDao.insertExpense(expense)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddExpense, "Expense added", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@AddExpense, Transactions::class.java))
                }
            }
        }

        val btnHome = findViewById<ImageButton>(R.id.imageButton19)
        btnHome.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
        }

        // Spinner Setup
        val spinner: Spinner = findViewById(R.id.spinner2)
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this, R.array.expense_categories, android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position).toString()
                Toast.makeText(applicationContext, "Selected: $selectedCategory", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val btnPickTime = findViewById<Button>(R.id.btnPickTime)
        val editTextTime = findViewById<TextView>(R.id.editTextTime)
        btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                editTextTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        editTextDate = findViewById<TextView>(R.id.editTextDate)
        btnShowDatePicker = findViewById<Button>(R.id.btnShowDatePicker)
        btnShowDatePicker.setOnClickListener {
            showDatePicker()
        }
    }

    // Helper: get month name from full date string (e.g., "2025/06/01" -> "June")
    private fun getMonthFromDateString(dateStr: String): String {
        return try {
            val sdfInput = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val date = sdfInput.parse(dateStr)
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            monthFormat.format(date ?: Calendar.getInstance().time)
        } catch (e: Exception) {
            getCurrentMonth() // fallback current month
        }
    }

    // Save image to internal storage
    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, filename)
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
        return file.absolutePath
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, monthOfYear, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            editTextDate.text = formattedDate
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun getCurrentMonth(): String {
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        return monthFormat.format(Calendar.getInstance().time)
    }

    // Helper to get file name from Uri
    private fun getFileName(uri: Uri?): String {
        uri ?: return ""
        var result = ""
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    result = cursor.getString(idx)
                }
            }
        }
        if (result.isEmpty()) {
            result = uri.lastPathSegment ?: ""
        }
        return result
    }
}
