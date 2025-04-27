package de.ywegel.svenska.feature.search

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.all
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import de.ywegel.svenska.ui.search.SearchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.LinkedList

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel
    private lateinit var fakeRepo: VocabularyRepositoryFake
    private lateinit var fakePrefs: UserPreferencesManagerFake
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        fakeRepo = VocabularyRepositoryFake()
        fakePrefs = UserPreferencesManagerFake()
        viewModel = SearchViewModel(
            savedStateHandle = SavedStateHandle(),
            repository = fakeRepo,
            userPreferencesManager = fakePrefs,
            ioDispatcher = testDispatcher,
        )
    }

    @Test
    fun `updateSearchQuery updates internal state`() = runTest(testDispatcher) {
        viewModel.updateSearchQuery("hund")
        assertThat(viewModel.searchQuery.value).isEqualTo("hund")
    }

    @Test
    fun `onSearch adds query to last searched items`() = runTest(testDispatcher) {
        viewModel.onSearch("hund")
        val items = viewModel.uiState.value.lastSearchedItems
        assertThat(items).containsExactlyInAnyOrder("hund")
    }

    @Test
    fun `onSearch maintains only 5 last searched items`() = runTest(testDispatcher) {
        repeat(6) { viewModel.onSearch("word$it") }
        val items = viewModel.uiState.value.lastSearchedItems
        assertThat(items).all {
            hasSize(5)
            containsExactlyInAnyOrder("word1", "word2", "word3", "word4", "word5")
        }
    }

    @Test
    fun `onSearch saves last searched items in preferences`() = runTest(testDispatcher) {
        val testQuery = "katt"

        fakePrefs.preferencesOverviewFlow.test {
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

        viewModel.vocabularyFlow.test {
            // When
            viewModel.updateSearchQuery("hund")
            advanceUntilIdle()

            // Then
            val result = awaitItem()

            assertAll {
                assertThat(result).hasSize(1)
                assertThat(result.first().word).isEqualTo("hund")
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePreferences updates last searched items`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            skipItems(1) // initial state

            fakePrefs.updateOverviewLastSearchedItems(LinkedList(listOf("älg", "fisk")))

            val updated = awaitItem()
            assertThat(updated.lastSearchedItems).containsExactlyInAnyOrder("älg", "fisk")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearch with same query twice adds only once if already max size`() = runTest {
        repeat(5) { viewModel.onSearch("same") }
        viewModel.onSearch("same")
        assertThat(viewModel.uiState.value.lastSearchedItems.count { it == "same" }).isEqualTo(5)
    }
}
