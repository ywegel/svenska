package de.ywegel.svenska.userflows

import com.ramcosta.composedestinations.generated.destinations.OverviewScreenDestination
import dagger.hilt.android.testing.HiltAndroidTest
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.WordGroup
import org.junit.Test

@HiltAndroidTest
class VocabularyFlowTest : UserFlowTest() {

    @Test
    fun `creating a new vocabulary adds it to the container`() {
        // Given
        val container = givenContainer(name = "Verbs")
        launchApp(startRoute = OverviewScreenDestination(containerId = container.id))

        // When
        overview { clickAddVocabulary() }
        addEdit {
            assertIsCreatingNew()
            selectWordGroup(R.string.lang_verb, R.string.lang_verb_abbreviation_2a)
            enterWord("ringa")
            enterTranslation("to call")
            enterEndings("-er -de -t")
            save()
        }

        // Then
        overview {
            assertOverviewScreenIsVisible(containerName = "Verbs")
            assertVocabularyIsListed(
                word = "ringa",
                translation = "to call",
                wordGroup = R.string.lang_verb,
                subGroup = R.string.lang_verb_abbreviation_2a,
            )
        }
    }

    @Test
    fun `editing a vocabulary updates translation and word group`() {
        // Given
        val container = givenContainer(name = "Nouns")
        givenVocabulary(
            container = container,
            word = "lampa",
            translation = "lamp",
            wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED),
        )
        launchApp(startRoute = OverviewScreenDestination(containerId = container.id))

        // When
        overview { clickVocabulary("lampa") }
        vocabularyDetail { clickEdit() }
        addEdit {
            assertIsEditing(word = "lampa")
            enterTranslation("light")
            selectWordGroup(R.string.lang_noun, R.string.lang_noun_abbreviation_or)
            save()
        }

        // Then
        overview {
            assertOverviewScreenIsVisible(containerName = "Nouns")
            assertVocabularyIsListed(
                word = "lampa",
                translation = "light",
                wordGroup = R.string.lang_noun,
                subGroup = R.string.lang_noun_abbreviation_or,
            )
            assertTranslationIsNotListed("lamp")
        }
    }
}
