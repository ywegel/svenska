package de.ywegel.svenska.data.preferences

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly

class LastSearchExtensionsTest {

    @Test
    fun `adds new element to empty deque`() {
        val result = ArrayDeque<String>().addedToFrontAndLimited("a", 3)
        expectThat(result.toList()).containsExactly("a")
    }

    @Test
    fun `adds new element to front if not present`() {
        val result = ArrayDeque(listOf("b", "c")).addedToFrontAndLimited("a", 3)
        expectThat(result.toList()).containsExactly(listOf("a", "b", "c"))
    }

    @Test
    fun `moves existing element to front`() {
        val result = ArrayDeque(listOf("a", "b")).addedToFrontAndLimited("b", 3)
        expectThat(result.toList()).containsExactly(listOf("b", "a"))
    }

    @Test
    fun `removes oldest element when limit exceeded`() {
        val result = ArrayDeque(listOf("a", "b", "c")).addedToFrontAndLimited("d", 3)
        expectThat(result.toList()).containsExactly(listOf("d", "a", "b"))
    }

    @Test
    fun `existing element moved to front without exceeding limit`() {
        val result = ArrayDeque(listOf("a", "b", "c")).addedToFrontAndLimited("b", 3)
        expectThat(result.toList()).containsExactly(listOf("b", "a", "c"))
    }

    @Test
    fun `leaves the original deque untouched`() {
        val original = ArrayDeque(listOf("a", "b"))
        original.addedToFrontAndLimited("c", 3)
        expectThat(original.toList()).containsExactly(listOf("a", "b"))
    }
}
