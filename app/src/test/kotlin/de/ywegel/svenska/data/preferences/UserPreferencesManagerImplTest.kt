@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import de.ywegel.svenska.data.model.OnlineSearchType
import de.ywegel.svenska.data.model.SortOrder
import de.ywegel.svenska.data.preferences.keys.AddEditPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.AppPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.OnboardingPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.OverviewPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.SearchPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.addLastSearchedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.io.File

class UserPreferencesManagerImplTest {

    @TempDir
    lateinit var tmp: File

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun cleanUp() {
        Dispatchers.resetMain()
    }

    private fun overviewStore(scope: TestScope): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = { tmp.resolve("overview.preferences_pb") },
    )

    private fun addEditStore(scope: TestScope): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = { tmp.resolve("add_edit.preferences_pb") },
    )

    private fun manager(
        scope: TestScope,
        overview: DataStore<Preferences> = overviewStore(scope),
        addEdit: DataStore<Preferences> = addEditStore(scope),
    ) = UserPreferencesManagerImpl(overview = overview, addEdit = addEdit)

    @Test
    fun `every key returns its declared default on an empty store`() = runTest(testDispatcher) {
        // Given
        val subject = manager(this)

        // Then
        expectThat(subject.flow(OverviewPreferenceKeys.SortOrder).first()).isEqualTo(SortOrder.default)
        expectThat(subject.flow(OverviewPreferenceKeys.Revert).first()).isEqualTo(false)
        expectThat(subject.flow(OverviewPreferenceKeys.ShowCompactVocabularyItem).first()).isEqualTo(false)
        expectThat(subject.flow(SearchPreferenceKeys.LastSearchedItems).first()).isEqualTo(ArrayDeque())
        expectThat(subject.flow(SearchPreferenceKeys.OnlineRedirectType).first()).isEqualTo(OnlineSearchType.DictCC)
        expectThat(subject.flow(OnboardingPreferenceKeys.HasCompleted).first()).isEqualTo(false)
        expectThat(subject.flow(AppPreferenceKeys.UseNewQuiz).first()).isEqualTo(false)
        expectThat(subject.flow(AddEditPreferenceKeys.AnnotationInformationHidden).first()).isEqualTo(false)
    }

    @Test
    fun `boolean preference round trips through update`() = runTest(testDispatcher) {
        // Given
        val subject = manager(this)

        // When
        subject.update(OverviewPreferenceKeys.ShowCompactVocabularyItem, true)
        advanceUntilIdle()

        // Then
        expectThat(subject.flow(OverviewPreferenceKeys.ShowCompactVocabularyItem).first()).isEqualTo(true)
    }

    @Test
    fun `enum preference round trips through update`() = runTest(testDispatcher) {
        // Given
        val subject = manager(this)

        // When
        subject.update(OverviewPreferenceKeys.SortOrder, SortOrder.LastEdited)
        advanceUntilIdle()

        // Then
        expectThat(subject.flow(OverviewPreferenceKeys.SortOrder).first()).isEqualTo(SortOrder.LastEdited)
    }

    @Test
    fun `json preference round trips through update - OnlineSearchType`() = runTest(testDispatcher) {
        // Given
        val subject = manager(this)
        val custom = OnlineSearchType.Custom("https://example.com")

        // When
        subject.update(SearchPreferenceKeys.OnlineRedirectType, custom)
        advanceUntilIdle()

        // Then
        expectThat(subject.flow(SearchPreferenceKeys.OnlineRedirectType).first()).isEqualTo(custom)
    }

    @Test
    fun `json preference round trips through update - last searched deque`() = runTest(testDispatcher) {
        // Given
        val subject = manager(this)
        val items = ArrayDeque(listOf("hund", "katt"))

        // When
        subject.update(SearchPreferenceKeys.LastSearchedItems, items)
        advanceUntilIdle()

        // Then
        expectThat(subject.flow(SearchPreferenceKeys.LastSearchedItems).first()).isEqualTo(items)
    }

    @Test
    fun `OnlineSearchType is persisted under its exact JSON format`() = runTest(testDispatcher) {
        // Given
        val overview = overviewStore(this)
        val subject = UserPreferencesManagerImpl(overview = overview, addEdit = addEditStore(this))
        val rawKey = stringPreferencesKey("search_online_redirect_type")

        // When
        subject.update(SearchPreferenceKeys.OnlineRedirectType, OnlineSearchType.DictCC)
        advanceUntilIdle()

        // Then
        expectThat(overview.data.first()[rawKey]).isEqualTo("""{"type":"DictCC"}""")
    }

    @Test
    fun `last searched deque is persisted under its exact JSON format`() = runTest(testDispatcher) {
        // Given
        val overview = overviewStore(this)
        val subject = UserPreferencesManagerImpl(overview = overview, addEdit = addEditStore(this))
        val rawKey = stringPreferencesKey("search_sort_last_searched_items")

        // When
        subject.update(SearchPreferenceKeys.LastSearchedItems, ArrayDeque(listOf("a", "b", "c")))
        advanceUntilIdle()

        // Then
        expectThat(overview.data.first()[rawKey]).isEqualTo("""["a","b","c"]""")
    }

    @Test
    fun `writing an add-edit key does not appear in the overview store`() = runTest(testDispatcher) {
        // Given
        val overview = overviewStore(this)
        val addEdit = addEditStore(this)
        val subject = UserPreferencesManagerImpl(overview = overview, addEdit = addEdit)
        val rawKey = booleanPreferencesKey("add_edit_annotation_information_hidden")

        // When
        subject.update(AddEditPreferenceKeys.AnnotationInformationHidden, true)
        advanceUntilIdle()

        // Then
        expectThat(overview.data.first()[rawKey]).isNull()
        expectThat(addEdit.data.first()[rawKey]).isEqualTo(true)
    }

    @Test
    fun `a garbage persisted enum value falls back to the default instead of throwing`() = runTest(testDispatcher) {
        // Given
        val overview = overviewStore(this)
        val subject = UserPreferencesManagerImpl(overview = overview, addEdit = addEditStore(this))
        val rawKey = stringPreferencesKey("overview_sort_order")

        // When
        overview.edit { it[rawKey] = "NotARealSortOrderConstant" }
        advanceUntilIdle()

        // Then
        expectThat(subject.flow(OverviewPreferenceKeys.SortOrder).first()).isEqualTo(SortOrder.default)
    }

    @Test
    fun `writing one key does not emit on an unrelated key's flow`() = runTest(testDispatcher) {
        // Given
        val subject = manager(this)

        // Then
        subject.flow(AppPreferenceKeys.UseNewQuiz).test {
            advanceUntilIdle()
            expectThat(awaitItem()).isEqualTo(false)

            // When
            subject.update(OverviewPreferenceKeys.ShowCompactVocabularyItem, true)
            advanceUntilIdle()

            // Then
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addLastSearchedItem caps the list at 8 entries and moves duplicates to the front`() = runTest(testDispatcher) {
        // Given
        val subject = manager(this)

        // When
        (1..8).forEach { subject.addLastSearchedItem("word$it") }
        advanceUntilIdle()
        subject.addLastSearchedItem("word3")
        advanceUntilIdle()
        subject.addLastSearchedItem("word9")
        advanceUntilIdle()

        // Then
        val expected = ArrayDeque(
            listOf("word9", "word3", "word8", "word7", "word6", "word5", "word4", "word2"),
        )
        val items = subject.flow(SearchPreferenceKeys.LastSearchedItems).first()
        expectThat(items).isEqualTo(expected)
    }
}
