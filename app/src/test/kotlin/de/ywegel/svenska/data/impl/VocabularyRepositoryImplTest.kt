package de.ywegel.svenska.data.impl

import app.cash.turbine.test
import de.ywegel.svenska.common.streamOf
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.db.VocabularyDao
import de.ywegel.svenska.data.model.SortOrder
import de.ywegel.svenska.data.model.Vocabulary
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.stream.Stream

class VocabularyRepositoryImplTest {
    private lateinit var dao: VocabularyDao
    private lateinit var repository: VocabularyRepository

    private val containerId = 1
    private val vocabularies = listOf(
        Vocabulary(word = "test", translation = "test", containerId = containerId),
    )

    @BeforeEach
    fun setUp() {
        dao = mockk()
        repository = VocabularyRepositoryImpl(dao)
    }

    data class SortTestCase(
        val sortOrder: SortOrder,
        val reverse: Boolean,
        val expectedDaoMethod: VocabularyDao.(Int) -> Flow<List<Vocabulary>>,
        val description: String,
    )

    companion object {
        @JvmStatic
        private fun sortTestCases(): Stream<SortTestCase> = streamOf(
            SortTestCase(
                sortOrder = SortOrder.Word,
                reverse = false,
                expectedDaoMethod = VocabularyDao::getVocabulariesByWordASC,
                description = "SortOrder.Word, reverse false calls getVocabulariesByWordASC",
            ),
            SortTestCase(
                sortOrder = SortOrder.Word,
                reverse = true,
                expectedDaoMethod = VocabularyDao::getVocabulariesByWordDESC,
                description = "SortOrder.Word, reverse true calls getVocabulariesByWordDESC",
            ),
            SortTestCase(
                sortOrder = SortOrder.Translation,
                reverse = false,
                expectedDaoMethod = VocabularyDao::getVocabulariesByTranslationASC,
                description = "SortOrder.Translation, reverse false calls getVocabulariesByTranslationASC",
            ),
            SortTestCase(
                sortOrder = SortOrder.Translation,
                reverse = true,
                expectedDaoMethod = VocabularyDao::getVocabulariesByTranslationDESC,
                description = "SortOrder.Translation, reverse true calls getVocabulariesByTranslationDESC",
            ),
            SortTestCase(
                sortOrder = SortOrder.Created,
                reverse = false,
                expectedDaoMethod = VocabularyDao::getVocabulariesByCreatedASC,
                description = "SortOrder.Created, reverse false calls getVocabulariesByCreatedASC",
            ),
            SortTestCase(
                sortOrder = SortOrder.Created,
                reverse = true,
                expectedDaoMethod = VocabularyDao::getVocabulariesByCreatedDESC,
                description = "SortOrder.Created, reverse true calls getVocabulariesByCreatedDESC",
            ),
            SortTestCase(
                sortOrder = SortOrder.LastEdited,
                reverse = false,
                expectedDaoMethod = VocabularyDao::getVocabulariesByEditedASC,
                description = "SortOrder.LastEdited, reverse false calls getVocabulariesByEditedASC",
            ),
            SortTestCase(
                sortOrder = SortOrder.LastEdited,
                reverse = true,
                expectedDaoMethod = VocabularyDao::getVocabulariesByEditedDESC,
                description = "SortOrder.LastEdited, reverse true calls getVocabulariesByEditedDESC",
            ),
        )

        private val allDaoMethods: List<VocabularyDao.(Int) -> Flow<List<Vocabulary>>> = listOf(
            VocabularyDao::getVocabulariesByWordASC,
            VocabularyDao::getVocabulariesByWordDESC,
            VocabularyDao::getVocabulariesByTranslationASC,
            VocabularyDao::getVocabulariesByTranslationDESC,
            VocabularyDao::getVocabulariesByCreatedASC,
            VocabularyDao::getVocabulariesByCreatedDESC,
            VocabularyDao::getVocabulariesByEditedASC,
            VocabularyDao::getVocabulariesByEditedDESC,
        )
    }

    private fun stubDaoMethod(daoMethod: VocabularyDao.(Int) -> Flow<List<Vocabulary>>) {
        every { daoMethod.invoke(dao, containerId) } returns flowOf(vocabularies)
    }

    private fun verifyDaoCalls(expectedMethod: VocabularyDao.(Int) -> Flow<List<Vocabulary>>) {
        verify(exactly = 1) { expectedMethod.invoke(dao, containerId) }
        allDaoMethods.filter { it != expectedMethod }.forEach { method ->
            verify(exactly = 0) { method.invoke(dao, any()) }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sortTestCases")
    fun `Verify that the correct dao function is called for all parameters of getVocabularies`(testCase: SortTestCase) =
        runTest {
            // Given
            stubDaoMethod(testCase.expectedDaoMethod)

            // When
            repository.getVocabularies(containerId, testCase.sortOrder, testCase.reverse).test {
                // Then
                expectThat(awaitItem()).isEqualTo(vocabularies)
                verifyDaoCalls(testCase.expectedDaoMethod)
                awaitComplete()
            }
        }

    @Test
    fun `Snapshot all vocabularies if no container is specified`() = runTest {
        val daoMock = mockk<VocabularyDao>(relaxed = true)

        VocabularyRepositoryImpl(daoMock).getAllVocabulariesSnapshot(null)

        coVerify(exactly = 1) { daoMock.getAllVocabulariesSnapshot() }
        coVerify(exactly = 0) { daoMock.getAllVocabulariesSnapshot(any()) }
    }

    @Test
    fun `Snapshot vocabularies in the specific container if container was specified`() = runTest {
        val daoMock = mockk<VocabularyDao>(relaxed = true)

        VocabularyRepositoryImpl(daoMock).getAllVocabulariesSnapshot(1)

        coVerify(exactly = 1) { daoMock.getAllVocabulariesSnapshot(1) }
        coVerify(exactly = 0) { daoMock.getAllVocabulariesSnapshot() }
    }

    @Test
    fun `Get all vocabularies with endings if no container is specified`() = runTest {
        val daoMock = mockk<VocabularyDao>(relaxed = true)

        VocabularyRepositoryImpl(daoMock).getAllVocabulariesWithEndings(null)

        coVerify(exactly = 1) { daoMock.getAllVocabulariesWithEndings() }
        coVerify(exactly = 0) { daoMock.getAllVocabulariesWithEndings(any()) }
    }

    @Test
    fun `Get vocabularies with endings in the specific container if container was specified`() = runTest {
        val daoMock = mockk<VocabularyDao>(relaxed = true)

        VocabularyRepositoryImpl(daoMock).getAllVocabulariesWithEndings(1)

        coVerify(exactly = 1) { daoMock.getAllVocabulariesWithEndings(1) }
        coVerify(exactly = 0) { daoMock.getAllVocabulariesWithEndings() }
    }
}
