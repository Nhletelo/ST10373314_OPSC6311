package com.example.CoinWatch

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.CoinWatch.data.ExpenseWithCategory
import java.io.File

class TransactionAdapter(
    private var expenses: List<ExpenseWithCategory>,
    private val onItemClick: (ExpenseWithCategory) -> Unit,
    private val onEditClick: (ExpenseWithCategory) -> Unit,
    private val onDeleteClick: (ExpenseWithCategory) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.txtTitle)
        private val description: TextView = itemView.findViewById(R.id.txtDescription)
        private val amount: TextView = itemView.findViewById(R.id.txtAmount)
        private val dateTime: TextView = itemView.findViewById(R.id.txtDateTime)
        private val category: TextView = itemView.findViewById(R.id.txtCategory)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.editImageButton)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.deleteImageButton)
        private val imageView: ImageView = itemView.findViewById(R.id.imgExpense)

        fun bind(expense: ExpenseWithCategory) {
            val context = itemView.context
            val expenseData = expense.expense

            title.text = expenseData.title
            description.text = expenseData.description
            amount.text = "R${expenseData.amount}"
            dateTime.text = "${expenseData.date} at ${expenseData.startTime}"
            category.text = expense.category.category_name

            // Click listeners
            itemView.setOnClickListener { onItemClick(expense) }
            btnEdit.setOnClickListener { onEditClick(expense) }
            btnDelete.setOnClickListener { onDeleteClick(expense) }

            // Load image if permission granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                loadImage(expenseData.photoPath)
            } else {
                Log.w("TransactionAdapter", "READ_EXTERNAL_STORAGE permission not granted.")
                imageView.setImageResource(R.drawable.error_image)
            }
        }

        private fun loadImage(imagePath: String?) {
            if (!imagePath.isNullOrBlank()) {
                val file = File(imagePath)

                // 👇 Add these logs to help debug
                Log.d("AdapterDebug", "Checking image at path: $imagePath")
                Log.d("AdapterDebug", "Does file exist? ${file.exists()}")

                if (file.exists()) {
                    Glide.with(itemView.context)
                        .load(file)
                        .placeholder(R.drawable.loading_image)
                        .error(R.drawable.default_image)
                        .into(imageView)
                } else {
                    Log.e("ImageLoadError", "File does not exist: $imagePath")
                    imageView.setImageResource(R.drawable.default_image)
                }
            } else {
                Log.d("TransactionAdapter", "PhotoPath is null or blank.")
                imageView.setImageResource(R.drawable.default_image)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    fun updateData(newData: List<ExpenseWithCategory>) {
        expenses = newData
        notifyDataSetChanged()
    }

    fun getExpenseAt(position: Int): ExpenseWithCategory = expenses[position]
}
