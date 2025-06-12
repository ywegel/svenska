package de.ywegel.svenska.ui.bonus

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.ywegel.svenska.data.vocabulary
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import de.ywegel.svenska.ui.bonus.favorites.FavoritesAndPronunciationViewModel
import de.ywegel.svenska.ui.container.BonusScreen
import io.mockk.clearAllMocks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
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
        )
    }


    @Test
    fun `Only Favorites vocabularies are emitted for Favorites`() = runTest(testDispatcher) {
        val viewModel = createViewModelForScreen(BonusScreen.Favorites)

        advanceUntilIdle()

        viewModel.bonusItems.test {
            val result = awaitItem()
            expectThat(result.size).isEqualTo(2)
            expectThat(result).containsExactlyInAnyOrder(favoriteWords)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Only pronunciation vocabularies are emitted for SpecialPronunciation`() = runTest(testDispatcher) {
        val viewModel = createViewModelForScreen(BonusScreen.SpecialPronunciation)

        advanceUntilIdle()

        viewModel.bonusItems.test {
            val result = awaitItem()
            expectThat(result.size).isEqualTo(2)
            expectThat(result).containsExactlyInAnyOrder(specialPronunciationWords)
        }
    }

    @Test
    fun `Given unsupported screenType, When ViewModel is created, Then throws IllegalStateException`() =
        runTest(testDispatcher) {
            val exception = assertThrows<IllegalStateException> {
                val _ignore = createViewModelForScreen(BonusScreen.Quiz)
                advanceUntilIdle()
            }

            assertTrue(exception.message!!.contains("This can't happen"))
        }
}
