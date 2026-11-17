package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class MapUiStateToVocabularyUseCaseTest {

    private val useCase = MapUiStateToVocabularyUseCase()
    private val containerId = 42

    @Test
    fun `Returns null when selectedWordGroup is null`() {
        val uiState = AddEditUiState(selectedWordGroup = null)
        val result = useCase(
            snapshot = uiState,
            initialVocabulary = null,
            containerId = containerId,
        )
        expectThat(result).isNull()
    }

    @Test
    fun `Returns null when toWordGroup mapping fails`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Noun,
            selectedSubGroup = ViewWordSubGroup.None,
            wordWithAnnotation = "penna",
            translation = "pencil",
        )

        val result = useCase(uiState, null, containerId)

        expectThat(result).isNull()
    }

    @Test
    fun `Creates new vocabulary when initialVocabulary is null`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Noun,
            selectedSubGroup = ViewWordSubGroup.Noun(WordGroup.NounSubgroup.OR),
            wordWithAnnotation = "penna",
            translation = "pencil",
            gender = Gender.Ultra,
        )

        val result = useCase(
            snapshot = uiState,
            initialVocabulary = null,
            containerId = containerId,
        )

        expectThat(result).isNotNull()
        expectThat(result!!.word).isEqualTo("penna")
        expectThat(result.translation).isEqualTo("pencil")
        expectThat(result.wordGroup).isEqualTo(WordGroup.Noun(WordGroup.NounSubgroup.OR))
        expectThat(result.gender).isEqualTo(Gender.Ultra)
        expectThat(result.containerId).isEqualTo(containerId)
        expectThat(result.created).isGreaterThan(0)
        expectThat(result.lastEdited).isGreaterThan(0)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `Preserves created value and updates everything else when editing existing vocabulary`() {
        val initialCreationTime = Clock.System.now().minus(30.days)
        val initial = vocabulary().copy(
            created = initialCreationTime.toEpochMilliseconds(),
            lastEdited = initialCreationTime.toEpochMilliseconds(),
        )
        val uiState = AddEditUiState.fromExistingVocabulary(initial)

        val result = useCase(
            snapshot = uiState,
            initialVocabulary = initial,
            containerId = containerId,
        )

        expectThat(result).isNotNull()
        expectThat(result!!.created).isEqualTo(initial.created)
        expectThat(result.created).isEqualTo(initialCreationTime.toEpochMilliseconds())
        expectThat(result.lastEdited).isGreaterThan(initial.lastEdited)
        expectThat(result.lastEdited).isGreaterThan(initialCreationTime.toEpochMilliseconds())
    }

    @Test
    fun `Gender is null if word group is not noun`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Verb,
            selectedSubGroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_1),
            gender = Gender.Ultra,
            wordWithAnnotation = "gå",
            translation = "walk",
        )

        val result = useCase(
            snapshot = uiState,
            initialVocabulary = null,
            containerId = containerId,
        )

        expectThat(result).isNotNull()
        expectThat(result!!.gender).isNull()
    }

    @Test
    fun `IrregularPronunciation is null when it is blank`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Verb,
            selectedSubGroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_1),
            irregularPronunciation = " ",
            wordWithAnnotation = "gå",
            translation = "walk",
        )

        val result = useCase(
            snapshot = uiState,
            initialVocabulary = null,
            containerId = containerId,
        )

        expectThat(result).isNotNull()
        expectThat(result!!.irregularPronunciation).isNull()
    }

    @Test
    fun `IrregularPronunciation is kept when isIrregularPronunciation is true`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Verb,
            selectedSubGroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_1),
            irregularPronunciation = "testPronunciation",
            wordWithAnnotation = "gå",
            translation = "walk",
        )

        val result = useCase(
            snapshot = uiState,
            initialVocabulary = null,
            containerId = containerId,
        )

        expectThat(result).isNotNull()
        expectThat(result!!.irregularPronunciation).isEqualTo("testPronunciation")
    }

    @Test
    fun `Word highlights and cleaned string are extracted from annotated input`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Noun,
            selectedSubGroup = ViewWordSubGroup.Noun(WordGroup.NounSubgroup.OR),
            wordWithAnnotation = "*p*enna",
            translation = "pencil",
        )

        val result = useCase(
            snapshot = uiState,
            initialVocabulary = null,
            containerId = containerId,
        )

        expectThat(result).isNotNull()
        expectThat(result!!.word).isEqualTo("penna")
        expectThat(result.wordHighlights).isEqualTo(listOf(0 to 1))
    }

    @Test
    fun `Saves prefix, suffix and the forms of the changing word`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Verb,
            selectedSubGroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
            wordWithAnnotation = "att känna sig",
            translation = "sich fühlen",
            endings = "-er, -de, -t",
        )

        val result = useCase(uiState, initialVocabulary = null, containerId = containerId)

        expectThat(result).isNotNull().and {
            get { wordPrefix }.isEqualTo("")
            get { word }.isEqualTo("känna")
            get { wordSuffix }.isEqualTo("sig")
            get { ending }.isEqualTo("-er -de -t")
            get { listOf(form1, form2, form3) }.isEqualTo(listOf("känner", "kände", "känt"))
            get { gender }.isNull()
        }
    }

    @Test
    fun `Written out forms are saved as forms and as full word endings`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Verb,
            selectedSubGroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL),
            wordWithAnnotation = "ta en dusch",
            translation = "duschen",
            endings = "-r",
            formsMode = FormsMode.WrittenOut,
            writtenForms = listOf("tar", " tog ", "tagit"),
        )

        val result = useCase(uiState, initialVocabulary = null, containerId = containerId)

        expectThat(result).isNotNull().and {
            get { ending }.isEqualTo("tar tog tagit")
            get { listOf(form1, form2, form3) }.isEqualTo(listOf("tar", "tog", "tagit"))
            get { formsNeedReview }.isFalse()
        }
    }

    @Test
    fun `Forms with a pending review are saved with the review flag`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Noun,
            selectedSubGroup = ViewWordSubGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_ETT),
            wordWithAnnotation = "exempel",
            translation = "Beispiel",
            endings = "-et - -en",
        )

        val result = useCase(uiState, initialVocabulary = null, containerId = containerId)

        expectThat(result).isNotNull().get { formsNeedReview }.isTrue()
    }

    @Test
    fun `Phrases under other are saved without endings and forms`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Other,
            wordWithAnnotation = "en gång",
            translation = "einmal",
            endings = "-er -de -t",
        )

        val result = useCase(uiState, initialVocabulary = null, containerId = containerId)

        expectThat(result).isNotNull().and {
            get { word }.isEqualTo("en gång")
            get { ending }.isEqualTo("")
            get { form1 }.isNull()
            get { formsNeedReview }.isFalse()
        }
    }

    @Test
    fun `Returns null when highlights are outside the changing word`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Verb,
            selectedSubGroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
            wordWithAnnotation = "känna s*i*g",
        )

        val result = useCase(uiState, initialVocabulary = null, containerId = containerId)

        expectThat(result).isNull()
    }
}
