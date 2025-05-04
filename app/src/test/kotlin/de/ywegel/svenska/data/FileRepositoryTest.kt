@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.data

import android.content.ContentResolver
import android.net.Uri
import assertk.assertThat
import assertk.assertions.isEqualTo
import de.ywegel.svenska.fakes.WordParserFake
import de.ywegel.svenska.ui.wordImporter.ImporterChapter
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class FileRepositoryTest {

    private lateinit var repository: FileRepository
    private lateinit var contentResolver: ContentResolver
    private lateinit var vocRepository: VocabularyRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        contentResolver = mockk()
        vocRepository = mockk(relaxUnitFun = true, relaxed = true)
        repository = FileRepositoryImpl(contentResolver, vocRepository, WordParserFake())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `parseFile should return list of entries when stream is valid`() = runTest {
        val expected = listOf(
            ImporterChapter(
                "Kapitel 1",
                listOf(
                    listOf(
                        "nounGroupOr Ultra (-n, -or, -orna)",
                        "translation1",
                    ),
                    listOf(
                        "verbGroup1 a (-r, -de, -t)",
                        "translation2",
                    ),
                    listOf(
                        "nounGroup5 Neutra (-et, -, -en)",
                        "translation3",
                    ),
                    listOf(
                        "adjective (-t, -a)",
                        "translation4",
                    ),
                    listOf(
                        "verb a (-er, -te, -t)",
                        "translation5",
                    ),
                ),
            ),
        )

        // Given
        val inputStream = javaClass.classLoader?.getResourceAsStream("sample_words.json")
        val uri = mockk<Uri>()

        every { contentResolver.openInputStream(uri) } returns inputStream

        // When
        val result = repository.parseFile(uri, testDispatcher)

        // Then
        assertTrue(result.isSuccess)
        val entries = result.getOrNull()
        assertNotNull(entries)
        assertThat(expected).isEqualTo(entries?.second)
    }

    @Test
    fun `parseFile should return failure when stream cannot be opened`() = runTest {
        // Given
        val uri = mockk<Uri>()
        every { contentResolver.openInputStream(uri) } returns null

        // When
        val result = repository.parseFile(uri, testDispatcher)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `parseFile should return failure when JSON is invalid`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val invalidJson = """{invalid json]"""
        val inputStream = ByteArrayInputStream(invalidJson.toByteArray())

        every { contentResolver.openInputStream(uri) } returns inputStream

        // When
        val result = repository.parseFile(uri, testDispatcher)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is kotlinx.serialization.SerializationException)
    }

    @Test
    fun `parseAndSaveEntriesToDbWithProgress should emit progress correctly`() = runTest {
        // Given
        val entries = listOf(
            importerChapter(1),
        )

        coEvery { vocRepository.upsertVocabulary(any()) } returns 1L

        // When
        val progressFlow = repository.parseAndSaveEntriesToDbWithProgress(entries, 0)
        val progressValues = progressFlow.toList()

        // Then
        assertThat(progressValues.last()).isEqualTo(2)
        coVerify(exactly = 2) { vocRepository.upsertVocabulary(any()) }
    }

    @Test
    fun `parseAndSaveEntriesToDbWithProgress should throw exception on save error`() = runTest {
        // Given
        val entries = listOf(
            importerChapter(1),
        )

        coEvery { vocRepository.upsertVocabulary(any()) } throws RuntimeException("Save error")

        // When & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runBlocking {
                repository.parseAndSaveEntriesToDbWithProgress(entries, 0).collect()
            }
        }

        assertEquals("Save error", exception.message)
    }
}
