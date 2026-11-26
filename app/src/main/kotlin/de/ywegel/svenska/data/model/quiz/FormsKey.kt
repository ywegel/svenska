package de.ywegel.svenska.data.model.quiz

import de.ywegel.svenska.data.model.Vocabulary

/**
 * The key of the forms card of this row, or `null` if the row has no forms, so it has no forms card.
 *
 * Made of the changing [Vocabulary.word] and its three forms, trimmed and in lowercase. The prefix and suffix are not
 * part of it, as the forms belong to the changing word only: `känna sig` and `känna` share their forms card. All rows
 * with the same key share one forms card.
 */
// TODO(formsKey column): Store as an indexed column of Vocabulary, computed only in VocabularyDao's write path. See the rework doc.
val Vocabulary.formsKey: String?
    get() {
        if (!hasForms) return null
        return listOf(word, form1, form2, form3).joinToString("|") { it.orEmpty().trim().lowercase() }
    }
