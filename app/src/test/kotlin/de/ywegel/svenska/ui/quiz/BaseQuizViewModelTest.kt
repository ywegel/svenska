package de.ywegel.svenska.ui.quiz

import androidx.compose.runtime.Composable
import app.cash.turbine.test
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.vocabularies
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.domain.quiz.QuizStrategy
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.domain.quiz.strategies.TranslationWithoutEndingsQuizStrategy
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo
import strikt.assertions.isTrue

// TODO: Refactor all tests to make them more readable
@ExperimentalCoroutinesApi
class BaseQuizViewModelTest {

    // Create a test implementation of the abstract BaseQuizViewModel
    private class TestViewModel(
        repository: VocabularyRepository,
        ioDispatcher: CoroutineDispatcher,
        strategy: QuizStrategy<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>,
        userInputControllerFactory: () -> QuizUserInputController<TestState, TestActions>,
        containerId: Int?,
        override val shuffleWords: Boolean = true,
        override val renderer: QuizRenderer<UserAnswer.TranslateWithoutEndingsAnswer, TestState, TestActions, Boolean>,
    ) : BaseQuizViewModel<UserAnswer.TranslateWithoutEndingsAnswer, TestState, TestActions, Boolean>(
        repository,
        ioDispatcher,
        strategy,
        userInputControllerFactory,
        containerId,
    ) {
        override suspend fun loadVocabularies(containerId: Int?): List<Vocabulary> {
            return repository.getAllVocabulariesSnapshot(containerId)
        }
    }

    // Simple test implementations
    private class TestState : QuizInputState<UserAnswer.TranslateWithoutEndingsAnswer> {
        var answer: String = ""

        override fun toUserAnswer(): UserAnswer.TranslateWithoutEndingsAnswer {
            return UserAnswer.TranslateWithoutEndingsAnswer(answer)
        }
    }

    private class TestActions {
        var onAnswerChanged: (String) -> Unit = {}
    }

    private class TestController : QuizUserInputController<TestState, TestActions> {
        private val _state = MutableStateFlow(TestState())
        override val state: StateFlow<TestState> = _state

        override val actions = TestActions().apply {
            onAnswerChanged = { answer ->
                _state.value = TestState().apply { this.answer = answer }
            }
        }

        override fun resetState() {
            _state.value = TestState()
        }
    }

    private class TestRenderer :
        QuizRenderer<UserAnswer.TranslateWithoutEndingsAnswer, TestState, TestActions, Boolean> {
        @Composable
        override fun Prompt(question: QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>) {
            // Empty test implementation
        }

        @Composable
        override fun UserInput(
            question: QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>,
            state: TestState,
            actions: TestActions,
        ) {
            // Empty test implementation
        }

        @Composable
        override fun Solution(
            question: QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>,
            userAnswer: UserAnswer.TranslateWithoutEndingsAnswer,
            userAnswerResult: Boolean,
            wordGroupSection: (@Composable (highlightEndings: Boolean) -> Unit)?,
        ) {
            // Empty test implementation}
        }
    }

    // Mocks and dependencies
    private lateinit var repository: VocabularyRepository
    private lateinit var strategy: QuizStrategy<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var controller: QuizUserInputController<TestState, TestActions>
    private lateinit var renderer:
        QuizRenderer<UserAnswer.TranslateWithoutEndingsAnswer, TestState, TestActions, Boolean>
    private lateinit var viewModel: TestViewModel
    private lateinit var controllerFactory: () -> QuizUserInputController<TestState, TestActions>

    @BeforeEach
    fun setup() {
        val repositoryMockData = vocabularies()
        repository = VocabularyRepositoryFake(repositoryMockData)
        strategy = TranslationWithoutEndingsQuizStrategy(TranslateMode.SwedishToNative)
        testDispatcher = StandardTestDispatcher()
        renderer = TestRenderer()
        controller = spyk(TestController())

        Dispatchers.setMain(testDispatcher)

        controllerFactory = { controller }
    }

