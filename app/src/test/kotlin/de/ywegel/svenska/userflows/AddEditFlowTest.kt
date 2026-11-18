package de.ywegel.svenska.userflows

import com.ramcosta.composedestinations.generated.destinations.OverviewScreenDestination
import dagger.hilt.android.testing.HiltAndroidTest
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.model.WordGroup
import org.junit.Test

@HiltAndroidTest
class AddEditFlowTest : UserFlowTest() {

    @Test
    fun `an existing vocabulary shows all its fields`() {
        // Given
        val container = givenContainer(name = "Nouns")
        givenVocabulary(redPen(container))

        // When
        openEditScreen(container, word = "röd penna")

        // Then
        addEdit {
            assertIsEditing(word = "penna")
            assertWordGroupIsSelected(R.string.lang_noun, R.string.lang_noun_abbreviation_or)
            assertGenderIs(article = "en", other = "ett")
            assertFieldShows(R.string.addEdit_label_word, "röd penn*a*")
            assertChangingWordIs("penna")
            assertFieldShows(R.string.addEdit_label_translation, "red pen")
            assertFieldShows(R.string.addEdit_label_endings, "-n -or -orna")
            assertFormIsShown("en röd penna")
            assertFormIsShown("röd pennan")
            assertFormIsShown("röd pennor")
            assertFormIsShown("röd pennorna")
            assertFieldShows(R.string.addEdit_label_notes, "Plural with -or")
            assertFieldShows(R.string.addEdit_label_irregular_pronunciation, "pena")
            assertIsFavorite()
        }
    }

    @Test
    fun `an unchanged vocabulary is saved as it was`() {
        // Given
        val container = givenContainer(name = "Nouns")
        val pen = givenVocabulary(redPen(container))

        // When
        openEditScreen(container, word = "röd penna")
        addEdit { save() }

        // Then
        assertSavedVocabularyIs(container, pen)
    }

    @Test
    fun `an unchanged vocabulary with written out forms keeps its forms and saves them as endings`() {
        // Given
        val container = givenContainer(name = "Nouns")
        val pen = givenVocabulary(redPen(container).copy(form2 = "pennar"))

        // When
        openEditScreen(container, word = "röd penna")
        addEdit { save() }

        // Then
        assertSavedVocabularyIs(container, pen.copy(ending = "pennan pennar pennorna"))
    }

    @Test
    fun `writing out the forms and using the endings again saves the vocabulary as it was`() {
        // Given
        val container = givenContainer(name = "Nouns")
        val pen = givenVocabulary(redPen(container))

        // When
        openEditScreen(container, word = "röd penna")
        addEdit {
            writeOutForms()
            useEndings()
            save()
        }

        // Then
        assertSavedVocabularyIs(container, pen)
    }

    @Test
    fun `using the endings again after changing a written out form asks before replacing it`() {
        // Given
        val container = givenContainer(name = "Nouns")
        givenVocabulary(redPen(container))
        openEditScreen(container, word = "röd penna")

        // When
        addEdit {
            writeOutForms()
            changeWrittenForm(from = "pennor", to = "pennar")
            useEndings()
        }

        // Then
        addEdit { assertSwitchToEndingsIsAsked() }
    }

    @Test
    fun `cancelling the switch to the endings keeps the written out forms`() {
        // Given
        val container = givenContainer(name = "Nouns")
        givenVocabulary(redPen(container))
        openEditScreen(container, word = "röd penna")
        addEdit {
            writeOutForms()
            changeWrittenForm(from = "pennor", to = "pennar")
            useEndings()
        }

        // When
        addEdit { cancelSwitchToEndings() }

        // Then
        addEdit { assertWrittenFormIsShown("pennar") }
    }

    @Test
    fun `removing the notes clears them`() {
        // Given
        val container = givenContainer(name = "Nouns")
        val pen = givenVocabulary(redPen(container))

        // When
        openEditScreen(container, word = "röd penna")
        addEdit {
            removeNotes()
            save()
        }

        // Then
        assertSavedVocabularyIs(container, pen.copy(notes = ""))
    }

