package de.ywegel.svenska.domain.words

import de.ywegel.svenska.common.streamOf
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class PhrasePartsTest {
    @ParameterizedTest(name = "\"{0}\", verb: {1}")
    @MethodSource("splitPhraseCases")
    fun `splitPhrase finds the changing word`(phrase: String, isVerb: Boolean, expected: PhraseParts) {
        // When
        val result = splitPhrase(phrase, isVerb)

        // Then
        expectThat(result).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        fun splitPhraseCases() = streamOf(
            Arguments.of("gifta sig", true, PhraseParts(prefix = "", word = "gifta", suffix = "sig")),
            Arguments.of("ta en dusch", true, PhraseParts(prefix = "", word = "ta", suffix = "en dusch")),
            Arguments.of("tro", true, PhraseParts(prefix = "", word = "tro", suffix = "")),
            Arguments.of("högsta dröm", false, PhraseParts(prefix = "högsta", word = "dröm", suffix = "")),
            Arguments.of("en  hel  del bil", false, PhraseParts(prefix = "en  hel  del", word = "bil", suffix = "")),
            Arguments.of("bil", false, PhraseParts(prefix = "", word = "bil", suffix = "")),
            Arguments.of("", false, PhraseParts(prefix = "", word = "", suffix = "")),
        )
    }
}
