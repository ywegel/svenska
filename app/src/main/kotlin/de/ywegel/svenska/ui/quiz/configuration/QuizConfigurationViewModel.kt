package de.ywegel.svenska.ui.quiz.configuration

import androidx.lifecycle.ViewModel
import de.ywegel.svenska.domain.quiz.model.QuizMode
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class QuizConfigurationViewModel(
    initialUiState: ConfigurationState = ConfigurationState(),
) : ViewModel(), QuizConfigurationCallbacks {
    private val _configurationState = MutableStateFlow(initialUiState)
    val configurationState = _configurationState.asStateFlow()

    override fun quizModeChanged(mode: TranslateMode) {
        _configurationState.update {
            it.copy(selectedType = mode)
        }
    }

    override fun withEndingsChanged(withEndings: Boolean) {
        _configurationState.update {
            it.copy(withEndings = withEndings)
        }
    }

    override fun onlyEndingsChanged(onlyEndings: Boolean) {
        _configurationState.update {
            it.copy(
                withEndings = true,
                onlyEndings = onlyEndings,
                selectedType = TranslateMode.NativeToSwedish,
            ) // Only Native mode is supported
        }
    }

    fun generateNavigationArgs(): QuizMode? {
        val state = configurationState.value
        return with(state) {
            when {
                !withEndings && !onlyEndings && selectedType != null -> QuizMode.Translate(selectedType)
                withEndings && !onlyEndings && selectedType != null -> QuizMode.TranslateWithEndings(selectedType)
                onlyEndings -> QuizMode.OnlyEndings
                else -> null
            }
        }
    }
}

data class ConfigurationState(
    val selectedType: TranslateMode? = null,
    val withEndings: Boolean = false,
    val onlyEndings: Boolean = false,
)
