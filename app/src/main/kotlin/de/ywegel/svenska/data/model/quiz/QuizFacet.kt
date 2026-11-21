package de.ywegel.svenska.data.model.quiz

/**
 * Each vocabulary has these 3 facets. They are what is quizzed of a vocabulary. Each facet is its own FSRS card.
 *
 * Domain and UI only use this type and always address a card as vocabulary + facet. The repository maps it to the
 * two card tables of the data layer: [SV_DE] and [DE_SV] to the translation card of that row, [FORMS] to the forms
 * card shared by all rows with the same [formsKey]. The card tables are not visible outside the data layer.
 *
 * The facets are unlocked in order: [SV_DE] when the word is introduced, [DE_SV] after the first GOOD or EASY on
 * SV_DE, [FORMS] after an SV_DE streak of at least 3.
 */
enum class QuizFacet { SV_DE, DE_SV, FORMS }
