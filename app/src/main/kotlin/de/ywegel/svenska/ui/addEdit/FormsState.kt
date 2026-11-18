package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.data.model.WordGroup

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
