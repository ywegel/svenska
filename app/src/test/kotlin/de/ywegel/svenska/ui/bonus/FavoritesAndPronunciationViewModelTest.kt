package de.ywegel.svenska.ui.bonus

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.ywegel.svenska.data.vocabulary
import de.ywegel.svenska.domain.ToggleVocabularyFavoriteUseCase
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import de.ywegel.svenska.ui.bonus.favorites.FavoritesAndPronunciationViewModel
import de.ywegel.svenska.ui.bonus.favorites.FavoritesUiState
import de.ywegel.svenska.ui.container.BonusScreen
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesAndPronunciationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: VocabularyRepositoryFake

    private val favoriteWords = listOf(
        vocabulary(id = 1, isFavorite = true),
        vocabulary(id = 2, isFavorite = true),
    )

    private val specialPronunciationWords = listOf(
        vocabulary(id = 3, irregularPronunciation = "a"),
        vocabulary(id = 4, irregularPronunciation = "a"),
    )

    private val regularWords = listOf(
        vocabulary(id = 5),
    )

    private val words = favoriteWords + specialPronunciationWords + regularWords

    @BeforeEach
    fun setup() {
        repository = VocabularyRepositoryFake(words)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    private fun createViewModelForScreen(screen: BonusScreen): FavoritesAndPronunciationViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf("screenType" to screen),
        )
        return FavoritesAndPronunciationViewModel(
            repository = repository,
            savedStateHandle = savedStateHandle,
            ioDispatcher = testDispatcher,
            toggleVocabularyFavoriteUseCase = ToggleVocabularyFavoriteUseCase(
                repository = repository,
                ioDispatcher = testDispatcher,
            ),
        )
    }

    @Test
    fun `Only Favorites vocabularies are emitted for Favorites`() = runTest(testDispatcher) {
        val viewModel = createViewModelForScreen(BonusScreen.Favorites)

        viewModel.uiState.test {
            assert(awaitItem() is FavoritesUiState.Loading)

            val successState = awaitItem() as FavoritesUiState.Success
            expectThat(successState.items.size).isEqualTo(2)
            expectThat(successState.items).containsExactlyInAnyOrder(favoriteWords)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Only pronunciation vocabularies are emitted for SpecialPronunciation`() = runTest(testDispatcher) {
        val viewModel = createViewModelForScreen(BonusScreen.SpecialPronunciation)

        viewModel.uiState.test {
            assert(awaitItem() is FavoritesUiState.Loading)

            val successState = awaitItem() as FavoritesUiState.Success
            expectThat(successState.items.size).isEqualTo(2)
            expectThat(successState.items).containsExactlyInAnyOrder(specialPronunciationWords)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `IllegalStateException when viewModel loads for unsopported screenType`() = runTest(testDispatcher) {
        val viewModel = createViewModelForScreen(BonusScreen.Quiz)

        viewModel.uiState.test {
            assert(awaitItem() is FavoritesUiState.Loading)

            val errorState = awaitItem() as FavoritesUiState.Error
            expectThat(errorState.message).contains("not supported")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given repository throws, When ViewModel loads, Then emit Error state`() = runTest(testDispatcher) {
        val throwingRepository = spyk(repository) {
            every { getFavorites(any()) } throws RuntimeException("Fetch failed")
        }

        val savedStateHandle = SavedStateHandle(mapOf("screenType" to BonusScreen.Favorites))
        val viewModel = FavoritesAndPronunciationViewModel(
            repository = throwingRepository,
            savedStateHandle = savedStateHandle,
            ioDispatcher = testDispatcher,
            toggleVocabularyFavoriteUseCase = ToggleVocabularyFavoriteUseCase(
                repository = throwingRepository,
                ioDispatcher = testDispatcher,
            ),
        )

        viewModel.uiState.test {
            assert(awaitItem() is FavoritesUiState.Loading)

            val errorState = awaitItem() as FavoritesUiState.Error
            expectThat(errorState.message).contains("Fetch failed")

            cancelAndIgnoreRemainingEvents()
        }
    }
}
