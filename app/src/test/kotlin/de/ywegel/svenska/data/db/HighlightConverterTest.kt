package de.ywegel.svenska.data.db

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.stream.Stream

class HighlightConverterTest {
    private val converter = HighlightConverter()

    @ParameterizedTest(name = "fromHighlightRanges: {0} -> {1}")
    @MethodSource("provideRangesToString")
    @DisplayName("Converts ranges to string correctly")
    fun testFromHighlightRanges(ranges: List<Pair<Int, Int>>, expected: String) {
        // When
        val result = converter.fromHighlightRanges(ranges)
        // Then
        expectThat(result).isEqualTo(expected)
    }

    @ParameterizedTest(name = "toHighlightRanges: {0} -> {1}")
    @MethodSource("provideRangesToString")
    @DisplayName("Converts string to ranges correctly")
    fun testToHighlightRanges(expected: List<Pair<Int, Int>>, input: String) {
        // When
        val result = converter.toHighlightRanges(input)
        // Then
        expectThat(result).isEqualTo(expected)
    }

    @ParameterizedTest(name = "Round trip: {0}")
    @MethodSource("provideRangesToString")
    @DisplayName("Round trip conversion preserves ranges")
    fun testRoundTripConversion(ranges: List<Pair<Int, Int>>) {
        // When
        val stringResult = converter.fromHighlightRanges(ranges)
        val roundTripResult = converter.toHighlightRanges(stringResult)
        // Then
        expectThat(roundTripResult).isEqualTo(ranges)
    }

    companion object {
        @JvmStatic
        fun provideRangesToString(): Stream<Arguments> = Stream.of(
            Arguments.of(listOf(Pair(1, 2), Pair(3, 4)), "1:2,3:4"),
            Arguments.of(listOf(Pair(0, 0)), "0:0"),
            Arguments.of(emptyList<Pair<Int, Int>>(), ""),
            Arguments.of(listOf(Pair(-1, -2)), "-1:-2"),
        )
    }
}
