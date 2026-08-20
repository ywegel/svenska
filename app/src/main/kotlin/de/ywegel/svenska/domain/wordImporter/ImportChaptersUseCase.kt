package de.ywegel.svenska.domain.wordImporter

import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.ImporterChapter
import de.ywegel.svenska.data.model.VocabularyContainer
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class ImportChaptersUseCase @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val containerRepository: ContainerRepository,
    private val wordParser: WordParser,
) {
    // TODO: containerId is ignored rn, because the importer only supports importing chapters
    @OptIn(FlowPreview::class)
    operator fun invoke(entries: List<ImporterChapter>, containerId: Int? = null): Flow<Int> = flow {
        var processed = 0

        entries.forEach { chapter ->
            val newContainerId = containerRepository
                .upsertContainer(VocabularyContainer(name = chapter.chapter))

            chapter.words.asFlow()
                .onEach { wordPair ->
                    val vocabulary = wordParser.parseWord(
                        word = wordPair.getOrNull(0),
                        translation = wordPair.getOrNull(1),
                        containerId = newContainerId.toInt(),
                    )
                    vocabularyRepository.upsertVocabulary(vocabulary)
                    processed++
                }
                .sample(100.milliseconds)
                .collect { emit(processed) }

            emit(processed)
        }
    }
}
