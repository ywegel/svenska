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

    private data class LoadedImport(val chapters: List<ImporterChapter>) {
        val wordCount: Int = chapters.sumOf { it.words.size }
    }

    private var loadedImport: LoadedImport? = null

    private fun failedState(error: ImporterError) =
        ImporterState.Finished(wordCount = 0, success = false, error = error)

    init {
        // Reset "loadedImport" to avoid an invalid state
        viewModelScope.launch {
            importerState.collect { state ->
                if (state is ImporterState.Finished || state is ImporterState.Idle) {
                    loadedImport = null
                }
            }
        }
    }

    fun onFilePicked(pickedFile: Uri) = viewModelScope.launch {
        _isLoading.value = true

        fileRepository.parseFile(pickedFile)
            .onSuccess { entries ->
                val newLoadedImport = LoadedImport(chapters = entries)
                loadedImport = newLoadedImport

                _importerState.value = ImporterState.Parsed(
                    words = newLoadedImport.wordCount,
                    chapters = entries.size,
                )
            }.onFailure { error ->
                Log.e(TAG, "onFilePicked: failed to parse picked file", error)
                _importerState.value = failedState(
                    (error as? FileParseException)?.toImporterError() ?: ImporterError.Unknown(error),
                )
            }

        _isLoading.value = false
    }

    @Suppress("detekt:TooGenericExceptionCaught")
    fun saveWords() = viewModelScope.launch {
        val loadedImport = loadedImport

        if (loadedImport == null || loadedImport.wordCount == 0) {
            _importerState.value = failedState(ImporterError.NoWordsLoaded)
            return@launch
        }

        try {
            importChapters(loadedImport.chapters)
                .onStart { _importerState.value = ImporterState.Importing(0f) }
                .map { processed -> processed.toFloat() / loadedImport.wordCount }
                .distinctUntilChanged()
                .collect { percentage ->
                    _importerState.update { ImporterState.Importing(percentage) }
                }

            _importerState.value = ImporterState.Finished(wordCount = loadedImport.wordCount, success = true)
        } catch (e: CancellationException) {
            // The ViewModel being cleared mid-import is not an import failure.
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "saveWords: failed to import loaded chapters", e)
            _importerState.value = failedState(ImporterError.SaveFailed(e))
        }
    }

    fun onRestartClicked() {
        _importerState.value = ImporterState.Idle
        loadedImport = null
    }
}
