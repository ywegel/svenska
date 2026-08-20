@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.db.VocabularyDao
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FavoritesAndPronunciationsRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `All favorite vocabularies are fetched, if no container is specified`() = runTest(testDispatcher) {
        val daoMock = mockk<VocabularyDao>(relaxed = true)

        val ignored = FavoritesAndPronunciationsRepositoryImpl(daoMock, testDispatcher).getFavorites(null)

        verify(exactly = 1) { daoMock.getAllFavorites() }
        verify(exactly = 0) { daoMock.getFavoritesByContainerId(any()) }
    }

    @Test
    fun `Only favorite vocabularies of a container are fetched, if container was specified`() =
        runTest(testDispatcher) {
            val daoMock = mockk<VocabularyDao>(relaxed = true)

            val ignored = FavoritesAndPronunciationsRepositoryImpl(daoMock, testDispatcher).getFavorites(1)

            verify(exactly = 1) { daoMock.getFavoritesByContainerId(1) }
            verify(exactly = 0) { daoMock.getAllFavorites() }
        }

    @Test
    fun `All pronunciations vocabularies are fetched, if no container is specified`() = runTest(testDispatcher) {
        val daoMock = mockk<VocabularyDao>(relaxed = true)

        val ignored = FavoritesAndPronunciationsRepositoryImpl(daoMock, testDispatcher).getPronunciations(null)

        verify(exactly = 1) { daoMock.getAllPronunciations() }
        verify(exactly = 0) { daoMock.getPronunciationsByContainerId(any()) }
    }

    @Test
    fun `Only pronunciations vocabularies of a container are fetched, if container was specified`() =
        runTest(testDispatcher) {
            val daoMock = mockk<VocabularyDao>(relaxed = true)

            val ignored = FavoritesAndPronunciationsRepositoryImpl(daoMock, testDispatcher).getPronunciations(1)

            verify(exactly = 1) { daoMock.getPronunciationsByContainerId(1) }
            verify(exactly = 0) { daoMock.getAllPronunciations() }
        }
}
