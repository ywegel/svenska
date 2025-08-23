@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.data

import android.content.ContentResolver
import android.net.Uri
import assertk.assertThat
import assertk.assertions.isEqualTo
import de.ywegel.svenska.assertVocabularyListEqualsIgnoringTimestamps
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.wordImporter.WordParserImpl
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FileRepositoryWordParserRoundTripTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `parser roundTrip test`() = runTest(testDispatcher) {
        val expected = listOf(
            Vocabulary(
                word = "nounGroupOr Ultra",
                translation = "translation1",
                gender = Gender.Ultra,
                wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.OR),
                ending = "-n -or -orna",
                containerId = 1,
            ),
            Vocabulary(
                word = "verbGroup1 a",
                translation = "translation2",
                gender = null,
                wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_1),
                ending = "-r -de -t",
                containerId = 1,
            ),
            Vocabulary(
                word = "nounGroup5 Neutra",
                translation = "translation3",
                gender = Gender.Neutra,
                wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_ETT),
                ending = "-et - -en",
                containerId = 1,
            ),
            Vocabulary(
                word = "adjective",
                translation = "translation4",
                gender = null,
                wordGroup = WordGroup.Adjective,
                ending = "-t -a",
                containerId = 1,
            ),
            Vocabulary(
                word = "verb a",
                translation = "translation5",
                gender = null,
                wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2B),
                ending = "-er -te -t",
                containerId = 1,
            ),
        )

        // Given
        val vocRepository: VocabularyRepositoryFake = VocabularyRepositoryFake()
        val contentResolver: ContentResolver = mockk()
        val inputStream = javaClass.classLoader?.getResourceAsStream("sample_words.json")
        val uri = mockk<Uri>()

        every { contentResolver.openInputStream(uri) } returns inputStream

        val repository = FileRepositoryImpl(
            contentResolver = contentResolver,
            vocabularyRepository = vocRepository,
            containerRepository = vocRepository,
            wordParser = WordParserImpl(),
        )

        // When
        val (containerId, entries) = repository.parseFile(uri, testDispatcher).getOrNull()
            ?: error("parser failed to decode json")

        val insertedFilesResult = repository.parseAndSaveEntriesToDbWithProgress(entries, containerId)

        // Then
        assertThat(entries.size).isEqualTo(1)
        assertThat(entries[0].words.size).isEqualTo(5)
        assertThat(insertedFilesResult.last()).isEqualTo(entries[0].words.size)
        assertVocabularyListEqualsIgnoringTimestamps(
            expectedList = expected,
            actualList = vocRepository.getAllVocabulariesSnapshot(null),
            // We need to sort, because we don't know if the VocabularyRepositoryFake can maintain the insertion order.
            sort = true,
        )
    }
}
