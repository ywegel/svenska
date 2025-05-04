@file:Suppress("detekt:MagicNumber", "detekt:CyclomaticComplexMethod")

package de.ywegel.svenska.ui.bonus.numbers

import android.content.Context
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.bonus.numbers.SwedishNumberConverter.getSwedishNumberResourceIds

object SwedishNumberConverter {
    /**
     * Returns a list of string resource IDs that should be concatenated to form the number
     */
    fun getSwedishNumberResourceIds(number: Int): List<Int> {
        return if (number == 1) {
            listOf(R.string.lang_numbers_one_extended)
        } else {
            internalGetSwedishNumberResourceIds(number)
        }
    }

    private fun internalGetSwedishNumberResourceIds(number: Int): List<Int> {
        require(number in 0..999)

        return when {
            // Direct translations for 0-20
            number == 1 -> listOf(R.string.lang_numbers_one)

            number <= 20 -> listOf(getDirectResourceId(number))

            // Numbers 21-99
            number < 100 -> {
                val tens = (number / 10) * 10
                val ones = number % 10
                if (ones == 0) {
                    // Just return the tens
                    listOf(getDirectResourceId(tens))
                } else {
                    // Combine tens and ones (e.g., tjugoett)
                    listOf(getDirectResourceId(tens), getDirectResourceId(ones))
                }
            }

            // 100-1000
            else -> {
                val hundreds = number / 100
                val remainder = number % 100

                when {
                    remainder == 0 -> {
                        if (hundreds == 1) {
                            listOf(R.string.lang_numbers_hundred)
                        } else {
                            listOf(getDirectResourceId(hundreds), R.string.lang_numbers_hundred)
                        }
                    }

                    else -> {
                        if (hundreds == 1) {
                            listOf(R.string.lang_numbers_hundred) + internalGetSwedishNumberResourceIds(
                                remainder,
                            )
                        } else {
                            listOf(getDirectResourceId(hundreds), R.string.lang_numbers_hundred) +
                                internalGetSwedishNumberResourceIds(remainder)
                        }
                    }
                }
            }
        }
    }

    private fun getDirectResourceId(number: Int): Int {
        return when (number) {
            0 -> R.string.lang_numbers_zero
            1 -> R.string.lang_numbers_one
            2 -> R.string.lang_numbers_two
            3 -> R.string.lang_numbers_three
            4 -> R.string.lang_numbers_four
            5 -> R.string.lang_numbers_five
            6 -> R.string.lang_numbers_six
            7 -> R.string.lang_numbers_seven
            8 -> R.string.lang_numbers_eight
            9 -> R.string.lang_numbers_nine
            10 -> R.string.lang_numbers_ten
            11 -> R.string.lang_numbers_eleven
            12 -> R.string.lang_numbers_twelve
            13 -> R.string.lang_numbers_thirteen
            14 -> R.string.lang_numbers_fourteen
            15 -> R.string.lang_numbers_fifteen
            16 -> R.string.lang_numbers_sixteen
            17 -> R.string.lang_numbers_seventeen
            18 -> R.string.lang_numbers_eighteen
            19 -> R.string.lang_numbers_nineteen
            20 -> R.string.lang_numbers_twenty
            30 -> R.string.lang_numbers_thirty
            40 -> R.string.lang_numbers_forty
            50 -> R.string.lang_numbers_fifty
            60 -> R.string.lang_numbers_sixty
            70 -> R.string.lang_numbers_seventy
            80 -> R.string.lang_numbers_eighty
            90 -> R.string.lang_numbers_ninety
            else -> throw IllegalArgumentException("No direct translation for number $number")
        }
    }
}

// Extension function to make it easy to get the final string
fun Context.getSwedishNumber(number: Int): String {
    return getSwedishNumberResourceIds(number)
        .joinToString(separator = "") { getString(it) }
}
