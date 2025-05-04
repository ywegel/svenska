package de.ywegel.svenska.wordimporter

import android.net.Uri
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import de.ywegel.svenska.fakes.FileRepositoryFake
import de.ywegel.svenska.ui.wordImporter.ImporterChapter
import de.ywegel.svenska.ui.wordImporter.ImporterError
import de.ywegel.svenska.ui.wordImporter.ImporterState
import de.ywegel.svenska.ui.wordImporter.WordImporterViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class WordImporterViewModelTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var fileRepositoryFake: FileRepositoryFake
    private lateinit var viewModel: WordImporterViewModel

    @BeforeEach
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        fileRepositoryFake = FileRepositoryFake()
        viewModel = WordImporterViewModel(testDispatcher, fileRepositoryFake)
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
            Result.success(
                Pair(
                    10,
                    listOf(ImporterChapter("Test", listOf(listOf("word1", "word2")))),
                ),
            ),
        )

        // When
        viewModel.onFilePicked(testUri)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.importerState.value).isInstanceOf(ImporterState.Parsed::class)
        val parsedState = viewModel.importerState.value as ImporterState.Parsed
        assertThat(parsedState.words).isEqualTo(10)
        assertThat(parsedState.chapters).isEqualTo(1)
    }

    @Disabled // TODO: Will be fixed with ne error handling for importer
    @Test
    fun `onFilePicked should update state to Finished with error on failure`() = runTest(testDispatcher) {
        // Given
        val testUri = mockk<Uri>()
        fileRepositoryFake.setParseResult(Result.failure(IllegalStateException()))

        // When
        viewModel.onFilePicked(testUri)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.importerState.value).isInstanceOf(ImporterState.Finished::class)
        val finishedState = viewModel.importerState.value as ImporterState.Finished
        assertThat(finishedState.success).isFalse()
        assertThat(finishedState.error).isNotNull()
            .isInstanceOf(ImporterError.InvalidJsonFormat::class)
    }

    @Test
    fun `saveWords should update state to Importing and then Finished on success`() = runTest(testDispatcher) {
        // Given
        fileRepositoryFake.setParseResult(
            Result.success(
                Pair(
                    10,
                    listOf(ImporterChapter("Test", listOf(listOf("word1", "word2")))),
                ),
            ),
        )
        viewModel.onFilePicked(mockk())
        advanceUntilIdle()

        // When
        viewModel.saveWords()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.importerState.value).isInstanceOf(ImporterState.Finished::class)
        val finishedState = viewModel.importerState.value as ImporterState.Finished
        assertThat(finishedState.success).isTrue()
        assertThat(finishedState.wordCount).isEqualTo(10)
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
            .isInstanceOf(ImporterError.EmptyOrCorruptFileError::class)
    }

    @Test
    fun `onRestartClicked should reset state to Idle`() = runTest(testDispatcher) {
        // Given
        viewModel.onRestartClicked()

        // Then
        assertThat(viewModel.importerState.value).isInstanceOf(ImporterState.Idle::class)
    }
}
