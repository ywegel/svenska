package de.ywegel.svenska.data.model.quiz

// Shared datatype for data, domain and ui. Mapped to QuizTranslationDirection and the FacetCard data classes. Domain only sees QuizFacet, which should simplify domain and ui code. Repository and FsrsMapping.kt does the mapping.
enum class QuizFacet { SV_DE, DE_SV, FORMS }

// Data layer internal type
enum class QuizTranslationDirection { SV_DE, DE_SV }

fun QuizTranslationDirection.toQuizFacet(): QuizFacet = when (this) {
    QuizTranslationDirection.SV_DE -> QuizFacet.SV_DE
    QuizTranslationDirection.DE_SV -> QuizFacet.DE_SV
}

fun QuizFacet.toTranslationDirection(): QuizTranslationDirection? = when (this) {
    QuizFacet.SV_DE -> QuizTranslationDirection.SV_DE
    QuizFacet.DE_SV -> QuizTranslationDirection.DE_SV
    QuizFacet.FORMS -> null
}
