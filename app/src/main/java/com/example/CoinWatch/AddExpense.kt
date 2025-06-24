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
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class AddExpense : AppCompatActivity() {

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var fileNameDisplay: EditText
    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var db: AppDatabase
    private lateinit var expenseDao: ExpenseDao
    private var capturedPhotoPath: String? = null
    private var uploadedFilePath: String? = null
    lateinit var editTextDate: TextView
    lateinit var btnShowDatePicker: Button
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        expenseDao = db.expenseDao()

        // Handle camera image
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
                fileUri?.let {
                    val fileName = getFileName(it)
                    fileNameDisplay.setText(fileName)
                    uploadedFilePath = saveSelectedFileToInternalStorage(it, fileName)
                }
            }
        }

        btnAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            filePickerLauncher.launch(intent)
        }

        val customCategoryEditText = findViewById<EditText>(R.id.editTextCustomCategory)
        customCategoryEditText.visibility = View.GONE // Hide initially

        // Spinner setup
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

                // Show custom category input if "Other" selected
                if (selectedCategory == "Other") {
                    customCategoryEditText.visibility = View.VISIBLE
                } else {
                    customCategoryEditText.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.button6.setOnClickListener {
            val title = binding.editTextText3.text.toString()
            val description = binding.editTextText4.text.toString()
            val amount = binding.editTextNumberDecimal.text.toString().toDoubleOrNull() ?: 0.0
            val date = binding.editTextDate.text.toString()
            val time = binding.editTextTime.text.toString()
            val selectedCategory = binding.spinner2.selectedItem?.toString() ?: "Other"
            val customCategoryText = customCategoryEditText.text.toString().trim()

            if (date.isBlank() || time.isBlank()) {
                Toast.makeText(this, "Please pick date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val userId = prefs.getInt("USER_ID", -1)

            if (userId == -1) {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedMonth = getMonthFromDateString(date)

            lifecycleScope.launch(Dispatchers.IO) {
                val budgetGoalDao = db.budgetGoalDao()
                val userBudget = budgetGoalDao.getBudgetForUserAndMonth(userId, selectedMonth)

                if (userBudget == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddExpense, "Please set your budget first", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val totalExpenses = expenseDao.getTotalExpensesForUserAndMonth(userId, selectedMonth) ?: 0.0
                if ((totalExpenses + amount) > userBudget.maxGoal) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddExpense, "Expense exceeds your budget", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val categoryDao = db.categoryDao()

                // Use custom category only if "Other" selected and text entered, else spinner category
                val categoryName = if (selectedCategory == "Other" && customCategoryText.isNotEmpty()) {
                    customCategoryText
                } else {
                    selectedCategory
                }

                val existing = categoryDao.getCategoryIdByNameAndUserId(categoryName, userId)
                val categoryId = existing ?: categoryDao.insert(
                    Category(category_name = categoryName, user_id = userId)
                ).toInt()

                val expense = Expense(
                    title = title,
                    description = description,
                    amount = amount,
                    date = date,
                    startTime = time,
                    photoPath = capturedPhotoPath,
                    filePath = uploadedFilePath,
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

        findViewById<ImageButton>(R.id.imageButton19).setOnClickListener {
            startActivity(Intent(this, HomeScreen::class.java))
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

        editTextDate = findViewById(R.id.editTextDate)
        btnShowDatePicker = findViewById(R.id.btnShowDatePicker)
        btnShowDatePicker.setOnClickListener {
            showDatePicker()
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, filename)
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
        return file.absolutePath
    }

    private fun saveSelectedFileToInternalStorage(uri: Uri, fileName: String): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val file = File(filesDir, "UPLOAD_${System.currentTimeMillis()}_$fileName")
            FileOutputStream(file).use { output ->
                inputStream?.copyTo(output)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, day)
            val formatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(selectedDate.time)
            editTextDate.text = formatted
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun getMonthFromDateString(dateStr: String): String {
        return try {
            val sdfInput = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val date = sdfInput.parse(dateStr)
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            monthFormat.format(date ?: Calendar.getInstance().time)
        } catch (e: Exception) {
            getCurrentMonth()
        }
    }

    private fun getCurrentMonth(): String {
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        return monthFormat.format(Calendar.getInstance().time)
    }

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
            result = uri.lastPathSegment ?: "file"
        }
        return result
    }
}
