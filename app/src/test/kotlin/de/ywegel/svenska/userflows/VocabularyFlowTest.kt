package de.ywegel.svenska.userflows

import com.ramcosta.composedestinations.generated.destinations.OverviewScreenDestination
import dagger.hilt.android.testing.HiltAndroidTest
import de.ywegel.svenska.R
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
}
