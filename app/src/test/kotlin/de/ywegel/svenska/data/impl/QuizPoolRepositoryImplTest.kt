package de.ywegel.svenska.data.impl

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.ywegel.svenska.RobolectricTest
import de.ywegel.svenska.data.db.QuizPoolDao
import de.ywegel.svenska.data.db.VocabularyDatabase
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.model.quiz.PoolRemoval
import de.ywegel.svenska.data.model.vocabulary
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class QuizPoolRepositoryImplTest : RobolectricTest() {
    private lateinit var db: VocabularyDatabase
    private lateinit var dao: QuizPoolDao
    private lateinit var repository: QuizPoolRepositoryImpl

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), VocabularyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.quizPool()
        repository = QuizPoolRepositoryImpl(dao)
        db.vocabulary().upsertVocabulary(vocabulary(id = VOCABULARY_ID))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `Removing a queued word deletes its entry`() = runTest {
        // Given
        val entry = queued()
        dao.insert(entry)

        // When
        val removal = repository.removeFromPool(VOCABULARY_ID)

        // Then
        expectThat(removal).isEqualTo(PoolRemoval.DELETED)
        expectThat(dao.get(VOCABULARY_ID)).isNull()
    }

    @Test
    fun `Removing an active word pauses it and keeps its progress`() = runTest {
        // Given
        val entry = active()
        dao.insert(entry)

        // When
        val removal = repository.removeFromPool(VOCABULARY_ID)

        // Then
        expectThat(removal).isEqualTo(PoolRemoval.PAUSED)
        expectThat(dao.get(VOCABULARY_ID)).isEqualTo(entry.copy(isPaused = true))
    }

    @Test
    fun `Removing a paused word keeps it paused`() = runTest {
        // Given
        val entry = active().copy(isPaused = true)
        dao.insert(entry)

        // When
        val removal = repository.removeFromPool(VOCABULARY_ID)

        // Then
        expectThat(removal).isEqualTo(PoolRemoval.UNCHANGED)
        expectThat(dao.get(VOCABULARY_ID)).isEqualTo(entry)
    }

    @Test
    fun `Removing a word that is not in the pool changes nothing`() = runTest {
        // When
        val removal = repository.removeFromPool(VOCABULARY_ID)

        // Then
        expectThat(removal).isEqualTo(PoolRemoval.UNCHANGED)
        expectThat(dao.get(VOCABULARY_ID)).isNull()
    }

    @Test
    fun `Removing a word introduced after it was shown as queued pauses it`() = runTest {
        // Given: the list still shows the word as queued, but a session has introduced it since
        val shownAsQueued = queued()
        dao.insert(shownAsQueued)
        dao.update(shownAsQueued.introduce(now = 2_000L))

        // When
        val removal = repository.removeFromPool(VOCABULARY_ID)

        // Then
        expectThat(removal).isEqualTo(PoolRemoval.PAUSED)
        expectThat(dao.get(VOCABULARY_ID)?.isPaused).isEqualTo(true)
    }

    private companion object {
        const val VOCABULARY_ID = 1

        fun queued() = QuizPoolEntry(vocabularyId = VOCABULARY_ID, queuedAt = 1_000L)

        fun active() = queued().copy(introducedAt = 2_000L, svDeStreak = 2)
    }
}
