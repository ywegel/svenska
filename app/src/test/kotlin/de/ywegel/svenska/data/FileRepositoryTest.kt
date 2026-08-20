@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.data

import android.content.ContentResolver
import android.net.Uri
import assertk.assertThat
import assertk.assertions.isEqualTo
import de.ywegel.svenska.data.impl.FileRepositoryImpl
import de.ywegel.svenska.data.model.ImporterChapter
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class FileRepositoryTest {

    private lateinit var repository: FileRepository
    private lateinit var contentResolver: ContentResolver
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        contentResolver = mockk()
        repository = FileRepositoryImpl(contentResolver, testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `parseFile should return list of entries when stream is valid`() = runTest(testDispatcher) {
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
        val result = repository.parseFile(uri)

        // Then
        assertTrue(result.isSuccess)
        val entries = result.getOrNull()
        assertNotNull(entries)
        assertThat(expected).isEqualTo(entries)
    }

    @Test
    fun `parseFile should fail with FileNotFound when stream cannot be opened`() = runTest(testDispatcher) {
        // Given
        val uri = mockk<Uri>()
        every { contentResolver.openInputStream(uri) } returns null

        // When
        val result = repository.parseFile(uri)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileParseException.FileNotFound)
    }

    @Test
    fun `parseFile should fail with InvalidFormat when JSON is invalid`() = runTest(testDispatcher) {
        // Given
        val uri = mockk<Uri>()
        val invalidJson = """{invalid json]"""
        val inputStream = ByteArrayInputStream(invalidJson.toByteArray())

        every { contentResolver.openInputStream(uri) } returns inputStream

        // When
        val result = repository.parseFile(uri)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileParseException.InvalidFormat)
    }
}
