@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.container

import app.cash.turbine.test
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.model.containers
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import io.mockk.clearAllMocks
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.doesNotContain
import strikt.assertions.isEqualTo

class ContainerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `addEditContainer creates a new container, if no id is passed`() = runTest(testDispatcher) {
        // Given
        val viewModel = setupViewModel()
        advanceUntilIdle()

        // When
        viewModel.addEditContainer(containerName = "newContainer", existingContainerId = null)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            expectThat(awaitItem().containers)
                .isEqualTo(listOf(VocabularyContainer(id = 1, name = "newContainer")))
        }
    }

    @Test
    fun `addEditContainer renames an existing container and keeps the id`() = runTest(testDispatcher) {
        // Given
        val existingContainer = VocabularyContainer(id = 1000, name = "existing test container")
        val expected = VocabularyContainer(id = 1000, name = "a different name for the existing container")

        val viewModel = setupViewModel(
            repository = VocabularyRepositoryFake(
                initialVocabulary = emptyList(),
                initialContainers = containers() + existingContainer,
            ),
        )
        advanceUntilIdle()

        // When
        viewModel.addEditContainer(containerName = expected.name, existingContainerId = existingContainer.id)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            expectThat(awaitItem().containers)
                .contains(expected)
                .doesNotContain(existingContainer)
                .containsExactlyInAnyOrder(containers() + expected)
        }
    }

    @Test
    fun `addEditContainer with same name and id does not change the container`() = runTest(testDispatcher) {
        // Given
        val existingContainer = VocabularyContainer(id = 5, name = "unchanged")
        val viewModel = setupViewModel(
            repository = VocabularyRepositoryFake(
                initialVocabulary = emptyList(),
                initialContainers = containers() + existingContainer,
            ),
        )
        advanceUntilIdle()

        // When
        viewModel.addEditContainer(containerName = "unchanged", existingContainerId = 5)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            expectThat(awaitItem().containers)
                .containsExactlyInAnyOrder(containers() + existingContainer)
        }
    }

    @Test
    fun `addEditContainer with empty name should not create container`() = runTest(testDispatcher) {
        // Given
        val viewModel = setupViewModel(
            repository = VocabularyRepositoryFake(
                initialVocabulary = emptyList(),
                initialContainers = containers(),
            ),
        )

        // When
        viewModel.addEditContainer("", null)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            expectThat(awaitItem().containers).isEqualTo(containers())
        }
    }

    private fun setupViewModel(
        repository: ContainerRepository = VocabularyRepositoryFake(),
        dispatcher: CoroutineDispatcher = testDispatcher,
    ): ContainerViewModel {
        return ContainerViewModel(
            containerRepository = repository,
            ioDispatcher = dispatcher,
        )
    }
}
