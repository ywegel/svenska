@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.addEdit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.domain.addEdit.MapUiStateToVocabularyUseCase
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

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
            annotationInformationHidden = false,
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
            annotationInformationHidden = false,
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
    fun `ViewModel with navigation data correctly reconstructs pronunciations`() = runTest(testDispatcher) {
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
            annotationInformationHidden = false,
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

    @Test
    fun `saveAndNavigateUp runs the use case and upserts the vocabulary correctly`() = runTest {
        // Given
        val expectedVocabulary = vocabulary()
        val mockedMapUiStateToVocabularyUseCase = mockk<MapUiStateToVocabularyUseCase> {
            every { this@mockk.invoke(any(), any(), any()) } returns expectedVocabulary
        }

        val mockedRepository = mockk<VocabularyRepository>(relaxed = true)

        val viewModel = setupViewModel(
            mapUiStateToVocabularyUseCase = mockedMapUiStateToVocabularyUseCase,
            repository = mockedRepository,
        )

        // When
        viewModel.saveAndNavigateUp()

        advanceUntilIdle()

        // Then
        coVerify {
            mockedMapUiStateToVocabularyUseCase.invoke(
                snapshot = any(),
                initialVocabulary = any(),
                containerId = any(),
            )
        }

        coVerify { mockedRepository.upsertVocabulary(expectedVocabulary) }

        viewModel.uiEvents.test {
            expectThat(awaitItem()).isEqualTo(AddEditViewModel.UiEvent.NavigateUp)
            expectNoEvents()
        }
    }

    @Test
    fun `saveAndNavigateUp emits an error event if the use case fails`() = runTest(testDispatcher) {
        // Given
        val mockedMapUiStateToVocabularyUseCase = mockk<MapUiStateToVocabularyUseCase> {
            // null equals an error in the use case
            every { this@mockk.invoke(any(), any(), any()) } returns null
        }

        val mockedRepository = mockk<VocabularyRepository>(relaxed = true)

        val viewModel = setupViewModel(
            mapUiStateToVocabularyUseCase = mockedMapUiStateToVocabularyUseCase,
            repository = mockedRepository,
        )

        // When
        viewModel.saveAndNavigateUp()

        advanceUntilIdle()

        // Then
        coVerify {
            mockedMapUiStateToVocabularyUseCase.invoke(
                snapshot = any(),
                initialVocabulary = any(),
                containerId = any(),
            )
        }

        coVerify(exactly = 0) { mockedRepository.upsertVocabulary(any()) }

        viewModel.uiEvents.test {
            expectThat(awaitItem()).isEqualTo(AddEditViewModel.UiEvent.InvalidWordGroupConfiguration)
            expectNoEvents()
        }
    }

    @Test
    fun `hideAnnotationInfo sets the annotation information to hidden in the preferences`() = runTest(testDispatcher) {
        // Given
        val mockedPreferences = mockk<UserPreferencesManager>(relaxed = true)
        val viewModel = setupViewModel(userPreferencesManager = mockedPreferences)

        // When
        viewModel.hideAnnotationInfo()

        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            expectThat(awaitItem().annotationInformationHidden).isTrue()
        }

        coVerify(exactly = 1) { mockedPreferences.setAnnotationInformationHidden() }
    }

    private fun setupViewModel(
        repository: VocabularyRepository = VocabularyRepositoryFake(),
        savedState: SavedStateHandle = SavedStateHandle(initialState = mapOf("containerId" to 1)),
        dispatcher: CoroutineDispatcher = testDispatcher,
        userPreferencesManager: UserPreferencesManager = UserPreferencesManagerFake(),
        mapUiStateToVocabularyUseCase: MapUiStateToVocabularyUseCase = MapUiStateToVocabularyUseCase(),
    ): AddEditViewModel {
        return AddEditViewModel(
            savedStateHandle = savedState,
            repository = repository,
            ioDispatcher = dispatcher,
            immediateDispatcher = dispatcher,
            mapUiStateToVocabularyUseCase = mapUiStateToVocabularyUseCase,
            userPreferencesManager = userPreferencesManager,
        )
    }
}
