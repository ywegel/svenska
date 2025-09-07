@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.addEdit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.addEdit.MapUiStateToVocabularyUseCase
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
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

class AddEditViewModelTest {

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
    fun `ViewModel is initialised with default data, if no initial Vocabulary is passed`() = runTest(testDispatcher) {
        val viewModel = setupViewModel()

        val expected = AddEditUiState(
            selectedWordGroup = null,
            selectedSubGroup = ViewWordSubGroup.None,
            gender = null,
            wordWithAnnotation = "",
            translation = "",
            ending = "",
            notes = "",
            isIrregularPronunciation = false,
            irregularPronunciation = null,
            isFavorite = false,
            editingExistingVocabulary = null,
        )

        advanceUntilIdle()

        viewModel.uiState.test {
            assertThat(awaitItem())
                .isEqualTo(expected)
        }
    }

    @Test
    fun `ViewModel is initialised with navigation data, if a initial Vocabulary is passed`() = runTest(testDispatcher) {
        val expected = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Verb,
            selectedSubGroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
            gender = null,
            wordWithAnnotation = "testWord",
            translation = "testTranslation",
            ending = "-er -de -t",
            notes = "testNote",
            isIrregularPronunciation = true,
            irregularPronunciation = "testPronunciation",
            isFavorite = false,
            editingExistingVocabulary = "testWord",
        )

        // Given
        val containerId = 1

        val navigationVocabulary = Vocabulary(
            word = "testWord",
            wordHighlights = emptyList(),
            translation = "testTranslation",
            gender = null,
            wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
            ending = "-er -de -t",
            notes = "testNote",
            irregularPronunciation = "testPronunciation",
            isFavorite = false,
            containerId = containerId,
            id = 1,
        )

        // When
        val viewModel = setupViewModel(
            savedState = SavedStateHandle(
                initialState = mapOf(
                    "containerId" to containerId,
                    "initialVocabulary" to navigationVocabulary,
                ),
            ),
        )

        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            expectThat(awaitItem())
                .isEqualTo(expected)
        }
    }
    
    @Test
    fun `ViewModel is initialised with navigation data and correctly reconstructs pronunciations`() = runTest(testDispatcher) {
        val expected = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Verb,
            selectedSubGroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
            gender = null,
            wordWithAnnotation = "t*e*st*With*An**not*at*ions",
            translation = "testTranslation",
            ending = "-er -de -t",
            notes = "testNote",
            isIrregularPronunciation = true,
            irregularPronunciation = "testPronunciation",
            isFavorite = false,
            editingExistingVocabulary = "testWithAnnotations",
        )

        // Given
        val containerId = 1

        val navigationVocabulary = Vocabulary(
            word = "testWithAnnotations",
            wordHighlights = listOf(1 to 2, 4 to 8, 10 to 10, 13 to 15),
            translation = "testTranslation",
            gender = null,
            wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
            ending = "-er -de -t",
            notes = "testNote",
            irregularPronunciation = "testPronunciation",
            isFavorite = false,
            containerId = containerId,
            id = 1,
        )

        // When
        val viewModel = setupViewModel(
            savedState = SavedStateHandle(
                initialState = mapOf(
                    "containerId" to containerId,
                    "initialVocabulary" to navigationVocabulary,
                ),
            ),
        )

        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            expectThat(awaitItem())
                .isEqualTo(expected)
        }
    }

    private fun setupViewModel(
        repository: VocabularyRepository = VocabularyRepositoryFake(),
        savedState: SavedStateHandle = SavedStateHandle(initialState = mapOf("containerId" to 1)),
        dispatcher: CoroutineDispatcher = testDispatcher,
        mapUiStateToVocabularyUseCase: MapUiStateToVocabularyUseCase = MapUiStateToVocabularyUseCase(),
    ): AddEditViewModel {
        return AddEditViewModel(
            savedStateHandle = savedState,
            repository = repository,
            ioDispatcher = dispatcher,
            immediateDispatcher = dispatcher,
            mapUiStateToVocabularyUseCase = mapUiStateToVocabularyUseCase,
        )
    }
}
