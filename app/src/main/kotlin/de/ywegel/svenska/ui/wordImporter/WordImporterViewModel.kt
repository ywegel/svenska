package de.ywegel.svenska.ui.wordImporter

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.FileParseException
import de.ywegel.svenska.data.FileRepository
import de.ywegel.svenska.data.model.ImporterChapter
import de.ywegel.svenska.domain.wordImporter.ImportChaptersUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WordImporterViewModel"

sealed class ImporterState {
    data object Idle : ImporterState()
    data class Parsed(val words: Int, val chapters: Int) : ImporterState()
    data class Importing(val progress: Float) : ImporterState()
    data class Finished(val wordCount: Int, val success: Boolean, val error: ImporterError? = null) : ImporterState()
}

/**
 * UI-facing import failure.  [WordImporterScreen] maps each case to a user facing error string.
 */
sealed class ImporterError {
    data object FileNotFound : ImporterError()
    data object InvalidFileFormat : ImporterError()
    data object NoWordsLoaded : ImporterError()
    data class SaveFailed(val cause: Throwable) : ImporterError()
    data class Unknown(val cause: Throwable) : ImporterError()
}

private fun FileParseException.toImporterError(): ImporterError = when (this) {
    is FileParseException.FileNotFound -> ImporterError.FileNotFound
    is FileParseException.InvalidFormat -> ImporterError.InvalidFileFormat
    is FileParseException.Unexpected -> ImporterError.Unknown(originalCause)
}

@HiltViewModel
class WordImporterViewModel @Inject constructor(
    private val importChapters: ImportChaptersUseCase,
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _importerState = MutableStateFlow<ImporterState>(ImporterState.Idle)
    val importerState = _importerState.asStateFlow()

    private var loadedChapters: List<ImporterChapter>? = null
    private var loadedChaptersWordCount: Int? = null

    init {
        // Reset "loadedWords" to avoid an invalid state
        viewModelScope.launch {
            importerState.collect { state ->
                if (state is ImporterState.Finished || state is ImporterState.Idle) {
                    loadedChapters = null
                }
            }
        }
    }

    fun onFilePicked(pickedFile: Uri) = viewModelScope.launch {
        _isLoading.value = true

        fileRepository.parseFile(pickedFile)
            .onSuccess { entries ->
                val sumOfWords = entries.sumOf { c -> c.words.size }

                loadedChapters = entries
                loadedChaptersWordCount = sumOfWords

                _importerState.value = ImporterState.Parsed(
                    words = sumOfWords,
                    chapters = entries.size,
                )
            }.onFailure { error ->
                Log.e(TAG, "onFilePicked: failed to parse picked file", error)
                _importerState.value = ImporterState.Finished(
                    wordCount = 0,
                    success = false,
                    error = (error as? FileParseException)?.toImporterError() ?: ImporterError.Unknown(error),
                )
            }

        _isLoading.value = false
    }

    @Suppress("detekt:TooGenericExceptionCaught")
    fun saveWords() = viewModelScope.launch {
        val loadedChapters = loadedChapters
        val loadedChaptersWordCount = loadedChaptersWordCount

        val noWordsLoaded =
            loadedChapters.isNullOrEmpty() || loadedChaptersWordCount == null || loadedChaptersWordCount == 0

        if (noWordsLoaded) {
            _importerState.value = ImporterState.Finished(
                wordCount = 0,
                success = false,
                error = ImporterError.NoWordsLoaded,
            )
            return@launch
        }

        try {
            importChapters(loadedChapters)
                .onStart { _importerState.value = ImporterState.Importing(0f) }
                .map { processed -> processed.toFloat() / loadedChaptersWordCount }
                .distinctUntilChanged()
                .collect { percentage ->
                    _importerState.update { ImporterState.Importing(percentage) }
                }

            _importerState.value = ImporterState.Finished(wordCount = loadedChaptersWordCount, success = true)
        } catch (e: CancellationException) {
            // The ViewModel being cleared mid-import is not an import failure.
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "saveWords: failed to import loaded chapters", e)
            _importerState.value = ImporterState.Finished(
                wordCount = 0,
                success = false,
                error = ImporterError.SaveFailed(e),
            )
        }
    }

    fun onRestartClicked() {
        _importerState.value = ImporterState.Idle
        loadedChapters = null
    }
}