    @Test
    fun `removing the irregular pronunciation clears it`() {
        // Given
        val container = givenContainer(name = "Nouns")
        val pen = givenVocabulary(redPen(container))

        // When
        openEditScreen(container, word = "röd penna")
        addEdit {
            removeIrregularPronunciation()
            save()
        }

        // Then
        assertSavedVocabularyIs(container, pen.copy(irregularPronunciation = null))
    }

    @Test
    fun `deleting an existing vocabulary removes it`() {
        // Given
        val container = givenContainer(name = "Nouns")
        givenVocabulary(redPen(container))

        // When
        openEditScreen(container, word = "röd penna")
        addEdit { delete() }

        // Then
        assertNoVocabularyIsSaved(container)
    }

    @Test
    fun `a new vocabulary can't be deleted`() {
        // Given
        val container = givenContainer(name = "Nouns")
        launchApp(startRoute = OverviewScreenDestination(containerId = container.id))

        // When
        overview { clickAddVocabulary() }

        // Then
        addEdit {
            assertIsCreatingNew()
            assertDeleteIsHidden()
        }
    }

    @Test
    fun `closing the screen discards the changes`() {
        // Given
        val container = givenContainer(name = "Nouns")
        val pen = givenVocabulary(redPen(container))

        // When
        openEditScreen(container, word = "röd penna")
        addEdit {
            enterTranslation("blue pen")
            close()
        }

        // Then
        assertSavedVocabularyIs(container, pen)
    }

    @Test
    fun `switching from noun to another word group hides the gender toggle`() {
        // Given
        val container = givenContainer(name = "Verbs")
        launchApp(startRoute = OverviewScreenDestination(containerId = container.id))
        overview { clickAddVocabulary() }
        addEdit { selectWordGroup(R.string.lang_noun) }

        // When
        addEdit { selectWordGroup(R.string.lang_verb) }

        // Then
        addEdit { assertGenderToggleIsHidden() }
    }

    @Test
    fun `saving without a word group reports it and saves nothing`() {
        // Given
        val container = givenContainer(name = "Nouns")
        launchApp(startRoute = OverviewScreenDestination(containerId = container.id))
        overview { clickAddVocabulary() }

        // When
        addEdit {
            enterWord("penna")
            trySave()
        }

        // Then
        addEdit {
            assertMissingWordGroupIsReported()
            assertIsCreatingNew()
        }
        assertNoVocabularyIsSaved(container)
    }

    @Test
    fun `saving with highlights outside the changing word shows the error and saves nothing`() {
        // Given
        val container = givenContainer(name = "Verbs")
        launchApp(startRoute = OverviewScreenDestination(containerId = container.id))
        overview { clickAddVocabulary() }

        // When
        addEdit {
            selectWordGroup(R.string.lang_verb, R.string.lang_verb_abbreviation_2a)
            enterWord("känna s*i*g")
            trySave()
        }

        // Then
        addEdit {
            assertHighlightErrorIsShown()
            assertIsCreatingNew()
        }
        assertNoVocabularyIsSaved(container)
    }

    @Test
    fun `the info button opens the word groups screen`() {
        // Given
        val container = givenContainer(name = "Nouns")
        launchApp(startRoute = OverviewScreenDestination(containerId = container.id))
        overview { clickAddVocabulary() }

        // When
        addEdit { openWordGroupInfo() }

        // Then
        wordGroups { assertWordGroupsScreenIsVisible() }
    }

    private fun openEditScreen(container: VocabularyContainer, word: String) {
        launchApp(startRoute = OverviewScreenDestination(containerId = container.id))
        overview { clickVocabulary(word) }
        vocabularyDetail { clickEdit() }
    }

    private fun redPen(container: VocabularyContainer) = Vocabulary(
        word = "penna",
        wordPrefix = "röd",
        wordHighlights = listOf(4 to 5),
        translation = "red pen",
        gender = Gender.Ultra,
        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.OR),
        ending = "-n -or -orna",
        form1 = "pennan",
        form2 = "pennor",
        form3 = "pennorna",
        notes = "Plural with -or",
        irregularPronunciation = "pena",
        isFavorite = true,
        containerId = container.id,
    )
}
