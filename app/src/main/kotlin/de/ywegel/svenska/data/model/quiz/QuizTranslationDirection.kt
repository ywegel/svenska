package de.ywegel.svenska.data.model.quiz

/**
 * The translation facets of a word, without [QuizFacet.FORMS]. Used wherever only a translation fits, so that e.g. a
 * forms card can't end up in the translation card table.
 */
enum class QuizTranslationDirection { SV_DE, DE_SV }

fun QuizTranslationDirection.toQuizFacet(): QuizFacet = when (this) {
    QuizTranslationDirection.SV_DE -> QuizFacet.SV_DE
    QuizTranslationDirection.DE_SV -> QuizFacet.DE_SV
}

/**
 * The translation direction of this facet, or `null` for [QuizFacet.FORMS], which is stored in the forms card table.
 */
fun QuizFacet.toTranslationDirection(): QuizTranslationDirection? = when (this) {
    QuizFacet.SV_DE -> QuizTranslationDirection.SV_DE
    QuizFacet.DE_SV -> QuizTranslationDirection.DE_SV
    QuizFacet.FORMS -> null
}
