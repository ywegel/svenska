package de.ywegel.svenska.fakes

import de.ywegel.svenska.data.SortOrder
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class VocabularyRepositoryFake(
    initialVocabulary: List<Vocabulary> = emptyList(),
    initialContainers: List<VocabularyContainer> = emptyList(),
) : VocabularyRepository {

    constructor(
        initialContainers: List<VocabularyContainer> = emptyList(),
        vararg entry: Vocabulary,
    ) : this(
        initialVocabulary = entry.asList(),
        initialContainers = initialContainers,
    )

    private var vocabulary = initialVocabulary.toMutableList()
    private val vocabularyFlow = MutableStateFlow(vocabulary.toList())

    private var containers = initialContainers.toMutableList()
    private val containersFlow = MutableStateFlow(containers.toList())

    public fun bulkInsertVocabularies(list: List<Vocabulary>) = runBlocking {
        bulkInsertVocabularies(*list.toTypedArray())
    }

    public fun bulkInsertVocabularies(vararg vocabularyList: Vocabulary) = runBlocking {
        vocabulary.addAll(vocabularyList)
        vocabularyFlow.emit(vocabulary.toList())
    }

    override fun getVocabularies(
        query: String,
        containerId: Int?,
        sortOrder: SortOrder,
        reverse: Boolean,
    ): Flow<List<Vocabulary>> {
        // TODO: handle null container scenario
        val flow = vocabularyFlow.map { list ->
            when (sortOrder) {
                SortOrder.Word -> list.sortedBy { it.word }
                SortOrder.Translation -> list.sortedBy { it.translation }
                SortOrder.Created -> list.sortedBy { it.created }
                SortOrder.LastEdited -> list.sortedBy { it.lastEdited }
            }.let { sorted ->
                if (reverse) sorted.reversed() else sorted
            }
        }

        return if (query.isNotBlank()) {
            flow.map { item -> item.filter { it.word.toLowerCase().contains(query.toLowerCase()) } }
        } else {
            flow
        }
    }

    override suspend fun deleteVocabulary(vocabulary: Vocabulary) {
        this.vocabulary.remove(vocabulary)
        vocabularyFlow.emit(this.vocabulary.toList())
    }

    override suspend fun upsertVocabulary(vocabulary: Vocabulary): Long {
        this.vocabulary.remove(vocabulary)
        this.vocabulary.add(vocabulary)
        vocabularyFlow.emit(this.vocabulary)
        return vocabulary.id.toLong()
    }

    override suspend fun getVocabularyById(id: Int): Vocabulary? {
        return vocabulary.firstOrNull { it.id == id }
    }

    override fun getAllContainers(): Flow<List<VocabularyContainer>> = containersFlow

    override suspend fun getContainerById(id: Int): VocabularyContainer? {
        return containers.find { it.id == id }
    }

    override suspend fun upsertContainer(container: VocabularyContainer): Long {
        containers.remove(container)
        containers.add(container)
        containersFlow.emit(containers)
        return container.id.toLong()
    }

    override suspend fun deleteContainerWithAllVocabulary(container: VocabularyContainer) {
        vocabulary.removeAll { it.containerId == container.id }
        containers.remove(container)
        vocabularyFlow.emit(vocabulary)
        containersFlow.emit(containers)
    }

    override fun getFavorites(containerId: Int?): List<Vocabulary> {
        return vocabulary.filter { it.isFavorite }
    }

    override fun getPronunciations(containerId: Int?): List<Vocabulary> {
        return vocabulary.filter { it.irregularPronunciation != null && it.irregularPronunciation.isNullOrBlank() }
    }

    override fun getAllContainerNamesWithIds(): List<VocabularyContainer> {
        return containers.toList()
    }

    override suspend fun getAllVocabulariesSnapshot(containerId: Int?): List<Vocabulary> {
        return vocabulary.toList()
    }

    override suspend fun toggleVocabularyFavorite(vocabularyId: Int, isFavorite: Boolean) {
        vocabulary.find { it.id == vocabularyId }?.let { old ->
            vocabulary.remove(old)
            val new = old.copy(isFavorite = isFavorite)
            vocabulary.add(new)
        }
    }
}
