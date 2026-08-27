package de.ywegel.svenska.data.preferences

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly

class LastSearchExtensionsTest {

    @Test
    fun `adds new element to empty list`() {
        val result = emptyList<String>().addedToFrontAndLimited("a", 3)
        expectThat(result).containsExactly("a")
    }

    @Test
    fun `adds new element to front if not present`() {
        val result = listOf("b", "c").addedToFrontAndLimited("a", 3)
        expectThat(result).containsExactly(listOf("a", "b", "c"))
    }

    @Test
    fun `moves existing element to front`() {
        val result = listOf("a", "b").addedToFrontAndLimited("b", 3)
        expectThat(result).containsExactly(listOf("b", "a"))
    }

    @Test
    fun `removes oldest element when limit exceeded`() {
        val result = listOf("a", "b", "c").addedToFrontAndLimited("d", 3)
        expectThat(result).containsExactly(listOf("d", "a", "b"))
    }

    @Test
    fun `existing element moved to front without exceeding limit`() {
        val result = listOf("a", "b", "c").addedToFrontAndLimited("b", 3)
        expectThat(result).containsExactly(listOf("b", "a", "c"))
    }

    @Test
    fun `leaves the original list untouched`() {
        val original = listOf("a", "b")
        original.addedToFrontAndLimited("c", 3)
        expectThat(original).containsExactly(listOf("a", "b"))
    }
}
