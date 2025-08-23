package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.db.SearchDao
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class SearchRepositoryImplTest {
    @Test
    fun `Search searches all vocabularies if no container is specified`() {
        val daoMock = mockk<SearchDao>(relaxed = true)

        val ignored = SearchRepositoryImpl(daoMock).searchVocabularies("abc", null)

        verify(exactly = 1) { daoMock.searchVocabularies("abc") }
        verify(exactly = 0) { daoMock.searchVocabulariesById(any(), any()) }
    }

    @Test
    fun `Search searches in the specific container if container was specified`() {
        val daoMock = mockk<SearchDao>(relaxed = true)

        val ignored = SearchRepositoryImpl(daoMock).searchVocabularies("abc", 1)

        verify(exactly = 1) { daoMock.searchVocabulariesById(1, "abc") }
        verify(exactly = 0) { daoMock.searchVocabularies(any()) }
    }
}
