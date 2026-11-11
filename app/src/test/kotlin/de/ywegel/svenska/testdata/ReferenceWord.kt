package de.ywegel.svenska.testdata

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup

/**
 * A real Swedish word with all its forms, used as shared test data.
 *
 * @param endings Endings as written in the source, for example "-n -or -orna" or "-n sidor sidorna".
 * @param forms Written out forms without prefix and suffix. Nouns: definite singular, plural, definite plural.
 * Verbs: present, past, supine. Adjectives: neuter, plural.
 * @param text The entry exactly as written in the source: "känna sig (-er, -de, -t)".
 */
data class ReferenceWord(
    val word: String,
    val endings: List<String>,
    val forms: List<String>,
    val wordGroup: WordGroup,
    val gender: Gender?,
    val text: String,
    val prefix: String = "",
    val suffix: String = "",
) {
    override fun toString(): String = text
}
