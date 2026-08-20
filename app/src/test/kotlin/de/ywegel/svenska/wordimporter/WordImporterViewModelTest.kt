package de.ywegel.svenska.wordimporter

import android.net.Uri
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import de.ywegel.svenska.data.FileParseException
import de.ywegel.svenska.data.model.ImporterChapter
import de.ywegel.svenska.domain.wordImporter.ImportChaptersUseCase
import de.ywegel.svenska.fakes.FileRepositoryFake
import de.ywegel.svenska.ui.wordImporter.ImporterError
import de.ywegel.svenska.ui.wordImporter.ImporterState
import de.ywegel.svenska.ui.wordImporter.WordImporterViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class WordImporterViewModelTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var fileRepositoryFake: FileRepositoryFake
    private lateinit var importChapters: ImportChaptersUseCase
    private lateinit var viewModel: WordImporterViewModel

    @BeforeEach
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        fileRepositoryFake = FileRepositoryFake()
        importChapters = mockk()
        viewModel = WordImporterViewModel(importChapters, fileRepositoryFake)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onFilePicked should update state to Parsed on success`() = runTest(testDispatcher) {
        // Given
        val testUri = mockk<Uri>()
        fileRepositoryFake.setParseResult(
            Result.success(listOf(ImporterChapter("Test", listOf(listOf("word1", "word2"))))),
        )

        // When
        viewModel.onFilePicked(testUri)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.importerState.value).isInstanceOf(ImporterState.Parsed::class)
        val parsedState = viewModel.importerState.value as ImporterState.Parsed
        assertThat(parsedState.words).isEqualTo(1)
        assertThat(parsedState.chapters).isEqualTo(1)
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @Test
    fun `onFilePicked should map FileNotFound to ImporterError FileNotFound`() = runTest(testDispatcher) {
        // Given
        fileRepositoryFake.setParseResult(Result.failure(FileParseException.FileNotFound()))

        // When
        viewModel.onFilePicked(mockk())
        advanceUntilIdle()

        // Then
        val finishedState = viewModel.importerState.value as ImporterState.Finished
        assertThat(finishedState.success).isFalse()
        assertThat(finishedState.error).isNotNull().isInstanceOf(ImporterError.FileNotFound::class)
    }

    @Test
    fun `onFilePicked should map InvalidFormat to ImporterError InvalidFileFormat`() = runTest(testDispatcher) {
        // Given
        fileRepositoryFake.setParseResult(
            Result.failure(FileParseException.InvalidFormat(IllegalStateException("bad json"))),
        )

        // When
        viewModel.onFilePicked(mockk())
        advanceUntilIdle()

        // Then
        val finishedState = viewModel.importerState.value as ImporterState.Finished
        assertThat(finishedState.error).isNotNull().isInstanceOf(ImporterError.InvalidFileFormat::class)
    }

    @Test
    fun `onFilePicked should map an unexpected throwable to ImporterError Unknown`() = runTest(testDispatcher) {
        // Given
        fileRepositoryFake.setParseResult(Result.failure(IllegalStateException("boom")))

        // When
        viewModel.onFilePicked(mockk())
        advanceUntilIdle()

        // Then
        val finishedState = viewModel.importerState.value as ImporterState.Finished
        assertThat(finishedState.error).isNotNull().isInstanceOf(ImporterError.Unknown::class)
    }

    @Test
    fun `saveWords should update state to Importing and then Finished on success`() = runTest(testDispatcher) {
        // Given
        val twoWords = listOf(listOf("word1", "translation1"), listOf("word2", "translation2"))
        fileRepositoryFake.setParseResult(Result.success(listOf(ImporterChapter("Test", twoWords))))
        every { importChapters(any(), any()) } returns flow {
            emit(1)
            emit(2)
        }
        viewModel.onFilePicked(mockk())
        advanceUntilIdle()

        // When
        viewModel.saveWords()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.importerState.value).isInstanceOf(ImporterState.Finished::class)
        val finishedState = viewModel.importerState.value as ImporterState.Finished
        assertThat(finishedState.success).isTrue()
        assertThat(finishedState.wordCount).isEqualTo(2)
    }

    @Test
    fun `saveWords should update state to Finished with SaveFailed when import flow throws`() =
        runTest(testDispatcher) {
            // Given
            fileRepositoryFake.setParseResult(
                Result.success(listOf(ImporterChapter("Test", listOf(listOf("word1", "word2"))))),
            )
            val dbException = IllegalStateException("db write failed")
            every { importChapters(any(), any()) } returns flow { throw dbException }
            viewModel.onFilePicked(mockk())
            advanceUntilIdle()

            // When
            viewModel.saveWords()
            advanceUntilIdle()

            // Then
            val finishedState = viewModel.importerState.value as ImporterState.Finished
            assertThat(finishedState.success).isFalse()
            assertThat(finishedState.error).isNotNull().isInstanceOf(ImporterError.SaveFailed::class)
            assertThat((finishedState.error as ImporterError.SaveFailed).cause).isEqualTo(dbException)
        }

    @Test
    fun `saveWords should update state to Finished with error when no words loaded`() = runTest(testDispatcher) {
        // When
        viewModel.saveWords()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.importerState.value).isInstanceOf(ImporterState.Finished::class)
        val finishedState = viewModel.importerState.value as ImporterState.Finished
        assertThat(finishedState.success).isFalse()
        assertThat(finishedState.error)
            .isNotNull()
            .isInstanceOf(ImporterError.NoWordsLoaded::class)
    }

    @Test
    fun `onRestartClicked should reset state to Idle`() = runTest(testDispatcher) {
        // Given
        viewModel.onRestartClicked()

        // Then
        assertThat(viewModel.importerState.value).isInstanceOf(ImporterState.Idle::class)
    }
}
