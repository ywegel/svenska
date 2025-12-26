@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.quiz.wordGroupQuiz

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.ywegel.svenska.data.impl.QuizRepositoryFake
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
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue
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
    fun `Completed state is reached, once all vocabularies where shown`() = runTest(testDispatcher) {
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
}
