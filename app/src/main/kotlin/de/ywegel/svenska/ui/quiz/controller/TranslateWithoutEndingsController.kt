package de.ywegel.svenska.ui.quiz.controller

import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.quiz.QuizInputState
import de.ywegel.svenska.ui.quiz.QuizUserInputController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TranslateWithoutEndingsController : QuizUserInputController<
    TranslateWithoutEndingsState,
    TranslateWithoutEndingsActions,
    > {
    private val _state = MutableStateFlow(TranslateWithoutEndingsState())
    override val state: StateFlow<TranslateWithoutEndingsState> = _state

    override val actions = TranslateWithoutEndingsActions(
        onInputChanged = { _state.value = _state.value.copy(translationInput = it) },
    )

    override fun resetState() {
        _state.value = TranslateWithoutEndingsState()
    }
}

data class TranslateWithoutEndingsState(
    val translationInput: String = "",
) : QuizInputState<UserAnswer.TranslateWithoutEndingsAnswer> {
    override fun toUserAnswer() = UserAnswer.TranslateWithoutEndingsAnswer(translationInput)
}

data class TranslateWithoutEndingsActions(
    val onInputChanged: (String) -> Unit,
)