    @AfterEach
    fun cleanUp() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `should start with Loading and transition to Active state`() = runTest(testDispatcher) {
        // Given
        val vocabularies = vocabularies()
        val question = vocabularies[0].toQuizQuestion()
        val strategy = QuizStrategyFake(isValid = true, question)

        val repository = spyk(VocabularyRepositoryFake(vocabularies))

        viewModel = TestViewModel(
            repository = repository,
            ioDispatcher = testDispatcher,
            strategy = strategy,
            userInputControllerFactory = controllerFactory,
            containerId = null,
            renderer = renderer,
        )

        // When
        // ViewModel initialization happens in setup

        // Then
        viewModel.uiState.test {
            // First emission should be Loading
            val loadingState = awaitItem()
            expectThat(loadingState).isA<QuizUiState.Loading<*, *>>()

            // Second emission should be Active
            val activeState = awaitItem()
            expectThat(activeState)
                .isA<QuizUiState.Active<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>>()
                .get { quizQuestion }
                .isEqualTo(question)
        }

        coVerify { repository.getAllVocabulariesSnapshot(any()) }
    }

    @Nested
    @DisplayName("NextWord")
    inner class NextWord {

        @Test
        fun `should update UI state with next question`() = runTest(testDispatcher) {
            // Given
            val vocabularies = vocabularies()
            val question = vocabularies[0].toQuizQuestion()
            val strategy = QuizStrategyFake(isValid = true, question)

            val repository = VocabularyRepositoryFake(vocabularies)

            viewModel = TestViewModel(
                repository = repository,
                ioDispatcher = testDispatcher,
                strategy = strategy,
                userInputControllerFactory = controllerFactory,
                containerId = null,
                renderer = renderer,
            )

            advanceUntilIdle()

            // When
            viewModel.nextWord()

            advanceUntilIdle()

            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state)
                    .isA<QuizUiState.Active<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>>()
                    .get { quizQuestion }
                    .isEqualTo(question)
            }
        }

