package de.ywegel.svenska.data.preferences

import de.ywegel.svenska.jsonConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArrayDequeSerializerTest {

    @Test
    fun `serialization roundtrip preserves ArrayDeque`() {
        val original = ArrayDeque(listOf("a", "b", "c"))
        val json = jsonConfig.encodeToString(ArrayDequeSerializer, original)
        val decoded = jsonConfig.decodeFromString(ArrayDequeSerializer, json)
        assertEquals(original, decoded)
    }

    @Test
    fun `empty ArrayDeque serializes and deserializes correctly`() {
        val original = ArrayDeque<String>()
        val json = jsonConfig.encodeToString(ArrayDequeSerializer, original)
        val decoded = jsonConfig.decodeFromString(ArrayDequeSerializer, json)
        assertTrue(decoded.isEmpty())
    }

    @Test
    fun `order of elements is preserved`() {
        val deque = ArrayDeque(listOf("first", "second", "third"))
        val result = jsonConfig.decodeFromString(
            ArrayDequeSerializer,
            jsonConfig.encodeToString(ArrayDequeSerializer, deque),
        )
        assertEquals(listOf("first", "second", "third"), result.toList())
    }
}
