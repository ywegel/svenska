package de.ywegel.svenska.ui.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.model.VocabularyContainer
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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContainerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observerContainers()
    }

    private fun observerContainers() = viewModelScope.launch(ioDispatcher) {
        containerRepository.getAllContainers().collectLatest { containers ->
            _uiState.update {
                it.copy(containers = containers)
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
)
