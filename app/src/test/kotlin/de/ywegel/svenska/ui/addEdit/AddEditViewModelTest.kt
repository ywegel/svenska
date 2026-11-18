@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.addEdit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
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
                    irregularPronunciation = "testPronunciation",
                    existingWord = "känna",
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
        fun `Stored forms with a pending review show the review hint again`() {
            // Given
            val initial = exempel().copy(
                form1 = "exempelet",
                form2 = "exempel",
                form3 = "exempelen",
                formsNeedReview = true,
            )

            // When
            val viewModel = setupViewModel(initialVocabulary = initial)

            // Then
            expectThat(viewModel.uiState.value) {
                get { formsMode }.isEqualTo(FormsMode.Endings)
                get { showReviewHint }.isTrue()
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

    @Test
    fun `Selecting noun sets the gender of an article typed before`() {
        // Given
        val viewModel = setupViewModel()
        viewModel.updateWordWithAnnotation("ett hus")
        expectThat(viewModel.uiState.value.gender).isEqualTo(Gender.Ultra)

        // When
        viewModel.updateSelectedWordGroup(ViewWordGroup.Noun)

        // Then
        expectThat(viewModel.uiState.value.gender).isEqualTo(Gender.Neutra)
    }

    @ParameterizedTest
    @MethodSource("wordGroupSelections")
    fun `Selecting a word group keeps the subgroup only for the same group`(
        group: ViewWordGroup,
        expectedSubGroup: ViewWordSubGroup,
    ) {
        // Given
        val viewModel = setupViewModel(initialVocabulary = penna())
        expectThat(viewModel.uiState.value.selectedWordGroup).isEqualTo(ViewWordGroup.Noun)
        expectThat(viewModel.uiState.value.selectedSubGroup).isEqualTo(ViewWordSubGroup.Noun(WordGroup.NounSubgroup.OR))

        // When
        viewModel.updateSelectedWordGroup(group)

        // Then
        expectThat(viewModel.uiState.value.selectedSubGroup).isEqualTo(expectedSubGroup)
    }

    @Test
    fun `Tapping a word makes it the changing word`() {
        // Given
        val viewModel = setupViewModel()
        viewModel.updateSelectedWordGroup(ViewWordGroup.Verb)
        viewModel.updateWordWithAnnotation("ta en dusch")
        expectThat(viewModel.uiState.value.wordInput.word).isEqualTo("ta")

        // When
        viewModel.selectChangingWord(2)

        // Then
        expectThat(viewModel.uiState.value.wordInput.word).isEqualTo("dusch")
    }

    @ParameterizedTest
    @MethodSource("typingAfterTappingAWord")
    fun `Typing keeps the tapped word only while the number of words stays the same`(
        keystrokes: List<String>,
        expectedWord: String,
    ) {
        // Given
        val viewModel = setupViewModel()
        viewModel.updateSelectedWordGroup(ViewWordGroup.Noun)
        viewModel.updateWordWithAnnotation("kopp kaffe")
        viewModel.selectChangingWord(0)

        // When
        keystrokes.forEach(viewModel::updateWordWithAnnotation)

        // Then
        expectThat(viewModel.uiState.value.wordInput.word).isEqualTo(expectedWord)
    }

    @Nested
    inner class FormsModes {
        @Test
        fun `A word group with more forms adds a field for the missing form`() {
            // Given
            val viewModel = setupViewModel()
            viewModel.updateSelectedWordGroup(ViewWordGroup.Adjective)
            viewModel.updateWordWithAnnotation("glad")
            viewModel.switchToWrittenOutForms()

            // When
            viewModel.updateSelectedWordGroup(ViewWordGroup.Noun)

            // Then
            expectThat(viewModel.uiState.value.forms).isEqualTo(listOf("", "", ""))
        }

        @Test
        fun `A word group with fewer forms hides the extra form until switching back`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = penna(form2 = "pennar"))

            // When
            viewModel.updateSelectedWordGroup(ViewWordGroup.Adjective)
            val adjectiveForms = viewModel.uiState.value.forms
            viewModel.updateSelectedWordGroup(ViewWordGroup.Noun)

            // Then
            expectThat(adjectiveForms).isEqualTo(listOf("pennan", "pennar"))
            expectThat(viewModel.uiState.value.forms).isEqualTo(listOf("pennan", "pennar", "pennorna"))
        }

        @Test
        fun `A form the stored forms left empty can still be written out`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = penna().copy(form2 = "pennar"))

            // When
            viewModel.updateWrittenForm(index = 2, form = "pennorna")

            // Then
            expectThat(viewModel.uiState.value.forms).isEqualTo(listOf("", "pennar", "pennorna"))
        }

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
        fun `Unchanged written out forms do not differ from the endings`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = penna())

            // When
            viewModel.switchToWrittenOutForms()

            // Then
            expectThat(viewModel.uiState.value.writtenFormsDifferFromEndings).isFalse()
        }

        @Test
        fun `Changed written out forms differ from the endings`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = penna())
            viewModel.switchToWrittenOutForms()

            // When
            viewModel.updateWrittenForm(index = 1, form = "pennar")

            // Then
            expectThat(viewModel.uiState.value.writtenFormsDifferFromEndings).isTrue()
        }

        @Test
        fun `Switching to the endings replaces the written out forms`() {
            // Given
            val viewModel = setupViewModel(initialVocabulary = penna())
            viewModel.switchToWrittenOutForms()
            viewModel.updateWrittenForm(index = 1, form = "pennar")

            // When
            viewModel.switchToEndings()

            // Then
            expectThat(viewModel.uiState.value) {
                get { formsMode }.isEqualTo(FormsMode.Endings)
                get { forms }.isEqualTo(listOf("pennan", "pennor", "pennorna"))
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

    @Test
    fun `Applying without a suggestion changes nothing`() {
        // Given
        val viewModel = setupViewModel(initialVocabulary = penna())
        val before = viewModel.uiState.value
        expectThat(before.suggestedWordGroup).isNull()

        // When
        viewModel.applySuggestedWordGroup()

        // Then
        expectThat(viewModel.uiState.value).isEqualTo(before)
    }

    @Nested
    inner class Saving {
        @Test
        fun `A valid input is upserted and the screen navigates up`() = runTest(testDispatcher) {
            // Given
            val repository = mockk<VocabularyRepository>(relaxed = true)
            val viewModel = setupViewModel(initialVocabulary = penna(), repository = repository)

            // When
            viewModel.saveAndNavigateUp()
            advanceUntilIdle()

            // Then
            coVerify { repository.upsertVocabulary(match { it.word == "penna" }) }
            viewModel.uiEvents.test {
                expectThat(awaitItem()).isEqualTo(AddEditViewModel.UiEvent.NavigateUp)
                expectNoEvents()
            }
        }

        @Test
        fun `Confirmed forms are saved without the review flag`() = runTest(testDispatcher) {
            // Given
            val repository = mockk<VocabularyRepository>(relaxed = true)
            val viewModel = setupViewModel(initialVocabulary = exempel(), repository = repository)
            viewModel.confirmForms()

            // When
            viewModel.saveAndNavigateUp()
            advanceUntilIdle()

            // Then
            coVerify { repository.upsertVocabulary(match { !it.formsNeedReview }) }
        }

        @Test
        fun `A missing word group is reported and nothing is saved`() = runTest(testDispatcher) {
            // Given
            val repository = mockk<VocabularyRepository>(relaxed = true)
            val viewModel = setupViewModel(repository = repository)

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
        fun `Invalid highlights are not reported again and nothing is saved`() = runTest(testDispatcher) {
            // Given
            val repository = mockk<VocabularyRepository>(relaxed = true)
            val viewModel = setupViewModel(repository = repository)
            viewModel.updateSelectedWordGroup(ViewWordGroup.Verb)
            viewModel.updateSelectedSubWordGroup(ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A))
            viewModel.updateWordWithAnnotation("känna s*i*g")

            // When
            viewModel.saveAndNavigateUp()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 0) { repository.upsertVocabulary(any()) }
            viewModel.uiEvents.test { expectNoEvents() }
        }
    }

    @Nested
    inner class Deleting {
        @Test
        fun `An existing vocabulary is deleted and the screen navigates up`() = runTest(testDispatcher) {
            // Given
            val repository = mockk<VocabularyRepository>(relaxed = true)
            val initial = penna()
            val viewModel = setupViewModel(initialVocabulary = initial, repository = repository)

            // When
            viewModel.deleteVocabulary()
            advanceUntilIdle()

            // Then
            coVerify { repository.deleteVocabulary(initial) }
            viewModel.uiEvents.test {
                expectThat(awaitItem()).isEqualTo(AddEditViewModel.UiEvent.NavigateUp)
                expectNoEvents()
            }
        }

        @Test
        fun `A new vocabulary is not deleted, but the screen navigates up`() = runTest(testDispatcher) {
            // Given
            val repository = mockk<VocabularyRepository>(relaxed = true)
            val viewModel = setupViewModel(repository = repository)

            // When
            viewModel.deleteVocabulary()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 0) { repository.deleteVocabulary(any()) }
            viewModel.uiEvents.test {
                expectThat(awaitItem()).isEqualTo(AddEditViewModel.UiEvent.NavigateUp)
                expectNoEvents()
            }
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
    ): AddEditViewModel {
        return AddEditViewModel(
            savedStateHandle = SavedStateHandle(
                initialState = mapOf("containerId" to 1, "initialVocabulary" to initialVocabulary),
            ),
            repository = repository,
            ioDispatcher = dispatcher,
            immediateDispatcher = dispatcher,
        )
    }

    companion object {
        @JvmStatic
        fun wordGroupSelections(): List<Arguments> = listOf(
            argumentSet("another group resets the subgroup", ViewWordGroup.Verb, ViewWordSubGroup.None),
            argumentSet(
                "the same group keeps the subgroup",
                ViewWordGroup.Noun,
                ViewWordSubGroup.Noun(WordGroup.NounSubgroup.OR),
            ),
        )

        /** Starts from "kopp kaffe" with "kopp" tapped. Without a tapped word, a noun changes its last word. */
        @JvmStatic
        fun typingAfterTappingAWord(): List<Arguments> = listOf(
            argumentSet("fixing a typo in the tapped word keeps it", listOf("kop kaffe", "kopa kaffe"), "kopa"),
            argumentSet("fixing a typo in another word keeps it", listOf("kopp kaffee"), "kopp"),
            argumentSet("a trailing space keeps it", listOf("kopp kaffe "), "kopp"),
            argumentSet(
                "splitting the tapped word resets it",
                listOf("skopp kaffe", "stkopp kaffe", "stokopp kaffe", "storkopp kaffe", "stor kopp kaffe"),
                "kaffe",
            ),
            argumentSet(
                "a new word typed after a leading space resets it",
                listOf(" kopp kaffe", "s kopp kaffe", "st kopp kaffe", "sto kopp kaffe", "stor kopp kaffe"),
                "kaffe",
            ),
        )
    }
}
