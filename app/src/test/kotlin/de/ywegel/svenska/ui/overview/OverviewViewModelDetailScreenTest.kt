@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.overview

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.vocabulary
import de.ywegel.svenska.domain.ToggleVocabularyFavoriteUseCase
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import de.ywegel.svenska.ui.detail.VocabularyDetailState
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
import strikt.assertions.isA
import strikt.assertions.isEqualTo

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
    fun `initial state is hidden`() = runTest(testDispatcher) {
        // Given
        val viewModel = setupViewModel()

        // When
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            expectThat(state.detailViewState).isA<VocabularyDetailState.Hidden>()
        }
    }

    @Test
    fun `showVocabularyDetail shows the detail screen`() = runTest(testDispatcher) {
        // Given
        val viewModel = setupViewModel()
        val vocabulary = vocabulary()

        advanceUntilIdle()

        // When
        viewModel.onVocabularyClick(vocabulary, false)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            expectThat(state.detailViewState).isA<VocabularyDetailState.Visible>()
            val detailViewState = state.detailViewState
            detailViewState as VocabularyDetailState.Visible
            expectThat(detailViewState.selectedVocabulary).isEqualTo(vocabulary)
        }
    }

    @Test
    fun `hideVocabularyDetail hides the detail screen`() = runTest(testDispatcher) {
        // Given
        val viewModel = setupViewModel()
        val vocabulary = vocabulary()

        // When
        viewModel.onVocabularyClick(vocabulary, false)
        advanceUntilIdle()
        viewModel.onDismissVocabularyDetail()
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            expectThat(state.detailViewState).isA<VocabularyDetailState.Hidden>()
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
            toggleVocabularyFavoriteUseCase = ToggleVocabularyFavoriteUseCase(repository, testDispatcher),
        )
    }
}
