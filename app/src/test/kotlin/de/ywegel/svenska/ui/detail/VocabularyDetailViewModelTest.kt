@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.detail

import de.ywegel.svenska.data.VocabularyRepository
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.mockk
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class VocabularyDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: VocabularyRepository

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `toggleFavorite calls repository toggleVocabularyFavorite with correct parameters`(isFavorite: Boolean) =
        runTest(testDispatcher) {
            // Given
            val viewModel = setupViewModel()
            val vocabularyId = 123

            // When
            viewModel.toggleFavorite(vocabularyId, isFavorite)
            advanceUntilIdle()

            // Then
            coVerify { repository.toggleVocabularyFavorite(vocabularyId, isFavorite) }
        }

    private fun setupViewModel(dispatcher: CoroutineDispatcher = testDispatcher): VocabularyDetailViewModel {
        return VocabularyDetailViewModel(
            repository = repository,
            ioDispatcher = dispatcher,
        )
    }
}
