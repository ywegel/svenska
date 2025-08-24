package de.ywegel.svenska.domain

import de.ywegel.svenska.data.model.container
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.containsExactly

@OptIn(ExperimentalCoroutinesApi::class)
class ToggleVocabularyFavoriteUseCaseTest {
    private val vocabularyId = 1
    private val containerId = 1

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Toggling value results in the correct updated value in the repository`(newIsFavoriteValue: Boolean) {
        val testDispatcher = StandardTestDispatcher()

        val initialVocabulary = vocabulary(
            isFavorite = !newIsFavoriteValue,
            containerId = containerId,
            id = vocabularyId,
        )

        val repository = spyk(
            VocabularyRepositoryFake(
                initialVocabulary = listOf(initialVocabulary),
                initialContainers = listOf(container(id = containerId)),
            ),
        )

        runTest(testDispatcher) {
            ToggleVocabularyFavoriteUseCase(repository, testDispatcher)
                .invoke(vocabularyId, newIsFavoriteValue)
            advanceUntilIdle()

            coVerify { repository.toggleVocabularyFavorite(containerId, newIsFavoriteValue) }

            val vocabularies = repository.getAllVocabulariesSnapshot(containerId)
            expectThat(vocabularies).containsExactly(
                initialVocabulary.copy(isFavorite = newIsFavoriteValue),
            )
        }
    }
}
