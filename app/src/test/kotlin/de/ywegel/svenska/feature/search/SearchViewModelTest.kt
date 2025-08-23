package de.ywegel.svenska.feature.search

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.doesNotContain
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.domain.ToggleVocabularyFavoriteUseCase
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import de.ywegel.svenska.ui.search.SearchViewModel
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
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel
    private lateinit var fakeRepo: VocabularyRepositoryFake
    private lateinit var fakePrefs: UserPreferencesManagerFake
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = VocabularyRepositoryFake()
        fakePrefs = UserPreferencesManagerFake()
        viewModel = SearchViewModel(
            savedStateHandle = SavedStateHandle(),
            containerRepository = fakeRepo,
            searchRepository = fakeRepo,
            userPreferencesManager = fakePrefs,
            ioDispatcher = testDispatcher,
            toggleVocabularyFavoriteUseCase = ToggleVocabularyFavoriteUseCase(
                repository = fakeRepo,
                ioDispatcher = testDispatcher,
            ),
        )
    }

    @AfterEach
    fun cleanUp() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateSearchQuery updates internal state`() = runTest(testDispatcher) {
        viewModel.updateSearchQuery("hund")
        assertThat(viewModel.searchQuery.value).isEqualTo("hund")
    }

    @Test
    fun `onSearch adds query to last searched items`() = runTest(testDispatcher) {
        // Given
        val query = "hund"

        // When
        viewModel.onSearch(query)
        advanceUntilIdle()

        // Then
        val items = viewModel.uiState.value.lastSearchedItems
        assertThat(items).containsExactly(query)
    }

    @Test
    fun `onSearch maintains only 5 last searched items`() = runTest(testDispatcher) {
        repeat(9) { viewModel.onSearch("word$it") }

        advanceUntilIdle()

        val items = viewModel.uiState.value.lastSearchedItems
        assertThat(items).all {
            hasSize(8)
            containsExactlyInAnyOrder("word1", "word2", "word3", "word4", "word5", "word6", "word7", "word8")
            doesNotContain("word0")
        }
    }

    @Test
    fun `onSearch saves last searched items in preferences`() = runTest(testDispatcher) {
        val testQuery = "katt"

        fakePrefs.preferencesSearchFlow.test {
            skipItems(1) // initial state

            viewModel.onSearch(testQuery)
            advanceUntilIdle()

            val updated = awaitItem()

            assertThat(updated.lastSearchedItems).containsExactlyInAnyOrder("katt")
        }
    }

    @Test
    fun `vocabularyFlow emits correct vocab after query update`() = runTest(testDispatcher) {
        fakeRepo.bulkInsertVocabularies(
            Vocabulary(
                word = "hund",
                translation = "dog",
                containerId = 1,
            ),
        )

        // When
        viewModel.updateSearchQuery("hund")
        advanceUntilIdle()

        // Then
        viewModel.vocabularyFlow.test {
            val result = awaitItem()

            expectThat(result).hasSize(1)
            expectThat(result.first().word).isEqualTo("hund")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePreferences updates last searched items`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            skipItems(1) // initial state

            fakePrefs.addLastSearchedItem("älg")
            fakePrefs.addLastSearchedItem("fisk")

            val updated = awaitItem()
            assertThat(updated.lastSearchedItems).containsExactlyInAnyOrder("älg", "fisk")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearch ignores duplicates`() = runTest {
        repeat(5) { viewModel.onSearch("same") }
        viewModel.onSearch("same")
        advanceUntilIdle()
        viewModel.uiState.test {
            val emission = awaitItem()
            expectThat(emission.lastSearchedItems.size).isEqualTo(1)
            expectThat(emission.lastSearchedItems).containsExactly("same")
        }
    }
}
