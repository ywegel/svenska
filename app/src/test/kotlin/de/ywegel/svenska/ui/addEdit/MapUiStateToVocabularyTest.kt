package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isGreaterThan
import strikt.assertions.isNull
import strikt.assertions.isTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class MapUiStateToVocabularyTest {

    @ParameterizedTest
    @MethodSource("unsavableInputs")
    fun `Reports why the input can't be saved`(uiState: AddEditUiState, expected: SaveResult) {
        // When
        val result = save(uiState)

        // Then
        expectThat(result).isEqualTo(expected)
    }

    @Test
    fun `Creates new vocabulary when initialVocabulary is null`() {
        // Given
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Noun,
            selectedSubGroup = ViewWordSubGroup.Noun(WordGroup.NounSubgroup.OR),
            wordWithAnnotation = "penna",
            translation = "pencil",
            gender = Gender.Ultra,
        )

        // When
        val result = save(uiState)

        // Then
        expectThat(result).savedVocabulary().and {
            get { word }.isEqualTo("penna")
            get { translation }.isEqualTo("pencil")
            get { wordGroup }.isEqualTo(WordGroup.Noun(WordGroup.NounSubgroup.OR))
            get { gender }.isEqualTo(Gender.Ultra)
            get { containerId }.isEqualTo(CONTAINER_ID)
            get { created }.isGreaterThan(0)
            get { lastEdited }.isGreaterThan(0)
        }
    }

    @Test
    fun `Preserves created value and updates everything else when editing existing vocabulary`() {
        // Given
        val initialCreationTime = Clock.System.now().minus(30.days).toEpochMilliseconds()
        val initial = vocabulary().copy(created = initialCreationTime, lastEdited = initialCreationTime)
        val uiState = AddEditUiState.fromExistingVocabulary(initial)

        // When
        val result = save(uiState, initialVocabulary = initial)

        // Then
        expectThat(result).savedVocabulary().and {
            get { created }.isEqualTo(initialCreationTime)
            get { lastEdited }.isGreaterThan(initial.lastEdited)
        }
    }

    @Test
    fun `Gender is null if word group is not noun`() {
        // Given
        val uiState = verb(wordWithAnnotation = "gå").copy(gender = Gender.Ultra)

        // When
        val result = save(uiState)

        // Then
        expectThat(result).savedVocabulary().get { gender }.isNull()
    }

    @ParameterizedTest
    @MethodSource("irregularPronunciations")
    fun `Saves the irregular pronunciation only when it is not blank`(input: String?, expected: String?) {
        // Given
        val uiState = verb(wordWithAnnotation = "gå").copy(irregularPronunciation = input)

        // When
        val result = save(uiState)

        // Then
        expectThat(result).savedVocabulary().get { irregularPronunciation }.isEqualTo(expected)
    }

    @Test
    fun `Word highlights and cleaned string are extracted from annotated input`() {
        // Given
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Noun,
            selectedSubGroup = ViewWordSubGroup.Noun(WordGroup.NounSubgroup.OR),
            wordWithAnnotation = "*p*enna",
        )

        // When
        val result = save(uiState)

        // Then
        expectThat(result).savedVocabulary().and {
            get { word }.isEqualTo("penna")
            get { wordHighlights }.isEqualTo(listOf(0 to 1))
        }
    }

    @Test
    fun `Saves prefix, suffix and the forms of the changing word`() {
        // Given
        val uiState = verb(wordWithAnnotation = "att känna sig", subgroup = WordGroup.VerbSubgroup.GROUP_2A)
            .copy(endings = "-er, -de, -t")

        // When
        val result = save(uiState)

        // Then
        expectThat(result).savedVocabulary().and {
            get { wordPrefix }.isEqualTo("")
            get { word }.isEqualTo("känna")
            get { wordSuffix }.isEqualTo("sig")
            get { ending }.isEqualTo("-er -de -t")
            get { listOf(form1, form2, form3) }.isEqualTo(listOf("känner", "kände", "känt"))
        }
    }

    @Test
    fun `Written out forms are saved as forms and as full word endings`() {
        // Given
        val uiState = writtenOutVerb("tar", " tog ", "tagit").copy(endings = "-r")

        // When
        val result = save(uiState)

        // Then
        expectThat(result).savedVocabulary().and {
            get { ending }.isEqualTo("tar tog tagit")
            get { listOf(form1, form2, form3) }.isEqualTo(listOf("tar", "tog", "tagit"))
            get { formsNeedReview }.isFalse()
        }
    }

    @Test
    fun `Empty written out forms at the end are saved`() {
        // Given
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Noun,
            selectedSubGroup = ViewWordSubGroup.Noun(WordGroup.NounSubgroup.SPECIAL),
            wordWithAnnotation = "mat",
            formsMode = FormsMode.WrittenOut,
            writtenForms = listOf("maten", "", ""),
        )

        // When
        val result = save(uiState)

        // Then
        expectThat(result).savedVocabulary().and {
            get { ending }.isEqualTo("maten")
            get { listOf(form1, form2, form3) }.isEqualTo(listOf("maten", null, null))
        }
    }

    @Test
    fun `Forms with a pending review are saved with the review flag`() {
        // Given
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Noun,
            selectedSubGroup = ViewWordSubGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_ETT),
            wordWithAnnotation = "exempel",
            endings = "-et - -en",
        )

        // When
        val result = save(uiState)

        // Then
        expectThat(result).savedVocabulary().get { formsNeedReview }.isTrue()
    }

    @Test
    fun `An adjective saves only two endings`() {
        // Given
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Adjective,
            wordWithAnnotation = "glad",
            endings = "-tt -a -are",
        )

        // When
        val result = save(uiState)

        // Then
        expectThat(result).savedVocabulary().and {
            get { ending }.isEqualTo("-tt -a")
            get { listOf(form1, form2, form3) }.isEqualTo(listOf("glatt", "glada", null))
        }
    }

    @Test
    fun `An adjective saves only two written out forms`() {
        // Given
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Adjective,
            wordWithAnnotation = "glad",
            formsMode = FormsMode.WrittenOut,
            writtenForms = listOf("glatt", "glada", "gladare"),
        )

        // When
        val result = save(uiState)

        // Then
        expectThat(result).savedVocabulary().and {
            get { ending }.isEqualTo("glatt glada")
            get { listOf(form1, form2, form3) }.isEqualTo(listOf("glatt", "glada", null))
        }
    }

    @Test
    fun `Phrases under other are saved without endings and forms`() {
        // Given
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Other,
            wordWithAnnotation = "en gång",
            endings = "-er -de -t",
        )

        // When
        val result = save(uiState)

        // Then
        expectThat(result).savedVocabulary().and {
            get { word }.isEqualTo("en gång")
            get { ending }.isEqualTo("")
            get { form1 }.isNull()
            get { formsNeedReview }.isFalse()
        }
    }

    @Test
    fun `Written out forms are not checked when the word group has no forms`() {
        // Given
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Other,
            wordWithAnnotation = "hur mår du",
            formsMode = FormsMode.WrittenOut,
            writtenForms = listOf("", "a b"),
        )

        // When
        val result = save(uiState)

        // Then
        expectThat(result).isA<SaveResult.Valid>()
    }

    @Test
    fun `The preview shows a noun without subgroup as undefined noun`() {
        // Given
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Noun,
            wordWithAnnotation = "penna",
            translation = "pencil",
        )

        // When
        val preview = uiState.toPreviewVocabulary()

        // Then
        expectThat(preview) {
            get { word }.isEqualTo("penna")
            get { translation }.isEqualTo("pencil")
            get { wordGroup }.isEqualTo(WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED))
        }
    }

    @Test
    fun `The preview shows a word without word group as other without forms`() {
        // Given
        val uiState = AddEditUiState(wordWithAnnotation = "penna", endings = "-n -or -orna")

        // When
        val preview = uiState.toPreviewVocabulary()

        // Then
        expectThat(preview) {
            get { wordGroup }.isEqualTo(WordGroup.Other)
            get { ending }.isEqualTo("")
            get { form1 }.isNull()
        }
    }

    private fun save(uiState: AddEditUiState, initialVocabulary: Vocabulary? = null): SaveResult =
        mapUiStateToVocabulary(uiState, initialVocabulary, CONTAINER_ID)

    private fun Assertion.Builder<SaveResult>.savedVocabulary(): Assertion.Builder<Vocabulary> =
        isA<SaveResult.Valid>().get { vocabulary }

    companion object {
        private const val CONTAINER_ID = 42

        private fun verb(
            wordWithAnnotation: String,
            subgroup: WordGroup.VerbSubgroup = WordGroup.VerbSubgroup.GROUP_1,
        ) = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Verb,
            selectedSubGroup = ViewWordSubGroup.Verb(subgroup),
            wordWithAnnotation = wordWithAnnotation,
        )

        private fun writtenOutVerb(vararg forms: String) =
            verb(wordWithAnnotation = "ta", subgroup = WordGroup.VerbSubgroup.GROUP_4_SPECIAL).copy(
                formsMode = FormsMode.WrittenOut,
                writtenForms = forms.toList(),
            )

        @JvmStatic
        fun unsavableInputs(): List<Arguments> = listOf(
            argumentSet(
                "no word group selected",
                AddEditUiState(selectedWordGroup = null),
                SaveResult.MissingWordGroup,
            ),
            argumentSet(
                "noun without subgroup",
                AddEditUiState(selectedWordGroup = ViewWordGroup.Noun, selectedSubGroup = ViewWordSubGroup.None),
                SaveResult.MissingWordGroup,
            ),
            argumentSet(
                "highlight outside the changing word",
                verb(wordWithAnnotation = "känna s*i*g", subgroup = WordGroup.VerbSubgroup.GROUP_2A),
                SaveResult.InvalidHighlights,
            ),
            argumentSet(
                "empty written out form before a filled one",
                writtenOutVerb("", "tog", "tagit"),
                SaveResult.InvalidWrittenForms(WrittenFormsError.Gap),
            ),
            argumentSet(
                "blank written out form counts as a gap",
                writtenOutVerb("tar", "  ", "tagit"),
                SaveResult.InvalidWrittenForms(WrittenFormsError.Gap),
            ),
            argumentSet(
                "space inside a written out form",
                writtenOutVerb("tar", "tog", "har tagit"),
                SaveResult.InvalidWrittenForms(WrittenFormsError.Space),
            ),
        )

        @JvmStatic
        fun irregularPronunciations(): List<Arguments> = listOf(
            argumentSet("blank is dropped", " ", null),
            argumentSet("text is kept", "jå", "jå"),
            argumentSet("Initial empty irregularPronunciation stays null", null, null),
        )
    }
}
