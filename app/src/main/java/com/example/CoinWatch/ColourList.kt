package com.example.CoinWatch

class ColourList {
    private val blackHex ="000000"
    private val whiteHex ="FFFFFF"

    val defaultColour: ColourObject
        get() = basicColours().firstOrNull() ?: ColourObject("Black", "000000", "FFFFFF")


    fun colourPosition(colourObject: ColourObject): Int
    {
        for (i in basicColours().indices)
        {
            if(colourObject == basicColours()[i])
                return i
        }
        return 0
    }

    fun basicColours(): List<ColourObject>
    {
        return listOf(ColourObject("Black", blackHex, whiteHex),
            ColourObject("Silver", "COCOCO", blackHex),
            ColourObject("Gray", "808080", whiteHex),
            ColourObject("Maroon", "800000", whiteHex),
            ColourObject("Red", "FF0000", whiteHex),
            ColourObject("Fuchsia", "FF00FF", whiteHex),
            ColourObject("Green", "008000", whiteHex),
            ColourObject("Lime", "00FF00", blackHex),
            ColourObject("Olive", "808000", whiteHex),
            ColourObject("Yellow", "FFFF00", blackHex),
            ColourObject("Navy", "000080", whiteHex),
            ColourObject("Blue", "0000FF", whiteHex),
            ColourObject("Teal", "008080", whiteHex),
            ColourObject("Aqua", "00FFFF", blackHex))
    }
}