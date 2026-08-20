package de.ywegel.svenska.fakes

import android.net.Uri
import de.ywegel.svenska.data.FileRepository
import de.ywegel.svenska.data.model.ImporterChapter

class FileRepositoryFake : FileRepository {
    private var parseResult: Result<List<ImporterChapter>> = Result.success(emptyList())

    fun setParseResult(result: Result<List<ImporterChapter>>) {
        this.parseResult = result
    }

    override suspend fun parseFile(uri: Uri): Result<List<ImporterChapter>> {
        return parseResult
    }
}
