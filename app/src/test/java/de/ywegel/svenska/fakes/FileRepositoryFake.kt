package de.ywegel.svenska.fakes

import android.net.Uri
import de.ywegel.svenska.data.FileRepository
import de.ywegel.svenska.ui.wordImporter.ImporterChapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FileRepositoryFake : FileRepository {
    private var parseResult: Result<Pair<Int, List<ImporterChapter>>> = Result.success(Pair(0, emptyList()))

    fun setParseResult(result: Result<Pair<Int, List<ImporterChapter>>>) {
        this.parseResult = result
    }

    override suspend fun parseFile(
        uri: Uri,
        ioDispatcher: CoroutineDispatcher,
    ): Result<Pair<Int, List<ImporterChapter>>> {
        return parseResult
    }

    override fun parseAndSaveEntriesToDbWithProgress(entries: List<ImporterChapter>, _containerId: Int?): Flow<Int> {
        return flow {
            entries.forEachIndexed { index, _ ->
                emit(index + 1)
            }
        }
    }
}
