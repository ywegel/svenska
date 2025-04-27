package de.ywegel.svenska.ui.wordImporter

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.FileRepository
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
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

sealed class ImporterError(val message: String) {
    // TODO: move strings to ressources and map in view
    object FileNotFound : ImporterError("File not found or cannot be opened.")
    object InvalidJsonFormat : ImporterError("Invalid file format. Please upload a valid file.")
    object EmptyOrCorruptFileError : ImporterError("The file contains no words or is corrupted.")
    object DatabaseError : ImporterError("Database operation failed.")
    data class UnknownError(val throwable: Throwable) :
        ImporterError("An unknown error occurred: ${throwable.localizedMessage}")
}

@HiltViewModel
class WordImporterViewModel @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _importerState = MutableStateFlow<ImporterState>(ImporterState.Idle)
    val importerState = _importerState.asStateFlow()

    private var loadedWords: Pair<Int, List<ImporterChapter>>? = null

    init {
        // Reset "loadedWords" to avoid an invalid state
        viewModelScope.launch {
            importerState.collect { state ->
                if (state is ImporterState.Finished || state is ImporterState.Idle) {
                    loadedWords = null
                }
            }
        }
    }

    fun onFilePicked(pickedFile: Uri) {
        _isLoading.value = true

        viewModelScope.launch(ioDispatcher) {
            val result = fileRepository.parseFile(pickedFile, ioDispatcher)
            result.onSuccess { entries ->
                loadedWords = entries
                _importerState.value = ImporterState.Parsed(
                    words = entries.first,
                    chapters = entries.second.size,
                )
            }.onFailure { error ->
                val importerError = error as? ImporterError ?: ImporterError.UnknownError(error)
                _importerState.value = ImporterState.Finished(
                    wordCount = 0,
                    success = false,
                    error = importerError,
                )
            }
        }

        _isLoading.value = false
    }

    fun saveWords() = viewModelScope.launch(ioDispatcher) {
        loadedWords.let { loadedWords ->
            if (loadedWords == null || loadedWords.second.isEmpty() || loadedWords.first == 0) {
                _importerState.value = ImporterState.Finished(
                    wordCount = 0,
                    success = false,
                    // TODO: Move to string resource and return an error object instead, that gets mapped in the view
                    error = ImporterError.EmptyOrCorruptFileError,
                )
                return@launch
            }
            val (wordCount, importerChapters) = loadedWords

            fileRepository
                .parseAndSaveEntriesToDbWithProgress(importerChapters, null)
                .onStart { _importerState.value = ImporterState.Importing(0f) }
                .map { processed -> processed.toFloat() / wordCount }
                .distinctUntilChanged()
                .onCompletion { cause ->
                    cause?.let {
                        Log.e(TAG, "saveWords: Error in parseAndSaveEntriesToDbWithProgress flow", cause)
                        _importerState.value = ImporterState.Finished(
                            wordCount = 0,
                            success = false,
                            error = ImporterError.UnknownError(cause),
                        )
                    } ?: run {
                        _importerState.value = ImporterState.Finished(
                            wordCount = wordCount,
                            success = true,
                        )
                    }
                }
                .collect { percentage ->
                    _importerState.update { ImporterState.Importing(percentage) }
                }
        }
    }

    fun onRestartClicked() {
        _importerState.value = ImporterState.Idle
        loadedWords = null
    }
}
