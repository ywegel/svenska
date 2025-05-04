package de.ywegel.svenska.bonus

import de.ywegel.svenska.R
import de.ywegel.svenska.ui.bonus.numbers.SwedishNumberConverter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SwedishNumberConverterTest {

    private lateinit var converter: SwedishNumberConverter

    @BeforeEach
    fun setup() {
        converter = SwedishNumberConverter
    }

    @Test
    fun `test single digit numbers`() {
        assertEquals(listOf(R.string.lang_numbers_zero), converter.getSwedishNumberResourceIds(0))
        assertEquals(
            listOf(R.string.lang_numbers_one_extended),
            converter.getSwedishNumberResourceIds(1),
        )
        assertEquals(listOf(R.string.lang_numbers_two), converter.getSwedishNumberResourceIds(2))
    }

    @Test
    fun `test numbers between 21 and 99`() {
        assertEquals(
            listOf(R.string.lang_numbers_twenty, R.string.lang_numbers_one),
            converter.getSwedishNumberResourceIds(21),
        )
        assertEquals(
            listOf(R.string.lang_numbers_thirty, R.string.lang_numbers_three),
            converter.getSwedishNumberResourceIds(33),
        )
    }

    @Test
    fun `test exact hundreds`() {
        assertEquals(
            listOf(R.string.lang_numbers_hundred),
            converter.getSwedishNumberResourceIds(100),
        )
        assertEquals(
            listOf(R.string.lang_numbers_two, R.string.lang_numbers_hundred),
            converter.getSwedishNumberResourceIds(200),
        )
    }

    @Test
    fun `test numbers between 101 and 999`() {
        assertEquals(
            listOf(R.string.lang_numbers_hundred, R.string.lang_numbers_one),
            converter.getSwedishNumberResourceIds(101),
        )
        assertEquals(
            listOf(
                R.string.lang_numbers_two,
                R.string.lang_numbers_hundred,
                R.string.lang_numbers_thirty,
            ),
            converter.getSwedishNumberResourceIds(230),
        )
    }

    @Test
    fun `test negative numbers throw exception`() {
        assertThrows<IllegalArgumentException> {
            converter.getSwedishNumberResourceIds(-1)
        }
    }

    @Test
    fun `test numbers above 1000 throw exception`() {
        assertThrows<IllegalArgumentException> {
            converter.getSwedishNumberResourceIds(1001)
        }
    }
}
