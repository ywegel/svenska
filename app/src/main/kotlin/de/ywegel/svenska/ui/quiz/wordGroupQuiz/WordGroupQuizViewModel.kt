package de.ywegel.svenska.ui.quiz.wordGroupQuiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.QuizRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.ui.quiz.wordGroupQuiz.WordGroupQuizUiState.QuizItemState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class WordGroupQuizViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    random: Random = Random,
) : ViewModel() {
    private val containerId: Int? = savedStateHandle.navArgs<WordGroupQuizScreenNavArgs>().containerId

    private val shuffleSeed: Long = savedStateHandle.get<Long>(SHUFFLE_SEED_KEY) ?: run {
        val seed = random.nextLong()
        savedStateHandle[SHUFFLE_SEED_KEY] = seed
        seed
    }

    private var currentIndex: Int by savedStateHandle.saved(CURRENT_INDEX_KEY) { 0 }

    private val _uiState = MutableStateFlow<WordGroupQuizUiState>(WordGroupQuizUiState.Loading)
    val uiState: StateFlow<WordGroupQuizUiState> = _uiState.asStateFlow()

    private var quizVocabularies: List<Vocabulary> = emptyList()

    init {
        viewModelScope.launch {
            loadQuizVocabularies()
        }
    }

    private suspend fun loadQuizVocabularies() = withContext(ioDispatcher) {
        val allVocabularies: List<Vocabulary> = quizRepository.getAllNouns(containerId)

        if (allVocabularies.isEmpty()) {
            _uiState.value = WordGroupQuizUiState.Empty
        } else {
            quizVocabularies = allVocabularies
                .shuffled(Random(shuffleSeed))
            loadItemForCurrentIndex(resetPreviousInput = false)
        }
    }

    fun selectSubgroup(subgroup: WordGroup.NounSubgroup) {
        val currentState = _uiState.value as? QuizItemState ?: return
        savedStateHandle[USER_SELECTED_SUBGROUP_KEY] = subgroup
        _uiState.value = currentState.copy(selectedSubgroup = subgroup)
    }

    fun check() {
        val currentState = _uiState.value as? QuizItemState ?: return
        val expectedSubGroup = (currentState.vocabulary.wordGroup as? WordGroup.Noun)?.subgroup

        val answerIsCorrect =
            when (expectedSubGroup) {
                WordGroup.NounSubgroup.UNDEFINED, WordGroup.NounSubgroup.SPECIAL -> {
                    currentState.selectedSubgroup == WordGroup.NounSubgroup.UNDEFINED ||
                        currentState.selectedSubgroup == WordGroup.NounSubgroup.SPECIAL
                }

                else -> {
                    currentState.selectedSubgroup == expectedSubGroup
                }
            }

        savedStateHandle[USER_ANSWER_CORRECT_KEY] = answerIsCorrect
        _uiState.value = currentState.copy(userAnswerCorrect = answerIsCorrect)
    }

    fun next() {
        currentIndex++
        if (currentIndex < quizVocabularies.size) {
            loadItemForCurrentIndex()
        } else {
            _uiState.value = WordGroupQuizUiState.Completed
        }
    }

    private fun loadItemForCurrentIndex(resetPreviousInput: Boolean = true) {
        val vocabulary = quizVocabularies[currentIndex]
        val correctSubgroup = (vocabulary.wordGroup as WordGroup.Noun).subgroup

        if (resetPreviousInput) {
            savedStateHandle[USER_SELECTED_SUBGROUP_KEY] = null
            savedStateHandle[USER_ANSWER_CORRECT_KEY] = null
            _uiState.value = QuizItemState(
                progress = currentIndex,
                progressGoal = quizVocabularies.size,
                vocabulary = vocabulary,
                correctSubgroup = correctSubgroup,
                selectedSubgroup = null,
                userAnswerCorrect = null,
            )
        } else {
            _uiState.value = QuizItemState(
                progress = currentIndex,
                progressGoal = quizVocabularies.size,
                vocabulary = vocabulary,
                correctSubgroup = correctSubgroup,
                selectedSubgroup = savedStateHandle[USER_SELECTED_SUBGROUP_KEY],
                userAnswerCorrect = savedStateHandle[USER_ANSWER_CORRECT_KEY],
            )
        }
    }
}

private const val SHUFFLE_SEED_KEY = "quiz_shuffle_seed"
private const val CURRENT_INDEX_KEY = "quiz_current_index"
private const val USER_SELECTED_SUBGROUP_KEY = "user_selected_subgroup"
private const val USER_ANSWER_CORRECT_KEY = "user_answer_correct"

sealed class WordGroupQuizUiState {
    data object Loading : WordGroupQuizUiState()
    data object Empty : WordGroupQuizUiState()
    data object Completed : WordGroupQuizUiState()
    data class QuizItemState(
        val progress: Int,
        val progressGoal: Int,
        val vocabulary: Vocabulary,
        val correctSubgroup: WordGroup.NounSubgroup,
        val selectedSubgroup: WordGroup.NounSubgroup?,
        val userAnswerCorrect: Boolean? = null,
    ) : WordGroupQuizUiState()
}

val WordGroupQuizUiState.progress: Int?
    get() = (this as? QuizItemState)?.progress

val WordGroupQuizUiState.progressGoal: Int?
    get() = (this as? QuizItemState)?.progressGoal
