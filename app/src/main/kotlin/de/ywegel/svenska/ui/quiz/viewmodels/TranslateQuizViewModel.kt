package de.ywegel.svenska.ui.quiz.viewmodels

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.domain.quiz.strategies.TranslationWithoutEndingsQuizStrategy
import de.ywegel.svenska.ui.quiz.BaseQuizViewModel
import de.ywegel.svenska.ui.quiz.controller.TranslateWithoutEndingsActions
import de.ywegel.svenska.ui.quiz.controller.TranslateWithoutEndingsController
import de.ywegel.svenska.ui.quiz.controller.TranslateWithoutEndingsState
import de.ywegel.svenska.ui.quiz.renderers.TranslateWithoutEndingsRenderer
import kotlinx.coroutines.CoroutineDispatcher

@HiltViewModel(assistedFactory = TranslateQuizViewModel.Factory::class)
class TranslateQuizViewModel @AssistedInject constructor(
    repository: VocabularyRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    @Assisted private val translateMode: TranslateMode,
    @Assisted containerId: Int?,
) : BaseQuizViewModel<
    UserAnswer.TranslateWithoutEndingsAnswer,
    TranslateWithoutEndingsState,
    TranslateWithoutEndingsActions,
    Boolean,
    >(
    repository = repository,
    ioDispatcher = ioDispatcher,
    strategy = TranslationWithoutEndingsQuizStrategy(translateMode),
    userInputControllerFactory = { TranslateWithoutEndingsController() },
    containerId = containerId,
) {
    override val renderer = TranslateWithoutEndingsRenderer()

    @AssistedFactory
    interface Factory {
        fun create(translateMode: TranslateMode, containerId: Int?): TranslateQuizViewModel
    }
}
