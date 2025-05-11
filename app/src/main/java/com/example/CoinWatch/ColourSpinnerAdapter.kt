package com.example.CoinWatch

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class ColourSpinnerAdapter(context: Context, list: List<ColourObject>) :
    ArrayAdapter<ColourObject>(context, 0, list) {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = layoutInflater.inflate(R.layout.colour_spinner_bg, null, true)
        return populateView(view, position)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: layoutInflater.inflate(R.layout.colour_spinner_item, parent, false)
        return populateView(view, position)
    }

    private fun populateView(view: View, position: Int): View {
        val colourObject: ColourObject = getItem(position) ?: return view

        val colourNameItem = view.findViewById<TextView>(R.id.colourName)
        val colourHexItem = view.findViewById<TextView>(R.id.colourHex)
        val colourNameBg = view.findViewById<TextView>(R.id.colourNameBg)
        val colourBlob = view.findViewById<View>(R.id.colourBlob)

        // Setting values
        colourNameItem?.text = colourObject.name
        colourHexItem?.text = colourObject.hexHash
        colourNameBg?.text = colourObject.name
        colourNameBg?.setTextColor(parseColorSafely(colourObject.contrastHexHash))

        // Set blob background color safely
        colourBlob?.background?.setTint(parseColorSafely(colourObject.hexHash))

        return view
    }

    // Helper function to avoid crashes when parsing colors
    private fun parseColorSafely(hex: String): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: IllegalArgumentException) {
            Color.BLACK // Default fallback color
        }
    }
}
