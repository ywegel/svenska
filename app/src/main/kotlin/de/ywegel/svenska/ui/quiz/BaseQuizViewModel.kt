package de.ywegel.svenska.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.domain.quiz.QuizManager
import de.ywegel.svenska.domain.quiz.QuizStrategy
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseQuizViewModel<A : UserAnswer, S : QuizInputState<A>, AC : Any, AR : Any>(
    protected val repository: VocabularyRepository,
    protected val ioDispatcher: CoroutineDispatcher,
    strategy: QuizStrategy<A, AR>,
    userInputControllerFactory: () -> QuizUserInputController<S, AC>,
    protected val containerId: Int?,
) : ViewModel(), QuizCallbacks<A> {

    abstract val shuffleWords: Boolean

    abstract val renderer: QuizRenderer<A, S, AC, AR>

    abstract suspend fun loadVocabularies(containerId: Int?): List<Vocabulary>

    private val _uiState = MutableStateFlow<QuizUiState<A, AR>>(QuizUiState.Loading())
    val uiState: StateFlow<QuizUiState<A, AR>> = _uiState.asStateFlow()

    private val inputController: QuizUserInputController<S, AC> = userInputControllerFactory()
    val inputState: StateFlow<S> = inputController.state
    val actions: AC = inputController.actions

    private val manager = QuizManager(
        strategy = strategy,
        loadVocabularies = ::loadVocabularies,
        containerId = containerId,
        shuffleWords = shuffleWords,
    )

    init {
        launchSafely {
            manager.startQuiz()
            setupQuestion()
        }
    }

    override fun nextWord() {
        launchSafely {
            val nextQuestionAvailable = manager.goToNextQuestion()
            inputController.resetState()

            if (nextQuestionAvailable) {
                setupQuestion()
            } else {
                val (correctAnswers, totalQuestions) = manager.getQuizStatistics()
                _uiState.update {
                    QuizUiState.Finished(
                        correctAnswers = correctAnswers,
                        totalQuestions = totalQuestions,
                        score = correctAnswers.toFloat() / totalQuestions,
                    )
                }
            }
        }
    }

    private fun setupQuestion() {
        val question = manager.getCurrentQuestion()
        _uiState.update { currentState ->
            QuizUiState.Active(
                quizQuestion = question,
                canReturnToPreviousQuestion = manager.hasPreviousQuestion(),
                vocabularyIsFavorite = manager.currentVocabularyIsFavorite(),
            )
        }
    }

    override fun checkAnswer(input: A) {
        val currentState = _uiState.value as? QuizUiState.Active ?: return

        launchSafely {
            val result = manager.validateAnswer(input, currentState.quizQuestion)
            _uiState.update {
                (it as? QuizUiState.Active)?.copy(userAnswerResult = result, userAnswer = input)
                    ?: it
            }
        }
    }

    override fun returnToPreviousQuestion() {
        inputController.resetState()
        manager.goToPreviousQuestion()
        setupQuestion()
    }

    override fun toggleFavorite(isFavorite: Boolean) {
        val currentState = _uiState.value as? QuizUiState.Active ?: return

        launchSafely {
            val id = currentState.quizQuestion.vocabularyId
            repository.toggleVocabularyFavorite(id, isFavorite)
            _uiState.update {
                (it as? QuizUiState.Active<A, AR>)?.copy(vocabularyIsFavorite = isFavorite) ?: it
            }
        }
    }

    /**
     * Launches a coroutine with standardized error handling
     */
    @Suppress("detekt:TooGenericExceptionCaught")
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            try {
                block()
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e) {
                    launchSafely(block)
                }
            }
        }
    }
}

sealed interface QuizUiState<A : UserAnswer, AR : Any> {
    class Loading<A : UserAnswer, AR : Any> : QuizUiState<A, AR>

    data class Error<A : UserAnswer, AR : Any>(
        val exception: Exception,
        val retryAction: () -> Unit,
    ) : QuizUiState<A, AR>

    data class Active<A : UserAnswer, AR : Any>(
        val quizQuestion: QuizQuestion<A>,
        val vocabularyIsFavorite: Boolean? = false,
        val canReturnToPreviousQuestion: Boolean = false,
        val userAnswer: A? = null,
        val userAnswerResult: AR? = null,
    ) : QuizUiState<A, AR>

    data class Finished<A : UserAnswer, AR : Any>(
        val correctAnswers: Int,
        val totalQuestions: Int,
        val score: Float,
    ) : QuizUiState<A, AR>
}
