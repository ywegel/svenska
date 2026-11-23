package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.words.FormBuilder

sealed interface FormsQuestion {
    /** The gender to tap, only asked for nouns. */
    val gender: Gender? get() = null

    /** The forms to type, in the order of the input fields. */
    val forms: List<String>

    data class Noun(override val gender: Gender, val plural: String) : FormsQuestion {
        override val forms get() = listOf(plural)
    }

    data class Verb(val past: String, val supine: String) : FormsQuestion {
        override val forms get() = listOf(past, supine)
    }

    data class Adjective(val neuter: String, val plural: String) : FormsQuestion {
        override val forms get() = listOf(neuter, plural)
    }
}

fun Vocabulary.formsQuestion(): FormsQuestion? {
    if (formsNeedReview) return null
    return when (wordGroup) {
        is WordGroup.Noun -> if (gender != null && form2 != null) FormsQuestion.Noun(gender, form2) else null
        is WordGroup.Verb -> if (form2 != null && form3 != null) FormsQuestion.Verb(form2, form3) else null
        WordGroup.Adjective -> adjectiveQuestion()
        WordGroup.Other -> null
    }
}

/**
 * Only ask for adjektive forms, if they differ from base + t/a.
 */
private fun Vocabulary.adjectiveQuestion(): FormsQuestion.Adjective? {
    if (form1 == null || form2 == null) return null
    val regular = FormBuilder.build(word, listOf("-t", "-a"), WordGroup.Adjective).forms
    return if (regular != listOf(form1, form2)) FormsQuestion.Adjective(form1, form2) else null
}
