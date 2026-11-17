@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.addEdit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
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

    @Nested
    inner class Initialisation {
        @Test
        fun `ViewModel starts empty without an initial vocabulary`() {
            // When
            val viewModel = setupViewModel()

            // Then
            expectThat(viewModel.uiState.value).isEqualTo(AddEditUiState())
        }

        @Test
        fun `ViewModel restores the input of an initial vocabulary`() {
            // Given
            val initial = Vocabulary(
                word = "känna",
                wordSuffix = "sig",
                wordHighlights = listOf(1 to 2),
                translation = "sich fühlen",
                wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
                ending = "-er -de -t",
                notes = "testNote",
                irregularPronunciation = "testPronunciation",
                containerId = 1,
            )

            // When
            val viewModel = setupViewModel(initialVocabulary = initial)

            // Then
            expectThat(viewModel.uiState.value).isEqualTo(
                AddEditUiState(
                    selectedWordGroup = ViewWordGroup.Verb,
                    selectedSubGroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
                    wordWithAnnotation = "k*ä*nna sig",
                    chosenChangingIndex = 0,
                    translation = "sich fühlen",
                    endings = "-er -de -t",
                    notes = "testNote",
                    isIrregularPronunciation = true,
                    irregularPronunciation = "testPronunciation",
                    editingExistingVocabulary = "känna",
                ),
            )
        }

        @Test
        fun `Stored forms that the endings build open in endings mode and count as confirmed`() {
            // When
            val viewModel = setupViewModel(initialVocabulary = penna(form2 = "pennor"))

            // Then
            val state = viewModel.uiState.value
            expectThat(state.formsMode).isEqualTo(FormsMode.Endings)
            expectThat(state.confirmedFormInputs).isEqualTo(state.formInputs)
        }

        @Test
        fun `Stored forms that the endings don't build open as written out forms`() {
            // When
            val viewModel = setupViewModel(initialVocabulary = penna(form2 = "pennar"))

            // Then
            expectThat(viewModel.uiState.value) {
                get { formsMode }.isEqualTo(FormsMode.WrittenOut)
                get { forms }.isEqualTo(listOf("pennan", "pennar", "pennorna"))
            }
        }

        @Test
        fun `Words without stored forms show the review hint, if the built forms need a review`() {
            // When
            val viewModel = setupViewModel(initialVocabulary = exempel())

            // Then
            expectThat(viewModel.uiState.value.showReviewHint).isTrue()
        }
    }

    @Test
    fun `Typing an article sets the gender of a noun`() {
        // Given
        val viewModel = setupViewModel()
        viewModel.updateSelectedWordGroup(ViewWordGroup.Noun)

        // When
        viewModel.updateWordWithAnnotation("ett hus")

        // Then
        expectThat(viewModel.uiState.value.gender).isEqualTo(Gender.Neutra)
    }

    @Nested
    inner class FormsModes {
        @Test
        fun `Writing out the forms starts with the built forms`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = penna())

            // When
            viewModel.switchToWrittenOutForms()

            // Then
            expectThat(viewModel.uiState.value) {
                get { formsMode }.isEqualTo(FormsMode.WrittenOut)
                get { writtenForms }.isEqualTo(listOf("pennan", "pennor", "pennorna"))
            }
        }

        @Test
        fun `Unchanged written out forms switch back to the endings without asking`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = penna())
            viewModel.switchToWrittenOutForms()

            // When
            viewModel.switchToEndings()

            // Then
            expectThat(viewModel.uiState.value) {
                get { formsMode }.isEqualTo(FormsMode.Endings)
                get { showSwitchToEndingsDialog }.isFalse()
            }
        }

        @Test
        fun `Changed written out forms ask before they are replaced`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = penna())
            viewModel.switchToWrittenOutForms()
            viewModel.updateWrittenForm(index = 1, form = "pennar")

            // When
            viewModel.switchToEndings()

            // Then
            expectThat(viewModel.uiState.value) {
                get { formsMode }.isEqualTo(FormsMode.WrittenOut)
                get { showSwitchToEndingsDialog }.isTrue()
            }
        }

        @Test
        fun `Confirming the dialog switches back to the endings`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = penna())
            viewModel.switchToWrittenOutForms()
            viewModel.updateWrittenForm(index = 1, form = "pennar")
            viewModel.switchToEndings()

            // When
            viewModel.confirmSwitchToEndings()

            // Then
            expectThat(viewModel.uiState.value) {
                get { formsMode }.isEqualTo(FormsMode.Endings)
                get { showSwitchToEndingsDialog }.isFalse()
            }
        }
    }

    @Nested
    inner class Review {
        @Test
        fun `Confirmed forms hide the review hint`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = exempel())

            // When
            viewModel.confirmForms()

            // Then
            expectThat(viewModel.uiState.value.showReviewHint).isFalse()
        }

        @Test
        fun `Changed endings need a new review`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = exempel())
            viewModel.confirmForms()

            // When
            viewModel.updateEndings("-et -er -erna")

            // Then
            expectThat(viewModel.uiState.value.showReviewHint).isTrue()
        }

        @Test
        fun `Endings changed back to the confirmed ones stay confirmed`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = exempel())
            viewModel.confirmForms()
            viewModel.updateEndings("-et")

            // When
            viewModel.updateEndings("-et -  -en")

            // Then
            expectThat(viewModel.uiState.value.showReviewHint).isFalse()
        }
    }

    @Test
    fun `Applying the suggestion selects the suggested word group`() {
        // Given
        val viewModel = setupViewModel()
        viewModel.updateWordWithAnnotation("penna")
        viewModel.updateEndings("-n -or -orna")

        // When
        viewModel.applySuggestedWordGroup()

        // Then
        expectThat(viewModel.uiState.value) {
            get { selectedWordGroup }.isEqualTo(ViewWordGroup.Noun)
            get { selectedSubGroup }.isEqualTo(ViewWordSubGroup.Noun(WordGroup.NounSubgroup.OR))
            get { gender }.isEqualTo(Gender.Ultra)
        }
    }

    @Nested
    inner class Saving {
        @Test
        fun `saveAndNavigateUp runs the use case and upserts the vocabulary correctly`() = runTest(testDispatcher) {
            // Given
            val expectedVocabulary = vocabulary()
            val mapper = mockk<MapUiStateToVocabularyUseCase> {
                every { this@mockk.invoke(any(), any(), any()) } returns expectedVocabulary
            }
            val repository = mockk<VocabularyRepository>(relaxed = true)
            val viewModel = setupViewModel(mapper = mapper, repository = repository)

            // When
            viewModel.saveAndNavigateUp()
            advanceUntilIdle()

            // Then
            coVerify { repository.upsertVocabulary(expectedVocabulary) }
            viewModel.uiEvents.test {
                expectThat(awaitItem()).isEqualTo(AddEditViewModel.UiEvent.NavigateUp)
                expectNoEvents()
            }
        }

        @Test
        fun `saveAndNavigateUp emits an error event if the use case fails`() = runTest(testDispatcher) {
            // Given
            val mapper = mockk<MapUiStateToVocabularyUseCase> {
                every { this@mockk.invoke(any(), any(), any()) } returns null
            }
            val repository = mockk<VocabularyRepository>(relaxed = true)
            val viewModel = setupViewModel(mapper = mapper, repository = repository)

            // When
            viewModel.saveAndNavigateUp()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 0) { repository.upsertVocabulary(any()) }
            viewModel.uiEvents.test {
                expectThat(awaitItem()).isEqualTo(AddEditViewModel.UiEvent.InvalidWordGroupConfiguration)
                expectNoEvents()
            }
        }

        @Test
        fun `saveAndNavigateUp does nothing while the highlights are invalid`() = runTest(testDispatcher) {
            // Given
            val mapper = mockk<MapUiStateToVocabularyUseCase>()
            val viewModel = setupViewModel(mapper = mapper)
            viewModel.updateSelectedWordGroup(ViewWordGroup.Verb)
            viewModel.updateWordWithAnnotation("känna s*i*g")

            // When
            viewModel.saveAndNavigateUp()
            advanceUntilIdle()

            // Then
            verify(exactly = 0) { mapper.invoke(any(), any(), any()) }
            viewModel.uiEvents.test { expectNoEvents() }
        }
    }

    private fun penna(form2: String? = null) = Vocabulary(
        word = "penna",
        translation = "pencil",
        gender = Gender.Ultra,
        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.OR),
        ending = "-n -or -orna",
        form1 = form2?.let { "pennan" },
        form2 = form2,
        form3 = form2?.let { "pennorna" },
        containerId = 1,
    )

    private fun exempel() = Vocabulary(
        word = "exempel",
        translation = "Beispiel",
        gender = Gender.Neutra,
        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_ETT),
        ending = "-et - -en",
        containerId = 1,
    )

    private fun setupViewModel(
        initialVocabulary: Vocabulary? = null,
        repository: VocabularyRepository = VocabularyRepositoryFake(),
        dispatcher: CoroutineDispatcher = testDispatcher,
        mapper: MapUiStateToVocabularyUseCase = MapUiStateToVocabularyUseCase(),
    ): AddEditViewModel {
        return AddEditViewModel(
            savedStateHandle = SavedStateHandle(
                initialState = mapOf("containerId" to 1, "initialVocabulary" to initialVocabulary),
            ),
            repository = repository,
            ioDispatcher = dispatcher,
            immediateDispatcher = dispatcher,
            mapUiStateToVocabularyUseCase = mapper,
        )
    }
}
