package de.ywegel.svenska.domain.addEdit

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.vocabulary
import de.ywegel.svenska.ui.addEdit.AddEditUiState
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotNull
import strikt.assertions.isNull
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
    fun `Uses default gender if null and word group is noun`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Noun,
            selectedSubGroup = ViewWordSubGroup.Noun(WordGroup.NounSubgroup.OR),
            gender = null,
            wordWithAnnotation = "penna",
            translation = "pencil",
        )

        val result = useCase(
            snapshot = uiState,
            initialVocabulary = null,
            containerId = containerId,
        )

        expectThat(result).isNotNull()
        expectThat(result!!.gender).isEqualTo(Gender.defaultIfEmpty)
    }

    @Test
    fun `IrregularPronunciation is null when isIrregularPronunciation is false`() {
        val uiState = AddEditUiState(
            selectedWordGroup = ViewWordGroup.Verb,
            selectedSubGroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_1),
            isIrregularPronunciation = false,
            irregularPronunciation = "ignored",
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
            isIrregularPronunciation = true,
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
        expectThat(result.wordHighlights).isEqualTo(listOf(0, 1))
    }
}
