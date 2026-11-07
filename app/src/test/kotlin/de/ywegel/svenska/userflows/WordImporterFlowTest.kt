package de.ywegel.svenska.userflows

import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class WordImporterFlowTest : UserFlowTest() {

    @Test
    fun `importing a json file creates a container with its words`() {
        // Given
        launchApp()
        container { openSettings() }

        // When
        settings { openWordImporter() }
        wordImporter {
            assertWordImporterScreenIsVisible()
            selectFile("sample_words.json")
            assertFoundWords(words = 7, chapters = 1)
            proceed()
            assertWordsImported(words = 7)
            exit()
        }
        settings { navigateUp() }
        container {
            assertContainerIsListed("Kapitel 1")
            openContainer("Kapitel 1")
        }

        // Then
        overview {
            assertOverviewScreenIsVisible(containerName = "Kapitel 1")
            assertWordIsListed(word = "klass", translation = "class")
            assertWordIsListed(word = "ord", translation = "word")
            assertWordIsListed(word = "dricka", translation = "to drink")
        }
    }
}
