@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.containers

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.fakes.VocabularyRepositoryFake
import de.ywegel.svenska.ui.container.ContainerViewModel
import io.mockk.clearAllMocks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContainerViewModelTest {
    private lateinit var testDispatcher: TestDispatcher

    @BeforeEach
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `A newly created container shows up in the uiState`() = runTest(testDispatcher) {
        // given
        val expected = VocabularyContainer(name = "ContainerTestName")
        val repository = VocabularyRepositoryFake()

        val viewModel = ContainerViewModel(
            repository = repository,
            ioDispatcher = testDispatcher,
        )

        advanceUntilIdle()

        viewModel.uiState.test {
            // given
            assertThat(awaitItem().containers).isEmpty()

            // when
            viewModel.addContainer(expected.name)

            // then
            assertThat(awaitItem().containers).contains(expected)
        }
    }
}
