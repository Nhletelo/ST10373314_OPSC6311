package com.example.CoinWatch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.CoinWatch.databinding.ActivitySelectCategoryBinding

class SelectCategory : AppCompatActivity() {
    private lateinit var binding: ActivitySelectCategoryBinding
    private lateinit var selectedColour: ColourObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadColourSpinner()

        binding.imageButton20.setOnClickListener {
            startActivity(Intent(this, AddExpense::class.java))
        }
    }

    private fun loadColourSpinner()
    {
        selectedColour = ColourList().defaultColour

        binding.colourSpinner.apply {
            adapter = ColourSpinnerAdapter(applicationContext, ColourList().basicColours())
            setSelection(ColourList().colourPosition(selectedColour), false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener
            {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
                {
                    selectedColour = ColourList().basicColours()[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?)
                {
                    // Do nothing
                }
            }
        }
    }
}
