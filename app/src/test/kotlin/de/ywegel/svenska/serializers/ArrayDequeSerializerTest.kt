package de.ywegel.svenska.serializers

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ArrayDequeSerializerTest {
    @Test
    fun `Serialize and deserialize ArrayDeque`() {
        val deque = ArrayDeque(listOf("a", "b", "c"))
        val json = Json.encodeToString(ArrayDequeSerializer, deque)
        val decoded = Json.decodeFromString(ArrayDequeSerializer, json)
        expectThat(decoded).isEqualTo(deque)
    }

    @Test
    fun `Serialize and deserialize empty ArrayDeque`() {
        val deque = ArrayDeque<String>()
        val json = Json.encodeToString(ArrayDequeSerializer, deque)
        val decoded = Json.decodeFromString(ArrayDequeSerializer, json)
        expectThat(decoded).isEqualTo(deque)
    }
}
