package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.common.streamOf
import de.ywegel.svenska.data.model.WordGroup
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class FormsTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @MethodSource("endingsCases")
    fun `splitEndings accepts commas and spaces`(text: String, expected: List<String>) {
        expectThat(splitEndings(text)).isEqualTo(expected)
    }

    @ParameterizedTest(name = "{0} from {1} -> {2} + {3}")
    @MethodSource("changeCases")
    fun `splitAtChange separates the shared stem from the changing part`(
        form: String,
        baseWord: String,
        stem: String,
        change: String,
    ) {
        expectThat(form.splitAtChange(baseWord)).isEqualTo(stem to change)
    }

    @ParameterizedTest(name = "{0} {1}, selected {2} -> {3}")
    @MethodSource("suggestionCases")
    fun `suggestWordGroup only suggests complete, known and different groups`(
        word: String,
        endings: List<String>,
        selected: WordGroup?,
        expected: WordGroup?,
    ) {
        // Given
        val formWordGroup = when (selected) {
            null -> WordGroup.Other
            is WordGroup.Noun -> WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED)
            else -> selected
        }

        // When
        val result = suggestWordGroup(FormInputs(word, endings, formWordGroup), selected)

        // Then
        expectThat(result).isEqualTo(expected)
    }

    companion object {
        private val ar = WordGroup.Noun(WordGroup.NounSubgroup.AR)

        @JvmStatic
        fun endingsCases() = streamOf(
            Arguments.of("-er, -de, -t", listOf("-er", "-de", "-t")),
            Arguments.of(" -er  -de -t ", listOf("-er", "-de", "-t")),
            Arguments.of("-et,-,-en", listOf("-et", "-", "-en")),
            Arguments.of("", emptyList<String>()),
        )

        @JvmStatic
        fun changeCases() = streamOf(
            Arguments.of("drömmen", "dröm", "dröm", "men"),
            Arguments.of("kände", "känna", "kän", "de"),
            Arguments.of("hus", "hus", "hus", ""),
            Arguments.of("tog", "ta", "", "tog"),
        )

        @JvmStatic
        fun suggestionCases() = streamOf(
            Arguments.of("dröm", listOf("-men", "-mar", "-marna"), null, ar),
            Arguments.of("dröm", listOf("-men", "-mar", "-marna"), ar, null),
            // The second of three noun endings is no complete adjective
            Arguments.of("dröm", listOf("-men", "-mar"), WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED), null),
            Arguments.of("dröm", listOf("-men"), null, null),
            Arguments.of("", listOf("-men", "-mar", "-marna"), null, null),
        )
    }
}
