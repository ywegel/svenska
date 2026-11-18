package de.ywegel.svenska.domain.words

import de.ywegel.svenska.common.streamOf
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class EndingsTest {

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

    companion object {
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
    }
}
