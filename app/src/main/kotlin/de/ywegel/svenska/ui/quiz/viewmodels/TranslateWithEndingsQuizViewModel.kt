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
import de.ywegel.svenska.domain.quiz.strategies.TranslationWithEndingsQuizStrategy
import de.ywegel.svenska.ui.quiz.BaseQuizViewModel
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsActions
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsController
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsResult
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsState
import de.ywegel.svenska.ui.quiz.renderers.TranslateWithEndingsRenderer
import kotlinx.coroutines.CoroutineDispatcher

@HiltViewModel(assistedFactory = TranslateWithEndingsQuizViewModel.Factory::class)
class TranslateWithEndingsQuizViewModel @AssistedInject constructor(
    repository: VocabularyRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    @Assisted private val quizMode: QuizMode.TranslateWithEndings,
    @Assisted containerId: Int?,
) : BaseQuizViewModel<
    UserAnswer.TranslateWithEndingsAnswer,
    TranslateWithEndingsState,
    TranslateWithEndingsActions,
    TranslateWithEndingsResult,
    >(
    repository,
    ioDispatcher,
    TranslationWithEndingsQuizStrategy(quizMode.mode),
    { TranslateWithEndingsController() },
    shuffleWords = quizMode.shuffleWords,
    containerId,
) {
    override val renderer = TranslateWithEndingsRenderer()

    override suspend fun loadVocabularies(containerId: Int?): List<Vocabulary> {
        return repository.getAllVocabulariesSnapshot(containerId)
    }

    @AssistedFactory
    interface Factory {
        fun create(quizMode: QuizMode.TranslateWithEndings, containerId: Int?): TranslateWithEndingsQuizViewModel
    }
}
