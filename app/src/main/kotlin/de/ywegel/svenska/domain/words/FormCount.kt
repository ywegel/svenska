package de.ywegel.svenska.domain.words

import de.ywegel.svenska.data.model.WordGroup

private const val NOUN_AND_VERB_FORMS = 3
private const val ADJECTIVE_FORMS = 2

// TODO: Detect comparative and superlative (-re, -st), they are saved as adjective forms too
val WordGroup.formCount: Int
    get() = when (this) {
        is WordGroup.Noun, is WordGroup.Verb -> NOUN_AND_VERB_FORMS
        WordGroup.Adjective -> ADJECTIVE_FORMS
        WordGroup.Other -> 0
    }
