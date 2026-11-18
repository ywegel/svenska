package de.ywegel.svenska.userflows

import com.ramcosta.composedestinations.generated.destinations.OverviewScreenDestination
import dagger.hilt.android.testing.HiltAndroidTest
import de.ywegel.svenska.R
    import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import org.junit.Test

@HiltAndroidTest
class VocabularyFlowTest : UserFlowTest() {

    @Test
    fun `creating a new vocabulary saves all filled fields and lists it in the container`() {
        // Given
        val container = givenContainer(name = "Nouns")
        launchApp(startRoute = OverviewScreenDestination(containerId = container.id))

        // When
        overview { clickAddVocabulary() }
        addEdit {
            assertIsCreatingNew()
            selectWordGroup(R.string.lang_noun, R.string.lang_noun_abbreviation_er)
            switchGender(from = "en", to = "ett")
            enterWord("stort p*a*rti")
            selectChangingWord("parti")
            enterTranslation("big party")
            enterEndings("-et -er -erna")
            addNotes("Also a political party")
            addIrregularPronunciation("partí")
            toggleFavorite()
            save()
        }

        // Then
        overview {
            assertOverviewScreenIsVisible(containerName = "Nouns")
            assertVocabularyIsListed(
                word = "stort parti",
                translation = "big party",
                wordGroup = R.string.lang_noun,
                subGroup = R.string.lang_noun_abbreviation_er,
            )
        }
        assertSavedVocabularyIs(
            container,
            Vocabulary(
                word = "parti",
                wordPrefix = "stort",
                wordHighlights = listOf(1 to 2),
                translation = "big party",
                gender = Gender.Neutra,
                wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.ER),
                ending = "-et -er -erna",
                form1 = "partiet",
                form2 = "partier",
                form3 = "partierna",
                notes = "Also a political party",
                irregularPronunciation = "partí",
                isFavorite = true,
                containerId = container.id,
            ),
        )
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

    @Test
    fun `a chosen changing word is still chosen when editing the vocabulary again`() {
        // Given
        val container = givenContainer(name = "Nouns")
        launchApp(startRoute = OverviewScreenDestination(containerId = container.id))

        // When
        overview { clickAddVocabulary() }
        addEdit {
            selectWordGroup(R.string.lang_noun, R.string.lang_noun_abbreviation_ar)
            enterWord("kopp kaffe")
            selectChangingWord("kopp")
            enterTranslation("cup of coffee")
            enterEndings("-en -ar -arna")
            save()
        }
        overview { clickVocabulary("kopp kaffe") }
        vocabularyDetail { clickEdit() }

        // Then
        addEdit {
            assertIsEditing(word = "kopp")
            assertChangingWordIs("kopp")
        }
    }
}
