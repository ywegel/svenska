package de.ywegel.svenska.data.model.quiz

import de.ywegel.svenska.data.model.Vocabulary

/** A vocabulary row together with its [VocabularyPoolState], for the vocabulary lists. */
data class VocabularyWithPoolState(val vocabulary: Vocabulary, val poolState: VocabularyPoolState)
