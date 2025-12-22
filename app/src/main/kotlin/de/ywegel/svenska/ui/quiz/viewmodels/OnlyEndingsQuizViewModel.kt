package de.ywegel.svenska.ui.quiz.viewmodels

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.domain.quiz.model.QuizMode
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.domain.quiz.strategies.OnlyEndingsQuizStrategy
import de.ywegel.svenska.ui.quiz.BaseQuizViewModel
import de.ywegel.svenska.ui.quiz.controller.OnlyEndingsActions
import de.ywegel.svenska.ui.quiz.controller.OnlyEndingsController
import de.ywegel.svenska.ui.quiz.controller.OnlyEndingsState
import de.ywegel.svenska.ui.quiz.renderers.OnlyEndingsRenderer
import kotlinx.coroutines.CoroutineDispatcher

@HiltViewModel(assistedFactory = OnlyEndingsQuizViewModel.Factory::class)
class OnlyEndingsQuizViewModel @AssistedInject constructor(
    repository: VocabularyRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    @Assisted private val quizMode: QuizMode.OnlyEndings,
    @Assisted containerId: Int?,
) : BaseQuizViewModel<
    UserAnswer.OnlyEndingsAnswer,
    OnlyEndingsState,
    OnlyEndingsActions,
    Boolean,
    >(
    repository = repository,
    ioDispatcher = ioDispatcher,
    strategy = OnlyEndingsQuizStrategy(),
    userInputControllerFactory = { OnlyEndingsController() },
    shuffleWords = quizMode.shuffleWords,
    containerId = containerId,
) {
    override val renderer = OnlyEndingsRenderer()

    override suspend fun loadVocabularies(containerId: Int?): List<Vocabulary> {
        return repository.getAllVocabulariesWithEndings(containerId)
    }

    @AssistedFactory
    interface Factory {
        fun create(quizMode: QuizMode.OnlyEndings, containerId: Int?): OnlyEndingsQuizViewModel
    }
}
