@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.quiz.wordGroupQuiz

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.testing.viewModelScenario
import de.ywegel.svenska.RobolectricTest
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotEqualTo
import strikt.assertions.isTrue
import kotlin.random.Random

class WordGroupQuizViewModelProcessDeathTest : RobolectricTest() {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun cleanUp() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `Process death restores ui state`() = runTest(testDispatcher) {
        // Given
        val repo = QuizRepositoryFake(
            initialNouns = listOf(
                vocabulary(id = 1, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR)),
                vocabulary(id = 2, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.OR)),
                vocabulary(id = 3, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
                vocabulary(id = 4, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.R)),
            ),
        )

        viewModelScenario {
            WordGroupQuizViewModel(
                savedStateHandle = createSavedStateHandle(),
                quizRepository = repo,
                ioDispatcher = testDispatcher,
            )
        }.use { scenario ->
            val vm = scenario.viewModel
            advanceUntilIdle()
            vm.selectSubgroup(WordGroup.NounSubgroup.R)
            val firstItem = vm.uiState.value as WordGroupQuizUiState.QuizItemState
            vm.check()
            vm.next()
            advanceUntilIdle()

            val beforeItem = vm.uiState.value as WordGroupQuizUiState.QuizItemState

            // When
            scenario.recreate()
            val newVm = scenario.viewModel
            advanceUntilIdle()

            // Then
            val afterItem = newVm.uiState.value as WordGroupQuizUiState.QuizItemState
            expectThat(afterItem).isEqualTo(beforeItem)
            expectThat(firstItem).isNotEqualTo(beforeItem).isNotEqualTo(afterItem)
        }
    }

    @Test
    fun `Process death restores ui state, while check screen is shown`() = runTest(testDispatcher) {
        // Given
        val repo = QuizRepositoryFake(
            initialNouns = listOf(
                vocabulary(id = 1, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR)),
                vocabulary(id = 2, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.OR)),
                vocabulary(id = 3, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
                vocabulary(id = 4, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR)),
            ),
        )

        viewModelScenario {
            WordGroupQuizViewModel(
                savedStateHandle = createSavedStateHandle(),
                quizRepository = repo,
                ioDispatcher = testDispatcher,
            )
        }.use { scenario ->
            val initialVm = scenario.viewModel
            advanceUntilIdle()
            initialVm.selectSubgroup(WordGroup.NounSubgroup.R)
            val firstItem = initialVm.uiState.value as WordGroupQuizUiState.QuizItemState
            initialVm.check()
            // vm.next() // We don't go to the next word yet!
            advanceUntilIdle()

            val beforeItem = initialVm.uiState.value as WordGroupQuizUiState.QuizItemState

            // When
            scenario.recreate()
            val newVm = scenario.viewModel
            advanceUntilIdle()

            // Then
            val afterItem = newVm.uiState.value as WordGroupQuizUiState.QuizItemState
            expectThat(afterItem.userAnswerCorrect).isFalse()
            expectThat(afterItem.selectedSubgroup).isEqualTo(WordGroup.NounSubgroup.R)
            expectThat(afterItem).isEqualTo(beforeItem)
            expectThat(firstItem).isNotEqualTo(beforeItem).isNotEqualTo(afterItem)
        }
    }

    @Test
    fun `Process death restores state for check screen for all words`() = runTest(testDispatcher) {
        // Given
        val nouns = listOf(
            vocabulary(id = 1, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR)),
            vocabulary(id = 2, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.OR)),
            vocabulary(id = 3, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
            vocabulary(id = 4, wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR)),
        )
        val repo = QuizRepositoryFake(
            initialNouns = nouns,
        )
        val mockedRandom = mockk<Random> {
            every { nextLong() } returns 123
        }
        val expectedShuffledNouns = nouns.shuffled(Random(mockedRandom.nextLong()))

        viewModelScenario {
            WordGroupQuizViewModel(
                savedStateHandle = createSavedStateHandle(),
                quizRepository = repo,
                ioDispatcher = testDispatcher,
                random = mockedRandom,
            )
        }.use { scenario ->
            for (currentExpectedNoun in expectedShuffledNouns) {
                val initialVm = scenario.viewModel
                advanceUntilIdle()

                initialVm.selectSubgroup((currentExpectedNoun.wordGroup as WordGroup.Noun).subgroup)
                val firstItem = initialVm.uiState.value as WordGroupQuizUiState.QuizItemState
                initialVm.check()
                // vm.next() // We don't go to the next word yet!
                advanceUntilIdle()

                val beforeItem = initialVm.uiState.value as WordGroupQuizUiState.QuizItemState

                // When
                scenario.recreate()
                val newVm = scenario.viewModel
                advanceUntilIdle()

                // Then
                val afterItem = newVm.uiState.value as WordGroupQuizUiState.QuizItemState
                expectThat(afterItem).isEqualTo(beforeItem)
                // Sanity checks:
                expectThat(afterItem.vocabulary.id)
                    .isEqualTo(beforeItem.vocabulary.id)
                    .isEqualTo(currentExpectedNoun.id)
                expectThat(afterItem.userAnswerCorrect).isTrue()
                expectThat(afterItem.selectedSubgroup).isEqualTo(currentExpectedNoun.wordGroup.subgroup)
                expectThat(firstItem).isNotEqualTo(beforeItem).isNotEqualTo(afterItem)
                newVm.next()
            }
        }
    }
}
