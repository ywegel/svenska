package de.ywegel.svenska.data

import android.net.Uri
import de.ywegel.svenska.ui.wordImporter.ImporterChapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    suspend fun parseFile(uri: Uri, ioDispatcher: CoroutineDispatcher): Result<Pair<Int, List<ImporterChapter>>>

    fun parseAndSaveEntriesToDbWithProgress(entries: List<ImporterChapter>, containerId: Int?): Flow<Int>
}
