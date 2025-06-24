package de.ywegel.svenska.data.preferences

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly

class LastSearchExtensionsTest {

    @Test
    fun `adds new element to empty deque`() {
        val deque = ArrayDeque<String>()
        deque.addToFrontAndLimit("a", 3)
        expectThat(deque.toList()).containsExactly("a")
    }

    @Test
    fun `adds new element to front if not present`() {
        val deque = ArrayDeque(listOf("b", "c"))
        deque.addToFrontAndLimit("a", 3)
        expectThat(deque.toList()).containsExactly(listOf("a", "b", "c"))
    }

    @Test
    fun `moves existing element to front`() {
        val deque = ArrayDeque(listOf("a", "b"))
        deque.addToFrontAndLimit("b", 3)
        expectThat(deque.toList()).containsExactly(listOf("b", "a"))
    }

    @Test
    fun `removes oldest element when limit exceeded`() {
        val deque = ArrayDeque(listOf("a", "b", "c"))
        deque.addToFrontAndLimit("d", 3)
        expectThat(deque.toList()).containsExactly(listOf("d", "a", "b"))
    }

    @Test
    fun `existing element moved to front without exceeding limit`() {
        val deque = ArrayDeque(listOf("a", "b", "c"))
        deque.addToFrontAndLimit("b", 3)
        expectThat(deque.toList()).containsExactly(listOf("b", "a", "c"))
    }
}
