package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.words.WordGroupMatcher

enum class FormsMode { Endings, WrittenOut }

/**
 * Written out forms that can't be saved, because the ending would no longer split into the same forms again.
 * Empty forms at the end are fine, a noun without plural has none.
 */
enum class WrittenFormsError {
    /** An empty form before a filled one. */
    Gap,

    /** A form with a space. The fields only take the changing word, the screen adds prefix and suffix. */
    Space,
}

/** Everything the forms are built from. A confirmed review only counts for exactly these inputs. */
data class FormInputs(val word: String, val endings: List<String>, val wordGroup: WordGroup)

private const val MIN_SHARED_STEM = 2
private const val NOUN_AND_VERB_FORMS = 3
private const val ADJECTIVE_FORMS = 2

/** Endings can be separated by commas or spaces: "-er, -de, -t" and "-er -de -t". */
fun splitEndings(text: String): List<String> = text.split(',', ' ').map { it.trim() }.filter { it.isNotEmpty() }

// TODO: Detect comparative and superlative (-re, -st), they are saved as adjective forms too
val WordGroup.formCount: Int
    get() = when (this) {
        is WordGroup.Noun, is WordGroup.Verb -> NOUN_AND_VERB_FORMS
        WordGroup.Adjective -> ADJECTIVE_FORMS
        WordGroup.Other -> 0
    }

/**
 * Splits a form into the part it shares with the base word and the part that changes: drömmen -> dröm + men.
 * Forms that barely share anything change completely: ta -> tog.
 */
fun String.splitAtChange(baseWord: String): Pair<String, String> {
    val shared = commonPrefixWith(baseWord).length
    return if (shared < MIN_SHARED_STEM) "" to this else take(shared) to drop(shared)
}

/**
 * The word group the endings point to, if it is worth suggesting: the endings are complete, the group is known and
 * the user selected something else. Endings are only complete once they are enough for the selected word group too,
 * so typing the second of three noun endings doesn't suggest an adjective.
 */
fun suggestWordGroup(inputs: FormInputs, selected: WordGroup?): WordGroup? {
    if (inputs.word.isBlank()) return null
    val suggestion = WordGroupMatcher.determineWordGroup(inputs.word, inputs.endings)
    val isComplete = suggestion.formCount > 0 &&
        inputs.endings.size == suggestion.formCount &&
        inputs.endings.size >= inputs.wordGroup.formCount
    return suggestion.takeIf { isComplete && !it.isUndefined && it != selected }
}

private val WordGroup.isUndefined: Boolean
    get() = (this as? WordGroup.Noun)?.subgroup == WordGroup.NounSubgroup.UNDEFINED ||
        (this as? WordGroup.Verb)?.subgroup == WordGroup.VerbSubgroup.UNDEFINED
