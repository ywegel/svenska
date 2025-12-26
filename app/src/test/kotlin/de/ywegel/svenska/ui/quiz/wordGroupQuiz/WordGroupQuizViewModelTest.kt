@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.quiz.wordGroupQuiz

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.ywegel.svenska.common.streamOf
import de.ywegel.svenska.data.impl.QuizRepositoryFake
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.vocabulary
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.util.stream.Stream
import kotlin.random.Random

class WordGroupQuizViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `Loading state while viewModel is initializing`() = runTest(testDispatcher) {
        // Given
        val vocabulary = vocabulary()
        val repository = QuizRepositoryFake(initialNouns = listOf(vocabulary))

        // When
        val vm = WordGroupQuizViewModel(
            savedStateHandle = SavedStateHandle(),
            quizRepository = repository,
            ioDispatcher = testDispatcher,
        )

        // Then
        vm.uiState.test {
            assertEquals(WordGroupQuizUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(
                WordGroupQuizUiState.QuizItemState(
                    0,
                    1,
                    vocabulary,
                    correctSubgroup = (vocabulary.wordGroup as WordGroup.Noun).subgroup,
                    selectedSubgroup = null,
                    userAnswerCorrect = null,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `Empty state if no vocabularies are provided`() = runTest(testDispatcher) {
        // Given
        val repository = QuizRepositoryFake(initialNouns = emptyList())

        // When
        val vm = WordGroupQuizViewModel(
            savedStateHandle = SavedStateHandle(),
            quizRepository = repository,
            ioDispatcher = testDispatcher,
        )

        // Then
        vm.uiState.test {
            assertEquals(WordGroupQuizUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(WordGroupQuizUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `QuizItemState shuffles words and tracks progress correctly`() = runTest(testDispatcher) {
        // Given
        val nouns = listOf(
            vocabulary(id = 1, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR)),
            vocabulary(id = 2, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.OR)),
            vocabulary(id = 3, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
            vocabulary(id = 4, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.R)),
        )
        val mockedRandom = mockk<Random.Default> {
            every { nextLong() } returns 123
        }
        val expectedShuffle = nouns.shuffled(Random(mockedRandom.nextLong()))
        val repository = QuizRepositoryFake(initialNouns = nouns)

        // When
        val vm = WordGroupQuizViewModel(
            savedStateHandle = SavedStateHandle(),
            quizRepository = repository,
            ioDispatcher = testDispatcher,
            random = mockedRandom,
        )

        vm.uiState.test {
            assertEquals(WordGroupQuizUiState.Loading, awaitItem())
            advanceUntilIdle()

            // Then
            for ((i, expectedVocab) in expectedShuffle.withIndex()) {
                val emittedState = awaitItem() as WordGroupQuizUiState.QuizItemState
                expect {
                    that(emittedState.vocabulary).isEqualTo(expectedVocab)
                    that(emittedState.progress).isEqualTo(i)
                    that(emittedState.progressGoal).isEqualTo(nouns.size)
                }
                vm.next()
                advanceUntilIdle()
            }
            expectThat(awaitItem()).isEqualTo(WordGroupQuizUiState.Completed)
        }
    }

    @Test
    fun `UserInput is correctly evaluated`() = runTest(testDispatcher) {
        // Given
        val nouns = listOf(
            vocabulary(id = 1, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR)),
            vocabulary(id = 2, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR)),
        )
        val repository = QuizRepositoryFake(initialNouns = nouns)

        val vm = WordGroupQuizViewModel(
            savedStateHandle = SavedStateHandle(),
            quizRepository = repository,
            ioDispatcher = testDispatcher,
        )

        vm.uiState.test {
            assertEquals(WordGroupQuizUiState.Loading, awaitItem())
            advanceUntilIdle()
            // First Data shown to User
            val state = awaitItem() as WordGroupQuizUiState.QuizItemState
            expect {
                that(state.selectedSubgroup).isNull()
                that(state.userAnswerCorrect).isNull()
                that(state.correctSubgroup).isEqualTo(WordGroup.NounSubgroup.AR)
            }

            // User selects group
            vm.selectSubgroup(WordGroup.NounSubgroup.OR)
            advanceUntilIdle()
            val state1 = awaitItem() as WordGroupQuizUiState.QuizItemState
            expect {
                that(state1.selectedSubgroup).isEqualTo(WordGroup.NounSubgroup.OR)
                that(state1.userAnswerCorrect).isNull()
            }

            // User checks answer
            vm.check()
            advanceUntilIdle()
            val state2 = awaitItem() as WordGroupQuizUiState.QuizItemState
            expect {
                that(state2.selectedSubgroup).isEqualTo(WordGroup.NounSubgroup.OR)
                that(state2.userAnswerCorrect).isFalse()
            }

            // User continues with next word
            vm.next()
            advanceUntilIdle()
            val state3 = awaitItem() as WordGroupQuizUiState.QuizItemState
            expect {
                that(state3.selectedSubgroup).isNull()
                that(state3.userAnswerCorrect).isNull()
                that(state3.correctSubgroup).isEqualTo(WordGroup.NounSubgroup.AR)
            }

            // User selects group
            vm.selectSubgroup(WordGroup.NounSubgroup.AR)
            advanceUntilIdle()
            val state4 = awaitItem() as WordGroupQuizUiState.QuizItemState
            expect {
                that(state4.selectedSubgroup).isEqualTo(WordGroup.NounSubgroup.AR)
                that(state4.userAnswerCorrect).isNull()
                that(state4.correctSubgroup).isEqualTo(WordGroup.NounSubgroup.AR)
            }

            // User checks answer
            vm.check()
            advanceUntilIdle()
            val state5 = awaitItem() as WordGroupQuizUiState.QuizItemState
            expect {
                that(state5.selectedSubgroup).isEqualTo(WordGroup.NounSubgroup.AR)
                that(state5.userAnswerCorrect).isTrue()
                that(state5.correctSubgroup).isEqualTo(WordGroup.NounSubgroup.AR)
            }
        }
    }

    @Test
    fun `Completed state is reached, once all vocabularies were shown`() = runTest(testDispatcher) {
        // Given
        val vocabulary = vocabulary()
        val repository = QuizRepositoryFake(initialNouns = listOf(vocabulary))

        val vm = WordGroupQuizViewModel(
            savedStateHandle = SavedStateHandle(),
            quizRepository = repository,
            ioDispatcher = testDispatcher,
        )

        vm.uiState.test {
            assertEquals(WordGroupQuizUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(
                WordGroupQuizUiState.QuizItemState(
                    0,
                    1,
                    vocabulary,
                    correctSubgroup = (vocabulary.wordGroup as WordGroup.Noun).subgroup,
                    selectedSubgroup = null,
                    userAnswerCorrect = null,
                ),
                awaitItem(),
            )

            // When
            vm.next()

            // Then
            assertEquals(WordGroupQuizUiState.Completed, awaitItem())
        }
    }

    @ParameterizedTest
    @MethodSource("provideDataForSpecialAndUndefinedTest")
    fun `Special and undefined word groups are treated as the same`(
        vocabulary: Vocabulary,
        selectedSubGroup: WordGroup.NounSubgroup,
    ) = runTest {
        // Given
        val repository = QuizRepositoryFake(initialNouns = listOf(vocabulary))

        val vm = WordGroupQuizViewModel(
            savedStateHandle = SavedStateHandle(),
            quizRepository = repository,
            ioDispatcher = testDispatcher,
        )
        advanceUntilIdle()
        vm.selectSubgroup(selectedSubGroup)

        // When
        vm.check()
        advanceUntilIdle()

        // Then
        vm.uiState.test {
            expectThat(awaitItem()).isEqualTo(
                WordGroupQuizUiState.QuizItemState(
                    0,
                    1,
                    vocabulary,
                    correctSubgroup = (vocabulary.wordGroup as WordGroup.Noun).subgroup,
                    selectedSubgroup = selectedSubGroup,
                    userAnswerCorrect = true,
                ),
            )
        }
    }

    companion object {
        @JvmStatic
        fun provideDataForSpecialAndUndefinedTest(): Stream<Arguments> = streamOf(
            Arguments.of(
                vocabulary(id = 1, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
                WordGroup.NounSubgroup.SPECIAL,
            ),
            Arguments.of(
                vocabulary(id = 1, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED)),
                WordGroup.NounSubgroup.SPECIAL,
            ),
            Arguments.of(
                vocabulary(id = 1, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
                WordGroup.NounSubgroup.UNDEFINED,
            ),
            Arguments.of(
                vocabulary(id = 1, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED)),
                WordGroup.NounSubgroup.UNDEFINED,
            ),
        )
    }
}
