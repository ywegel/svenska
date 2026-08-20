package de.ywegel.svenska.data

import android.net.Uri
import de.ywegel.svenska.data.model.ImporterChapter

interface FileRepository {
    suspend fun parseFile(uri: Uri): Result<List<ImporterChapter>>
}
