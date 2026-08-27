package de.ywegel.svenska.data.preferences

import de.ywegel.svenska.data.model.SortOrder
import de.ywegel.svenska.data.preferences.keys.AddEditPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.AppPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.OnboardingPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.OverviewPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.SearchPreferenceKeys
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo

/**
 * AI-generated test.
 *
 * Pins the exact on-disk format of every preference: the DataStore key
 * string, the store it lives in, and the persisted text of each enum constant. If a preference
 * key or enum constant name changes, existing users' stored values silently stop matching and
 * fall back to defaults — these tests catch that before it ships.
 */
class PersistedPreferenceFormatTest {

    private val sortOrderOnDisk = mapOf(
        "Word" to SortOrder.Word,
        "Translation" to SortOrder.Translation,
        "Created" to SortOrder.Created,
        "LastEdited" to SortOrder.LastEdited,
    )

    private val preferencesOnDisk = mapOf(
        "OverviewPreferenceKeys.SortOrder" to "Overview/overview_sort_order",
        "OverviewPreferenceKeys.Revert" to "Overview/overview_sort_order_revert",
        "OverviewPreferenceKeys.ShowCompactVocabularyItem" to "Overview/overview_show_compact_vocabulary_item",
        "SearchPreferenceKeys.LastSearchedItems" to "Overview/search_sort_last_searched_items",
        "SearchPreferenceKeys.OnlineRedirectType" to "Overview/search_online_redirect_type",
        "OnboardingPreferenceKeys.HasCompleted" to "Overview/app_has_completed_onboarding",
        "AppPreferenceKeys.UseNewQuiz" to "Overview/app_uses_new_quiz",
        "AddEditPreferenceKeys.AnnotationInformationHidden" to "AddEdit/add_edit_annotation_information_hidden",
    )

    private val coveredEnumPreferences = setOf("OverviewPreferenceKeys.SortOrder")

    @Test
    fun `SortOrder is written to disk under its exact constant name`() {
        sortOrderOnDisk.forEach { (onDisk, constant) ->
            expectThat(OverviewPreferenceKeys.SortOrder.encode(constant))
                .describedAs("SortOrder.$constant written to DataStore")
                .isEqualTo(onDisk)
        }
    }

    @Test
    fun `SortOrder decodes every value a previous version could have written`() {
        sortOrderOnDisk.forEach { (onDisk, constant) ->
            expectThat(OverviewPreferenceKeys.SortOrder.decode(onDisk))
                .describedAs("\"$onDisk\" read back from DataStore")
                .isEqualTo(constant)
        }
    }

    @Test
    fun `no SortOrder constant was renamed, added or removed`() {
        expectThat(SortOrder.entries.map { it.name })
            .describedAs("SortOrder constant names, which are persisted verbatim")
            .containsExactlyInAnyOrder(sortOrderOnDisk.keys)
    }

    @Test
    fun `the default SortOrder still resolves to a constant that exists on disk`() {
        expectThat(OverviewPreferenceKeys.SortOrder.default.name)
            .describedAs("default SortOrder")
            .isEqualTo("Created")
    }

    @Test
    fun `no preference key string or store assignment changed`() {
        val actual = allPreferenceKeys().mapValues { (_, key) -> "${key.store}/${key.key.name}" }

        expectThat(actual)
            .describedAs("persisted preference keys")
            .isEqualTo(preferencesOnDisk)
    }

    @Test
    fun `every enum-backed preference has its constant names pinned above`() {
        val enumBacked = allPreferenceKeys()
            .filterValues { it.default is Enum<*> }
            .keys

        expectThat(enumBacked)
            .describedAs("preferences persisted as Enum.name")
            .containsExactlyInAnyOrder(coveredEnumPreferences)
    }

    private fun allPreferenceKeys(): Map<String, PreferenceKey<*, *>> = listOf(
        OverviewPreferenceKeys,
        SearchPreferenceKeys,
        AppPreferenceKeys,
        AddEditPreferenceKeys,
        OnboardingPreferenceKeys,
    ).flatMap { holder ->
        holder::class.java.declaredFields
            .filter { PreferenceKey::class.java.isAssignableFrom(it.type) }
            .map { field ->
                field.isAccessible = true
                "${holder::class.java.simpleName}.${field.name}" to field.get(holder) as PreferenceKey<*, *>
            }
    }.toMap()
}
