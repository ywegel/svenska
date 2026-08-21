package de.ywegel.svenska.ui.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.AppPreferenceKeys
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContainerViewModel @Inject constructor(
    private val containerRepository: ContainerRepository,
    userPreferencesManager: UserPreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val isEditMode = MutableStateFlow(false)

    val uiState: StateFlow<ContainerUiState> = combine(
        containerRepository.getAllContainers().flowOn(ioDispatcher),
        userPreferencesManager.flow(AppPreferenceKeys.UseNewQuiz),
        isEditMode,
    ) { containers, useNewQuiz, editMode ->
        ContainerUiState(
            containers = containers,
            isEditModeMode = editMode,
            useNewQuiz = useNewQuiz,
        )
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ContainerUiState())

    fun updateIsEditMode(isEnabled: Boolean) {
        isEditMode.value = isEnabled
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
