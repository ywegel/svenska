package de.ywegel.svenska.domain.wordImporter

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.ImporterChapter
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import de.ywegel.svenska.fakes.WordParserFake
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportChaptersUseCaseTest {

    @Test
    fun `invoke should emit progress and persist each word under the created container`() = runTest {
        // Given
        val vocRepository = VocabularyRepositoryFake()
        val useCase = ImportChaptersUseCase(vocRepository, vocRepository, WordParserFake())
        val entries = listOf(
            ImporterChapter(
                "Kapitel 1",
                listOf(listOf("word1", "translation1"), listOf("word2", "translation2")),
            ),
        )

        // When
        val progressValues = useCase(entries).toList()

        // Then
        assertThat(progressValues.last()).isEqualTo(2)

        val persisted = vocRepository.getAllVocabulariesSnapshot(null)
        assertThat(persisted).hasSize(2)
        assertThat(persisted.map { it.word to it.translation }).isEqualTo(
            listOf("word1" to "translation1", "word2" to "translation2"),
        )
        // Both words came from the same chapter, so they must share the one container created for it.
        assertThat(persisted.map { it.containerId }.distinct()).hasSize(1)
    }

    @Test
    fun `invoke should propagate the underlying exception when persisting fails`() = runTest {
        // Given
        val vocabularyRepository: VocabularyRepository = mockk()
        val containerRepository: ContainerRepository = mockk()
        val dbException = RuntimeException("Save error")

        coEvery { containerRepository.upsertContainer(any()) } returns 1L
        coEvery { vocabularyRepository.upsertVocabulary(any()) } throws dbException

        val useCase = ImportChaptersUseCase(vocabularyRepository, containerRepository, WordParserFake())
        val entries = listOf(ImporterChapter("Kapitel 1", listOf(listOf("word1", "translation1"))))

        // When
        val exception = assertThrows(RuntimeException::class.java) {
            runBlocking { useCase(entries).collect() }
        }

        // Then
        assertThat(exception.message).isEqualTo(dbException.message)
    }
}
