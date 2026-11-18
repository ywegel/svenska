package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.common.streamOf
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

class WordInputTest {

    @ParameterizedTest(name = "\"{0}\" as {1}, chosen {2}: {3} | {4} | {5}")
    @MethodSource("splitCases")
    fun `parse splits the input into prefix, changing word and suffix`(
        text: String,
        group: ViewWordGroup?,
        chosenIndex: Int?,
        prefix: String,
        word: String,
        suffix: String,
    ) {
        // When
        val result = WordInput.parse(text, group, chosenIndex)

        // Then
        expectThat(result) {
            get { this.prefix }.isEqualTo(prefix)
            get { this.word }.isEqualTo(word)
            get { this.suffix }.isEqualTo(suffix)
        }
    }

    @ParameterizedTest(name = "\"{0}\" as {1} has the article {2}")
    @MethodSource("articleCases")
    fun `parse only removes the article that fits the word group`(
        text: String,
        group: ViewWordGroup?,
        article: String?,
    ) {
        // When
        val result = WordInput.parse(text, group, chosenIndex = null)

        // Then
        expectThat(result.article).isEqualTo(article)
    }

    @ParameterizedTest
    @MethodSource("articleGenderCases")
    fun `articleGender maps the article to its gender`(text: String, group: ViewWordGroup, gender: Gender?) {
        // When
        val result = WordInput.parse(text, group, chosenIndex = null)

        // Then
        expectThat(result.articleGender).isEqualTo(gender)
    }

    @Test
    fun `highlights are relative to the changing word`() {
        // When
        val result = WordInput.parse("högsta dr*ö*m", ViewWordGroup.Noun, chosenIndex = null)

        // Then
        expectThat(result) {
            get { wordHighlights }.isEqualTo(listOf(2 to 3))
            get { hasHighlightError }.isFalse()
        }
    }

    @ParameterizedTest(name = "\"{0}\" has a highlight error")
    @MethodSource("highlightErrorCases")
    fun `stars outside the changing word or unbalanced stars are an error`(text: String) {
        // When
        val result = WordInput.parse(text, ViewWordGroup.Verb, chosenIndex = null)

        // Then
        expectThat(result.hasHighlightError).isTrue()
    }

    @Test
    fun `phrases under other keep the whole text as word`() {
        // When
        val result = WordInput.parse("  hur m*å*r du?", ViewWordGroup.Other, chosenIndex = null)

        // Then
        expectThat(result) {
            get { word }.isEqualTo("hur mår du?")
            get { wordHighlights }.isEqualTo(listOf(5 to 6))
            get { changingIndex }.isNull()
        }
    }

    companion object {
        @JvmStatic
        fun splitCases() = streamOf(
            // Without a choice, the same word as in the importer changes
            Arguments.of("känna sig", ViewWordGroup.Verb, null, "", "känna", "sig"),
            Arguments.of("högsta dröm", ViewWordGroup.Noun, null, "högsta", "dröm", ""),
            Arguments.of("högsta dröm", null, null, "högsta", "dröm", ""),
            // The user's choice wins
            Arguments.of("ta en dusch", ViewWordGroup.Verb, 2, "ta en", "dusch", ""),
            // A choice that no longer exists falls back to the default
            Arguments.of("bil", ViewWordGroup.Noun, 3, "", "bil", ""),
            // The article is no part of the prefix
            Arguments.of("att  känna   sig", ViewWordGroup.Verb, null, "", "känna", "sig"),
            Arguments.of("", ViewWordGroup.Noun, null, "", "", ""),
        )

        @JvmStatic
        fun articleCases() = streamOf(
            Arguments.of("ett hus", ViewWordGroup.Noun, "ett"),
            Arguments.of("En bil", ViewWordGroup.Noun, "en"),
            Arguments.of("att vara", ViewWordGroup.Verb, "att"),
            Arguments.of("en", ViewWordGroup.Noun, null),
            Arguments.of("att vara", ViewWordGroup.Noun, null),
            Arguments.of("en gång", ViewWordGroup.Other, null),
            Arguments.of("en gång", null, null),
        )

        @JvmStatic
        fun articleGenderCases(): List<Arguments> = listOf(
            argumentSet("en is utrum", "en bil", ViewWordGroup.Noun, Gender.Ultra),
            argumentSet("ett is neutrum", "ett hus", ViewWordGroup.Noun, Gender.Neutra),
            argumentSet("att has no gender", "att vara", ViewWordGroup.Verb, null),
        )

        @JvmStatic
        fun highlightErrorCases() = streamOf("känna s*i*g", "k*änna", "*att* känna")
    }
}
