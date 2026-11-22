package de.ywegel.svenska.data.db.quiz

import de.ywegel.svenska.data.model.quiz.QuizFacet

/**
 * The facets stored in [TranslationFacetCard]. Only used inside the data layer, so that a forms card can't end up in
 * the translation table. Domain and UI use [QuizFacet] instead.
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
