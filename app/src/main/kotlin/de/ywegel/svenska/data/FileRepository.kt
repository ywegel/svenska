package de.ywegel.svenska.data

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.domain.wordImporter.WordParser
import de.ywegel.svenska.jsonConfig
import de.ywegel.svenska.ui.wordImporter.ImporterChapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import javax.inject.Inject

private const val TAG = "FileRepository"

interface FileRepository {
    suspend fun parseFile(uri: Uri, ioDispatcher: CoroutineDispatcher): Result<Pair<Int, List<ImporterChapter>>>

    fun parseAndSaveEntriesToDbWithProgress(entries: List<ImporterChapter>, containerId: Int?): Flow<Int>
}

class FileRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val vocabularyRepository: VocabularyRepository,
    private val containerRepository: ContainerRepository,
    private val wordParser: WordParser,
) : FileRepository {

    @Suppress("detekt:TooGenericExceptionCaught")
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun parseFile(
        uri: Uri,
        ioDispatcher: CoroutineDispatcher,
    ): Result<Pair<Int, List<ImporterChapter>>> = withContext(ioDispatcher) {
        return@withContext try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val entries: List<ImporterChapter> = jsonConfig.decodeFromStream(inputStream)
                val entryCount = entries.fold(0) { count, chapter ->
                    count + chapter.words.size
                }

                Result.success(Pair(entryCount, entries))
            } ?: throw IllegalArgumentException("Unable to open InputStream for URI: $uri")
        } catch (e: Exception) {
            Log.e(TAG, "parseFile: failed to parse file", e)
            Result.failure(e)
        }
    }

    @OptIn(FlowPreview::class)
    override fun parseAndSaveEntriesToDbWithProgress(entries: List<ImporterChapter>, containerId: Int?): Flow<Int> =
        flow {
            // TODO: containerId is ignored rn, because the importer only supports importing chapters at the moment
            // When no longer ignoring containerId, we need to adjust the test as well
            var lastChapterWordSize = 0

            entries.forEach { chapter ->
                val newContainerId = containerRepository.upsertContainer(VocabularyContainer(name = chapter.chapter))

                chapter.words.asFlow()
                    .onEach { wordPair ->
                        val newVocabulary = wordParser.parseWord(
                            word = wordPair.getOrNull(0),
                            translation = wordPair.getOrNull(1),
                            containerId = newContainerId.toInt(),
                        )

                        vocabularyRepository.upsertVocabulary(newVocabulary)
                        lastChapterWordSize++
                    }
                    .sample(100)
                    .collect { _ ->
                        emit(lastChapterWordSize)
                    }

                emit(lastChapterWordSize)
            }
        }
}
