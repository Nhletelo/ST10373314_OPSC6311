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

        db = AppDatabase.getDatabase(this) //  getDatabase(context) function from your RoomDB class
        expenseDao = db.expenseDao()

        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Ask for Camera Permission
        // ✅ Setup camera result
        val cameraProviderResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                val bitmap = it.data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    binding.imgCameraImage.setImageBitmap(bitmap)
                    capturedPhotoPath = saveImageToInternalStorage(bitmap) // ✅ Save path
                } else {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ✅ Launch camera
        binding.imageButton20.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraProviderResult.launch(intent)
        }

        // ✅ File picker
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

            val username = prefs.getString("username", "User")
            val homeIntent = Intent(this, HomeScreen::class.java)
            homeIntent.putExtra("username", username)
            startActivity(homeIntent)

            // Insert the expense asynchronously
            lifecycleScope.launch(Dispatchers.IO) {
                val categoryDao = db.categoryDao()
                var categoryId: Int? = null

                // Handle custom category logic
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

                // Create Expense object
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
                // Insert the expense into the database
                expenseDao.insertExpense(expense)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddExpense, "Expense added", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@AddExpense, Transactions::class.java))
                }
            }
        }

        /*val btnCustomCategory = findViewById<Button>(R.id.button5)
        btnCustomCategory.setOnClickListener {
            val intent = Intent(this, SelectCategory::class.java)
            startActivity(intent)
        }*/

        val btnHome = findViewById<ImageButton>(R.id.imageButton19)
        btnHome.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
        }

        // **Spinner Setup**
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
        btnPickTime.setOnClickListener{
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener{ timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                editTextTime.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this,timeSetListener,cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),true).show()
        }

        editTextDate = findViewById<TextView>(R.id.editTextDate)
        btnShowDatePicker = findViewById<Button>(R.id.btnShowDatePicker)
        btnShowDatePicker.setOnClickListener{
            showDatePicker()
        }
    }

    // ✅ Save image to internal storage
    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, filename)
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
        return file.absolutePath
    }
    private fun showDatePicker(){
        val datePickerDialog = DatePickerDialog(this,{DatePicker, year:Int, monthOfYear:Int, dayofMonth:Int ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year,monthOfYear,dayofMonth)
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            editTextDate.text = formattedDate
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        return monthFormat.format(calendar.time)
    }

    // ✅ Get file name
    private fun getFileName(uri: Uri?): String {
        return uri?.path?.substringAfterLast("/") ?: "Unknown File"
    }
}
