package de.ywegel.svenska.ui.quiz

import de.ywegel.svenska.domain.quiz.model.QuizMode
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import de.ywegel.svenska.ui.quiz.configuration.ConfigurationState
import de.ywegel.svenska.ui.quiz.configuration.QuizConfigurationViewModel
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.stream.Stream

class QuizConfigurationViewModelTest {

    @ParameterizedTest
    @MethodSource("provideGenerateNavigationArgsTestData")
    fun `generateNavigationArgs returns the correct QuizMode`(data: Pair<ConfigurationState, QuizMode>) {
        val viewModel = QuizConfigurationViewModel(data.first)

        expectThat(viewModel.generateNavigationArgs()).isEqualTo(data.second)
    }

    companion object {
        @JvmStatic
        private fun provideGenerateNavigationArgsTestData(): Stream<Pair<ConfigurationState, QuizMode?>> = Stream.of(
            Pair(
                ConfigurationState(selectedMode = TranslateMode.Random, withEndings = false, onlyEndings = false),
                QuizMode.Translate(mode = TranslateMode.Random, shuffleWords = true),
            ),
            Pair(
                ConfigurationState(
                    selectedMode = TranslateMode.SwedishToNative,
                    withEndings = false,
                    onlyEndings = false,
                ),
                QuizMode.Translate(mode = TranslateMode.SwedishToNative, shuffleWords = true),
            ),
            Pair(
                ConfigurationState(selectedMode = null, withEndings = false, onlyEndings = false),
                null,
            ),
            Pair(
                ConfigurationState(selectedMode = TranslateMode.Random, withEndings = true, onlyEndings = false),
                QuizMode.TranslateWithEndings(mode = TranslateMode.Random, shuffleWords = true),
            ),
            Pair(
                ConfigurationState(
                    selectedMode = TranslateMode.SwedishToNative,
                    withEndings = true,
                    onlyEndings = false,
                ),
                QuizMode.TranslateWithEndings(mode = TranslateMode.SwedishToNative, shuffleWords = true),
            ),
            Pair(
                ConfigurationState(selectedMode = null, withEndings = true, onlyEndings = false),
                null,
            ),
            Pair(
                ConfigurationState(
                    selectedMode = TranslateMode.SwedishToNative,
                    withEndings = true,
                    onlyEndings = true,
                ),
                QuizMode.OnlyEndings(shuffleWords = true),
            ),
            Pair(
                ConfigurationState(selectedMode = TranslateMode.Random, withEndings = true, onlyEndings = true),
                QuizMode.OnlyEndings(shuffleWords = true),
            ),
            Pair(
                ConfigurationState(
                    selectedMode = TranslateMode.SwedishToNative,
                    withEndings = false,
                    onlyEndings = true,
                ),
                QuizMode.OnlyEndings(shuffleWords = true),
            ),
            Pair(
                ConfigurationState(selectedMode = null, withEndings = false, onlyEndings = true),
                QuizMode.OnlyEndings(shuffleWords = true),
            ),
            Pair(
                ConfigurationState(
                    selectedMode = TranslateMode.Random,
                    withEndings = false,
                    onlyEndings = false,
                    shuffleWords = false,
                ),
                QuizMode.Translate(mode = TranslateMode.Random, shuffleWords = false),
            ),
        )
    }
}
