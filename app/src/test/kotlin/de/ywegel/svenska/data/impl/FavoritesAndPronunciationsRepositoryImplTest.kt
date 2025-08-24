package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.db.VocabularyDao
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class FavoritesAndPronunciationsRepositoryImplTest {
    @Test
    fun `All favorite vocabularies are fetched, if no container is specified`() {
        val daoMock = mockk<VocabularyDao>(relaxed = true)

        val ignored = FavoritesAndPronunciationsRepositoryImpl(daoMock).getFavorites(null)

        verify(exactly = 1) { daoMock.getAllFavorites() }
        verify(exactly = 0) { daoMock.getFavoritesByContainerId(any()) }
    }

    @Test
    fun `Only favorite vocabularies of a container are fetched, if container was specified`() {
        val daoMock = mockk<VocabularyDao>(relaxed = true)

        val ignored = FavoritesAndPronunciationsRepositoryImpl(daoMock).getFavorites(1)

        verify(exactly = 1) { daoMock.getFavoritesByContainerId(1) }
        verify(exactly = 0) { daoMock.getAllFavorites() }
    }

    @Test
    fun `All pronunciations vocabularies are fetched, if no container is specified`() {
        val daoMock = mockk<VocabularyDao>(relaxed = true)

        val ignored = FavoritesAndPronunciationsRepositoryImpl(daoMock).getPronunciations(null)

        verify(exactly = 1) { daoMock.getAllPronunciations() }
        verify(exactly = 0) { daoMock.getPronunciationsByContainerId(any()) }
    }

    @Test
    fun `Only pronunciations vocabularies of a container are fetched, if container was specified`() {
        val daoMock = mockk<VocabularyDao>(relaxed = true)

        val ignored = FavoritesAndPronunciationsRepositoryImpl(daoMock).getPronunciations(1)

        verify(exactly = 1) { daoMock.getPronunciationsByContainerId(1) }
        verify(exactly = 0) { daoMock.getAllPronunciations() }
    }
}
