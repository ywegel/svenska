@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.overview

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
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
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

// Test the DetailScreen logic in the OverviewViewModel
class OverviewViewModelDetailScreenTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userPreferencesManager: UserPreferencesManager

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        userPreferencesManager = UserPreferencesManagerFake()
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has showDetailScreen false and selectedVocabulary null`() = runTest(testDispatcher) {
        // Given
        val viewModel = setupViewModel()

        // When
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            expectThat(state.showDetailScreen).isFalse()
            expectThat(state.selectedVocabulary).isNull()
        }
    }

    @Test
    fun `showVocabularyDetail sets selectedVocabulary and showDetailScreen to true`() = runTest(testDispatcher) {
        // Given
        val viewModel = setupViewModel()
        val vocabulary = createTestVocabulary()

        // When
        viewModel.showVocabularyDetail(vocabulary)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            expectThat(state.showDetailScreen).isTrue()
            expectThat(state.selectedVocabulary).isEqualTo(vocabulary)
        }
    }

    @Test
    fun `hideVocabularyDetail sets showDetailScreen to false`() = runTest(testDispatcher) {
        // Given
        val viewModel = setupViewModel()
        val vocabulary = createTestVocabulary()

        // When
        viewModel.showVocabularyDetail(vocabulary)
        advanceUntilIdle()
        viewModel.hideVocabularyDetail()
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            expectThat(state.showDetailScreen).isFalse()
            // Note: selectedVocabulary is not cleared, only the visibility changes
            expectThat(state.selectedVocabulary).isEqualTo(vocabulary)
        }
    }

    private fun setupViewModel(
        repository: VocabularyRepository = VocabularyRepositoryFake(),
        savedState: SavedStateHandle = SavedStateHandle(
            initialState = mapOf(
                "containerId" to 1,
                "containerName" to "name",
            ),
        ),
        dispatcher: CoroutineDispatcher = testDispatcher,
    ): OverviewViewModel {
        return OverviewViewModel(
            savedStateHandle = savedState,
            repository = repository,
            userPreferencesManager = userPreferencesManager,
            ioDispatcher = dispatcher,
        )
    }

    private fun createTestVocabulary(): Vocabulary {
        return Vocabulary(
            word = "testWord",
            wordHighlights = emptyList(),
            translation = "testTranslation",
            containerId = 1,
            id = 1,
        )
    }
}
