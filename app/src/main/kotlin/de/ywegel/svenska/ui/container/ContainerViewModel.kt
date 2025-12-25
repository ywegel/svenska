package de.ywegel.svenska.ui.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContainerViewModel @Inject constructor(
    private val containerRepository: ContainerRepository,
    userPreferencesManager: UserPreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContainerUiState())
    val uiState = _uiState.asStateFlow()

    private val appPreferencesFlow = userPreferencesManager.preferencesAppFlow

    init {
        observerContainers()
        observerPreferencesState()
    }

    private fun observerContainers() = viewModelScope.launch(ioDispatcher) {
        containerRepository.getAllContainers().collectLatest { containers ->
            _uiState.update {
                it.copy(containers = containers)
            }
        }
    }

    private fun observerPreferencesState() = viewModelScope.launch {
        launch {
            appPreferencesFlow.collectLatest { preferences ->
                _uiState.update {
                    it.copy(useNewQuiz = preferences.useNewQuiz)
                }
            }
        }
    }

    fun updateIsEditMode(isEnabled: Boolean) {
        _uiState.update {
            it.copy(isEditModeMode = isEnabled)
        }
    }

    fun deleteContainer(container: VocabularyContainer) = viewModelScope.launch(ioDispatcher) {
        containerRepository.deleteContainerWithAllVocabulary(container)
    }

    fun addEditContainer(containerName: String, existingContainerId: Int?) {
        // Don't create container if name is blank. This should be blocked by the ui as well.
        if (containerName.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            containerRepository.upsertContainer(
                container = VocabularyContainer(
                    name = containerName,
                    id = existingContainerId ?: 0,
                ),
            )
        }
    }
}

data class ContainerUiState(
    val containers: List<VocabularyContainer> = emptyList(),
    val isEditModeMode: Boolean = false,
    val useNewQuiz: Boolean = false,
)
