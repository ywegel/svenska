package de.ywegel.svenska.domain.quiz.strategies

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.stream.Stream

class ComparisonHelpersTest {

    companion object {
        @JvmStatic
        fun compareEndingsTestCases(): Stream<Arguments> = Stream.of(
            Arguments.of("-en -ar -arna", "en ar arna", true),
            Arguments.of("-et - -en", "et en", true),
            Arguments.of("en   ar  arna", "en ar arna", true),
            Arguments.of("-en -ar", "-en -er", false),
            Arguments.of("", "", true),
            Arguments.of("  -en  ", "en", true),
            Arguments.of("-en-ar", "en ar", true),
            Arguments.of("-en--ar", "en ar", true),
            Arguments.of("  ", "", true),
            Arguments.of("-en -", "en", true),
        )
    }

    @ParameterizedTest
    @MethodSource("compareEndingsTestCases")
    fun `given expected and user input, when comparing endings, then returns expected result`(
        expected: String,
        userInput: String,
        expectedResult: Boolean,
    ) {
        // When
        val result = ComparisonHelpers.compareEndings(expected, userInput)

        // Then
        expectThat(result).isEqualTo(expectedResult)
    }
}