//        @Test
//        fun `should transition to Finished state when quiz ends`() = runTest(testDispatcher) {
//            // Given
//            // Mock strategy to return null to simulate end of quiz
//            coEvery { strategy.generateQuestion(any()) } returns null
//
//            // Mock quiz statistics
//            val quizStats = mockk<QuizManager.QuizStatistics>()
//            every { quizStats.correctAnswers } returns 8
//            every { quizStats.totalQuestions } returns 10
//
//            // Mock manager to return the statistics
//            every { manager.getQuizStatistics() } returns quizStats
//
//            advanceUntilIdle()
//
//            // When
//            viewModel.nextWord()
//
//            // Then
//            viewModel.uiState.test {
//                val state = awaitItem()
//                expectThat(state).isA<QuizUiState.Finished<*, *>>()
//
//                val finishedState = state as QuizUiState.Finished<*, *>
//
//                expectThat(finishedState) {
//                    get { correctAnswers }.isEqualTo(8)
//                    get { totalQuestions }.isEqualTo(10)
//                    get { score }.isEqualTo(0.8f)
//                }
//            }
//        }
    }

    @Nested
    @DisplayName("CheckAnswer")
    inner class CheckAnswer {

        @Test
        fun `should validate answer with strategy and update state`() = runTest(testDispatcher) {
            // Given
            val strategy = spyk(strategy)

            viewModel = TestViewModel(
                repository = repository,
                ioDispatcher = testDispatcher,
                strategy = strategy,
                userInputControllerFactory = controllerFactory,
                containerId = null,
                renderer = renderer,
            )

            advanceUntilIdle()

            val currentQuestion = (
                viewModel.uiState.value as QuizUiState.Active<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>
                ).quizQuestion
            viewModel.actions.onAnswerChanged(currentQuestion.expectedAnswer.answer)
            val userAnswer = viewModel.inputState.value.toUserAnswer()

            advanceUntilIdle()

            // When
            viewModel.checkAnswer(userAnswer)

            advanceUntilIdle()

            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                expect {
                    expectThat(state).isA<QuizUiState.Active<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>>()

                    state as QuizUiState.Active<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>
                    expectThat(state.userAnswer).isEqualTo(userAnswer)
                    expectThat(state.userAnswerResult).isTrue()
                }
            }

            verify { strategy.validateAnswer(any(), userAnswer) }
        }
    }

    @Test
    fun `Clicking on returnToPreviousQuestion shows the last question`() = runTest(testDispatcher) {
        // Given
        viewModel = TestViewModel(
            repository = repository,
            ioDispatcher = testDispatcher,
            strategy = strategy,
            userInputControllerFactory = controllerFactory,
            containerId = null,
            renderer = renderer,
        )

        println(repository.getAllVocabulariesSnapshot(null))

        advanceUntilIdle()

        viewModel.uiState.test {
            val lastQuestion =
                (awaitItem() as QuizUiState.Active<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>).quizQuestion

            // When
            viewModel.nextWord()

            val nextQuestion =
                (awaitItem() as QuizUiState.Active<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>).quizQuestion
            expectThat(nextQuestion).isNotEqualTo(lastQuestion)

            viewModel.returnToPreviousQuestion()

            // Then
            val currentQuestion =
                (awaitItem() as QuizUiState.Active<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>).quizQuestion

            expectThat(currentQuestion).isEqualTo(lastQuestion)
            expectThat(currentQuestion).isNotEqualTo(nextQuestion)
        }
    }

    @Test
    fun `Finish state is reached after all entries`() = runTest(testDispatcher) {
        val repository = VocabularyRepositoryFake(initialVocabulary = vocabularies().take(2))

        viewModel = TestViewModel(
            repository = repository,
            ioDispatcher = testDispatcher,
            strategy = strategy,
            userInputControllerFactory = controllerFactory,
            containerId = null,
            renderer = renderer,
        )

        advanceUntilIdle()

        viewModel.uiState.test {
            val entry1 = awaitItem()

            expectThat(entry1).isA<QuizUiState.Active<*, *>>()

            viewModel.nextWord()

            val entry2 = awaitItem()
            expectThat(entry2).isA<QuizUiState.Active<*, *>>()

            viewModel.nextWord()

            val finishState = awaitItem()
            expectThat(finishState).isA<QuizUiState.Finished<*, *>>()
        }
    }

    @Test
    fun `Finish state is reached if only one entry exists`() = runTest(testDispatcher) {
        val repository = VocabularyRepositoryFake(listOf(vocabulary()))

        viewModel = TestViewModel(
            repository = repository,
            ioDispatcher = testDispatcher,
            strategy = strategy,
            userInputControllerFactory = controllerFactory,
            containerId = null,
            renderer = renderer,
        )

        advanceUntilIdle()

        viewModel.uiState.test {
            val entry1 = awaitItem()

            expectThat(entry1).isA<QuizUiState.Active<*, *>>()

            viewModel.nextWord()

            val finishState = awaitItem()
            expectThat(finishState).isA<QuizUiState.Finished<*, *>>()
        }
    }

    @Nested
    @DisplayName("Favorites")
    inner class Favorites {

        @Test
        fun `should update UI state and repository when toggling favorite with no initial favorites`() =
            runTest(testDispatcher) {
                // Given
                // Create a list of vocabularies with no favorites
                val nonFavoriteVocabulary = vocabulary(isFavorite = false)
                val repository = spyk(VocabularyRepositoryFake(listOf(nonFavoriteVocabulary)))
                val question = nonFavoriteVocabulary.toQuizQuestion()
                val strategy = QuizStrategyFake(isValid = true, question)

                viewModel = TestViewModel(
                    repository = repository,
                    ioDispatcher = testDispatcher,
                    strategy = strategy,
                    userInputControllerFactory = controllerFactory,
                    containerId = null,
                    renderer = renderer,
                )

                advanceUntilIdle()

                // When
                viewModel.toggleFavorite(true)

                advanceUntilIdle()

                // Then
                // Verify UI state is updated
                viewModel.uiState.test {
                    val state = awaitItem()
                    expectThat(state)
                        .isA<QuizUiState.Active<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>>()
                        .and {
                            get { quizQuestion.vocabularyId }.isEqualTo(nonFavoriteVocabulary.id)
                            get { vocabularyIsFavorite }.isTrue()
                        }
                }

                // Verify repository is updated
                coVerify { repository.toggleVocabularyFavorite(nonFavoriteVocabulary.id, true) }

                // Verify repository state
                val favorites = repository.getFavorites(null)
                expectThat(favorites).hasSize(1)
                expectThat(favorites.first().id).isEqualTo(nonFavoriteVocabulary.id)
            }

        @Test
        fun `should update UI state and repository when toggling favorite with existing favorites`() =
            runTest(testDispatcher) {
                // Given
                // Create a list of vocabularies with 1 existing favorite and 2 non-favorites
                val vocabulariesWithOneFavorite = vocabularies().take(3).mapIndexed { index, vocabulary ->
                    vocabulary.copy(isFavorite = index == 0) // Only the first one is a favorite
                }
                val repositoryFake = VocabularyRepositoryFake(vocabulariesWithOneFavorite)
                val repository = spyk(repositoryFake)
                val question = vocabulariesWithOneFavorite[1].toQuizQuestion() // Second vocabulary (not a favorite)
                val strategy = QuizStrategyFake(isValid = true, question)

                // Sanity check: We should only have 3 vocabularies in the repository, and one of them is a favorite
                expectThat(repositoryFake.getFavorites(null).size).isEqualTo(1)
                expectThat(repositoryFake.getAllVocabulariesSnapshot(null).size).isEqualTo(3)

                viewModel = TestViewModel(
                    repository = repository,
                    ioDispatcher = testDispatcher,
                    strategy = strategy,
                    userInputControllerFactory = controllerFactory,
                    containerId = null,
                    renderer = renderer,
                )

                advanceUntilIdle()

                // When
                viewModel.toggleFavorite(true)

                advanceUntilIdle()

                // Then
                // Verify UI state is updated
                viewModel.uiState.test {
                    val state = awaitItem()
                    expectThat(state)
                        .isA<QuizUiState.Active<UserAnswer.TranslateWithoutEndingsAnswer, Boolean>>()
                        .and {
                            get { quizQuestion.vocabularyId }.isEqualTo(vocabulariesWithOneFavorite[1].id)
                            get { vocabularyIsFavorite }.isTrue()
                        }
                }

                // Verify the repository is updated - now we should have 2 favorites and 1 non-favorite
                coVerify { repository.toggleVocabularyFavorite(vocabulariesWithOneFavorite[1].id, true) }

                // Verify the repository contains 2 favorite vocabularies and 1 non-favorite vocabulary
                val favorites = repositoryFake.getFavorites(null)
                val allVocabularies = repositoryFake.getAllVocabulariesSnapshot(null)
                val nonFavorites = allVocabularies.filter { !it.isFavorite }

                expectThat(favorites.size).isEqualTo(2)
                expectThat(nonFavorites.size).isEqualTo(1)
                expectThat(allVocabularies)
                    .hasSize(3)
                    .containsExactlyInAnyOrder(favorites + nonFavorites)
            }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandling {

        @Test
        fun `should transition to Error state when an exception occurs`() = runTest(testDispatcher) {
            // Given
            val exception = RuntimeException("Test error")

            // Force an error in the nextWord function
            val strategy = spyk(strategy)
            coEvery { strategy.generateQuestion(any()) } throws exception

            viewModel = TestViewModel(
                repository = repository,
                ioDispatcher = testDispatcher,
                strategy = strategy,
                userInputControllerFactory = controllerFactory,
                containerId = null,
                renderer = renderer,
            )

            // When
            viewModel.nextWord()

            advanceUntilIdle()

            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                expectThat(state).isA<QuizUiState.Error<*, *>>()

                val errorState = state as QuizUiState.Error<*, *>
                expectThat(errorState.exception).isEqualTo(exception)
            }
        }

        @Disabled("Refactor test") // TODO: Refactor test
        @Test
        fun `should retry operation when retry action is called`() = runTest(testDispatcher) {
            // Given
            val exception = RuntimeException("Test error")

            val strategy = spyk(strategy)

            // Force an error in the first call, then succeed in the second
            coEvery { strategy.generateQuestion(any()) } throws exception andThen QuizQuestion(
                vocabularyId = 3,
                prompt = "mus",
                expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("mouse"),
            )

            viewModel = TestViewModel(
                repository = repository,
                ioDispatcher = testDispatcher,
                strategy = strategy,
                userInputControllerFactory = controllerFactory,
                containerId = null,
                renderer = renderer,
            )

            // When
            viewModel.nextWord()

            advanceUntilIdle()

            // Then
            viewModel.uiState.test {
                expect {
                    val errorState = awaitItem()
                    expectThat(errorState).isA<QuizUiState.Error<*, *>>()

                    errorState as QuizUiState.Error<*, *>

                    errorState.retryAction()

                    val activeState = awaitItem()
                    expectThat(activeState).isA<QuizUiState.Active<*, *>>()
                }
            }

            // Verify the strategy was called twice (once for the error, once for the retry)
            verify(exactly = 2) { strategy.generateQuestion(any()) }
        }
    }

    private fun Vocabulary.toQuizQuestion(): QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer> {
        return QuizQuestion(
            vocabularyId = this.id,
            prompt = this.word,
            expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer(this.translation),
        )
    }
}

private class QuizStrategyFake(
    private val isValid: Boolean,
    private val generatedQuestion: QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>,
) : QuizStrategy<UserAnswer.TranslateWithoutEndingsAnswer, Boolean> {

    override fun generateQuestion(vocabulary: Vocabulary): QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer> {
        return generatedQuestion
    }

    override fun validateAnswer(
        question: QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>,
        userAnswer: UserAnswer.TranslateWithoutEndingsAnswer,
    ): Boolean = isValid
}
