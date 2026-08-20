package de.ywegel.svenska.data.impl

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import de.ywegel.svenska.data.FileParseException
import de.ywegel.svenska.data.FileRepository
import de.ywegel.svenska.data.model.ImporterChapter
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.jsonConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.decodeFromStream
import java.io.IOException
import javax.inject.Inject

private const val TAG = "FileRepository"

class FileRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FileRepository {

    @Suppress("detekt:TooGenericExceptionCaught")
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun parseFile(uri: Uri): Result<List<ImporterChapter>> = withContext(ioDispatcher) {
        try {
            val entries = contentResolver.openInputStream(uri)?.use { inputStream ->
                jsonConfig.decodeFromStream<List<ImporterChapter>>(inputStream)
            } ?: return@withContext Result.failure(FileParseException.FileNotFound())

            Result.success(entries)
        } catch (e: IOException) {
            Log.e(TAG, "parseFile: failed to open picked file", e)
            Result.failure(FileParseException.FileNotFound())
        } catch (e: SerializationException) {
            Log.e(TAG, "parseFile: picked file is not valid json for the expected format", e)
            Result.failure(FileParseException.InvalidFormat(e))
        } catch (e: Exception) {
            Log.e(TAG, "parseFile: unexpected failure while parsing file", e)
            Result.failure(FileParseException.Unexpected(e))
        }
    }
}
