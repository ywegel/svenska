package de.ywegel.svenska.fakes

import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.FavoritesAndPronunciationsRepository
import de.ywegel.svenska.data.SearchRepository
import de.ywegel.svenska.data.SortOrder
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class VocabularyRepositoryFake(
    initialVocabulary: List<Vocabulary> = emptyList(),
    initialContainers: List<VocabularyContainer> = emptyList(),
) : VocabularyRepository, ContainerRepository, FavoritesAndPronunciationsRepository, SearchRepository {

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

    override fun getVocabularies(containerId: Int, sortOrder: SortOrder, reverse: Boolean): Flow<List<Vocabulary>> {
        return vocabularyFlow.map { list ->
            list.filter {
                it.containerId == containerId
            }.let { filtered ->
                when (sortOrder) {
                    SortOrder.Word -> filtered.sortedBy { it.word }
                    SortOrder.Translation -> filtered.sortedBy { it.translation }
                    SortOrder.Created -> filtered.sortedBy { it.created }
                    SortOrder.LastEdited -> filtered.sortedBy { it.lastEdited }
                }
            }.let { sorted ->
                if (reverse) sorted.reversed() else sorted
            }
        }
    }

    override fun searchVocabularies(query: String, containerId: Int?): Flow<List<Vocabulary>> {
        return if (query.isNotBlank()) {
            vocabularyFlow.map { item ->
                item.filter {
                    it.word.lowercase().contains(query.lowercase()) &&
                        (containerId == null || it.containerId == containerId)
                }
            }
        } else {
            vocabularyFlow.map { item -> item.filter { containerId == null || it.containerId == containerId } }
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

    override fun getAllContainers(): Flow<List<VocabularyContainer>> = containersFlow

    override suspend fun getContainerById(id: Int): VocabularyContainer? {
        return containers.find { it.id == id }
    }

    override suspend fun upsertContainer(container: VocabularyContainer): Long {
        return if (container.id == 0) {
            val newId = (containers.maxOfOrNull { it.id } ?: 0) + 1
            containers.add(container.copy(id = newId))
            containersFlow.emit(containers)
            newId.toLong()
        } else {
            containers.removeIf { it.id == container.id }
            containers.add(container)
            containersFlow.emit(containers)
            container.id.toLong()
        }
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
        return vocabulary.filter { it.irregularPronunciation != null }
    }

    override suspend fun getAllVocabulariesSnapshot(containerId: Int?): List<Vocabulary> {
        return containerId?.let {
            vocabulary.filter { it.containerId == containerId }
        } ?: vocabulary.toList()
    }

    override suspend fun toggleVocabularyFavorite(vocabularyId: Int, isFavorite: Boolean) {
        vocabulary.find { it.id == vocabularyId }?.let { old ->
            vocabulary.remove(old)
            val new = old.copy(isFavorite = isFavorite)
            vocabulary.add(new)
        }
    }

    override suspend fun getAllVocabulariesWithEndings(containerId: Int?): List<Vocabulary> {
        return containerId?.let {
            vocabulary.filter { it.ending.isNotBlank() && it.containerId == containerId }
        } ?: vocabulary.filter { it.ending.isNotBlank() }
    }
}
