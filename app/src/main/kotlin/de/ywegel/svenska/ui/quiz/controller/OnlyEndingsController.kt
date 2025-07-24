package de.ywegel.svenska.ui.quiz.controller

import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.quiz.QuizInputState
import de.ywegel.svenska.ui.quiz.QuizUserInputController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OnlyEndingsController : QuizUserInputController<
    OnlyEndingsState,
    OnlyEndingsActions,
    > {
    private val _state = MutableStateFlow(OnlyEndingsState())
    override val state: StateFlow<OnlyEndingsState> = _state

    override val actions = OnlyEndingsActions(
        onEndingsChanged = { _state.value = _state.value.copy(endingsInput = it) },
    )

    override fun resetState() {
        _state.value = OnlyEndingsState()
    }
}

data class OnlyEndingsState(
    val endingsInput: String = "",
) : QuizInputState<UserAnswer.OnlyEndingsAnswer> {
    override fun toUserAnswer(): UserAnswer.OnlyEndingsAnswer = UserAnswer.OnlyEndingsAnswer(endingsInput)
}

data class OnlyEndingsActions(
    val onEndingsChanged: (String) -> Unit,
)
