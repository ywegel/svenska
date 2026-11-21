package de.ywegel.svenska.data.model.quiz

/**
 * Each vocabulary has these 3 facets. They are what is quizzed of a vocabulary. Each facet is its own FSRS card ([FacetCard]).
 *
 * Domain and UI only use this type and always address a card as vocabulary + facet. The repository maps it to the
 * two card tables: [SV_DE] and [DE_SV] to [TranslationFacetCard] of that row, [FORMS] to the [FormsFacetCard] shared
 * by all rows with the same [de.ywegel.svenska.data.model.Vocabulary.formsKey].
 *
 * The facets are unlocked in order: [SV_DE] when the word is introduced, [DE_SV] after the first GOOD or EASY on
 * SV_DE, [FORMS] after an SV_DE streak of at least 3 (see [QuizPoolEntry.svDeStreak]).
 */
enum class QuizFacet { SV_DE, DE_SV, FORMS }

/**
 * The facets stored in [TranslationFacetCard]. Only used inside the data layer, so that a forms card can't end up in
 * the translation table.
 */
enum class QuizTranslationDirection { SV_DE, DE_SV }

fun QuizTranslationDirection.toQuizFacet(): QuizFacet = when (this) {
    QuizTranslationDirection.SV_DE -> QuizFacet.SV_DE
    QuizTranslationDirection.DE_SV -> QuizFacet.DE_SV
}

/**
 * The translation direction of this facet, or `null` for [QuizFacet.FORMS], which is stored in [FormsFacetCard].
 */
fun QuizFacet.toTranslationDirection(): QuizTranslationDirection? = when (this) {
    QuizFacet.SV_DE -> QuizTranslationDirection.SV_DE
    QuizFacet.DE_SV -> QuizTranslationDirection.DE_SV
    QuizFacet.FORMS -> null
}
