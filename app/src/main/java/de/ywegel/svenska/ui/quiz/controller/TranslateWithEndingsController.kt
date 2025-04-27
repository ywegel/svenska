package de.ywegel.svenska.ui.quiz.controller

import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.quiz.QuizInputState
import de.ywegel.svenska.ui.quiz.QuizUserInputController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TranslateWithEndingsController : QuizUserInputController<
    TranslateWithEndingsState,
    TranslateWithEndingsActions,
    > {
    private val _state = MutableStateFlow(TranslateWithEndingsState())
    override val state: StateFlow<TranslateWithEndingsState> = _state

    override val actions = TranslateWithEndingsActions(
        onTranslationChanged = { _state.value = _state.value.copy(translationInput = it) },
        onEndingChanged = { _state.value = _state.value.copy(endingInput = it) },
    )

    override fun resetState() {
        _state.value = TranslateWithEndingsState()
    }
}

data class TranslateWithEndingsState(
    val translationInput: String = "",
    val endingInput: String = "",
) : QuizInputState<UserAnswer.TranslateWithEndingsAnswer> {
    override fun toUserAnswer() = UserAnswer.TranslateWithEndingsAnswer(
        answer = translationInput,
        endings = endingInput,
    )
}

data class TranslateWithEndingsActions(
    val onTranslationChanged: (String) -> Unit,
    val onEndingChanged: (String) -> Unit,
)

data class TranslateWithEndingsResult(
    val translationCorrect: Boolean,
    val endingsCorrect: Boolean,
)
