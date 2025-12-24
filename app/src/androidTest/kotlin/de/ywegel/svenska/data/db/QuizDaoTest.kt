@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.vocabulary
import io.mockk.clearAllMocks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isEmpty

@RunWith(AndroidJUnit4::class)
class QuizDaoTest {
    private lateinit var db: VocabularyDatabase
    private lateinit var quizDao: QuizDao
    private lateinit var vocabularyDao: VocabularyDao

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            VocabularyDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        quizDao = db.quiz()
        vocabularyDao = db.vocabulary()
    }

    @After
    fun tearDown() {
        db.close()
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun getAllNouns_with_containerID_returns_only_nouns_in_that_container() = runTest(testDispatcher) {
        // Given
        val noun1 = vocabulary(
            id = 1,
            word = "hus",
            containerId = 1,
            wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.R),
        )
        val noun2 = vocabulary(
            id = 2,
            word = "bok",
            containerId = 1,
            wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.OR),
        )
        val verb = vocabulary(
            id = 3,
            word = "hoppa",
            containerId = 1,
            wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_1),
        )

        vocabularyDao.upsertVocabulary(noun1)
        vocabularyDao.upsertVocabulary(noun2)
        vocabularyDao.upsertVocabulary(verb)
        advanceUntilIdle()

        // When
        val result = quizDao.getAllNouns(containerId = 1)

        // Then
        expectThat(result)
            .hasSize(2)
            .containsExactlyInAnyOrder(noun1, noun2)
    }

    @Test
    fun getAllNouns_with_containerId_returns_empty_list_when_no_nouns_in_container() = runTest {
        // Given
        val containerId = 42
        val verb = vocabulary(
            word = "äta",
            id = 1,
            containerId = containerId,
            wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
        )

        vocabularyDao.upsertVocabulary(verb)
        advanceUntilIdle()

        // When
        val result = quizDao.getAllNouns(containerId = containerId)

        // Then
        expectThat(result).isEmpty()
    }

    @Test
    fun getAllNouns_without_containerId_returns_all_nouns_across_all_containers() = runTest {
        // Given
        val noun1 = vocabulary(
            id = 1,
            word = "hus",
            containerId = 1,
            wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.R),
        )
        val noun2 = vocabulary(
            id = 2,
            word = "bok",
            containerId = 2,
            wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.OR),
        )
        val verb = vocabulary(
            id = 3,
            word = "hoppa",
            containerId = 5,
            wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_1),
        )

        vocabularyDao.upsertVocabulary(noun1)
        vocabularyDao.upsertVocabulary(noun2)
        vocabularyDao.upsertVocabulary(verb)
        advanceUntilIdle()

        // When
        val result = quizDao.getAllNouns()

        // Then
        expectThat(result)
            .hasSize(2)
            .containsExactlyInAnyOrder(noun1, noun2)
    }

    @Test
    fun getAllNouns_queries_ignore_Other_and_Adjective_word_groups() = runTest {
        // Given
        val other = vocabulary(word = "idag", id = 1, containerId = 1, wordGroup = WordGroup.Other)
        val adjective = vocabulary(word = "stor", id = 2, containerId = 1, wordGroup = WordGroup.Adjective)

        vocabularyDao.upsertVocabulary(other)
        vocabularyDao.upsertVocabulary(adjective)

        // When
        val resultWithContainer = quizDao.getAllNouns(containerId = 1)
        val resultGlobal = quizDao.getAllNouns()

        // Then
        expectThat(resultWithContainer).isEmpty()
        expectThat(resultGlobal).isEmpty()
    }
}
