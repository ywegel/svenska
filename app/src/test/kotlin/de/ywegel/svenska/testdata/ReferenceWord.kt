package de.ywegel.svenska.testdata

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup

/**
 * A real Swedish word with all its forms, used as shared test data.
 *
 * @param endings Endings as written in the source, for example "-n -or -orna" or "-n sidor sidorna".
 * @param forms Written out forms without prefix and suffix. Nouns: definite singular, plural, definite plural.
 * Verbs: present, past, supine. Adjectives: neuter, plural.
 * @param text The entry exactly as written in the source: "känna sig (-er, -de, -t)".
 * @param formsNeedReview Whether the forms built from the endings have to be reviewed. [forms] stay correct anyway.
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
    val formsNeedReview: Boolean = false,
) {
    override fun toString(): String = text

    fun toVocabulary(containerId: Int = 0) = Vocabulary(
        word = word,
        wordPrefix = prefix,
        wordSuffix = suffix,
        translation = "",
        gender = gender,
        wordGroup = wordGroup,
        ending = endings.joinToString(" "),
        form1 = forms.getOrNull(0),
        form2 = forms.getOrNull(1),
        form3 = forms.getOrNull(2),
        formsNeedReview = formsNeedReview,
        containerId = containerId,
    )
}
