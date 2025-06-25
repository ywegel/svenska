package de.ywegel.svenska.ui.common.vocabulary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.WordGroup

@ReadOnlyComposable
@Composable
fun WordGroup.mainGroupUserFacingString(): String {
    val id = when (this) {
        is WordGroup.Noun -> R.string.lang_noun
        is WordGroup.Verb -> R.string.lang_verb
        WordGroup.Adjective -> R.string.lang_adjective
        WordGroup.Other -> R.string.lang_other
    }

    return stringResource(id)
}

@ReadOnlyComposable
@Composable
fun WordGroup.mainGroupAbbreviation(): String {
    val id = when (this) {
        is WordGroup.Noun -> R.string.lang_noun_abbreviation
        is WordGroup.Verb -> R.string.lang_verb_abbreviation
        WordGroup.Adjective -> R.string.lang_adjective_abbreviation
        WordGroup.Other -> R.string.lang_other_abbreviation
    }

    return stringResource(id)
}

@ReadOnlyComposable
@Composable
fun WordGroup.subGroupAbbreviation(): String? {
    return when (this) {
        is WordGroup.Noun -> this.subgroup.abbreviation()
        is WordGroup.Verb -> this.subgroup.abbreviation()
        else -> null
    }
}

@ReadOnlyComposable
@Composable
fun WordGroup.NounSubgroup.abbreviation(): String {
    val id = when (this) {
        WordGroup.NounSubgroup.OR -> R.string.lang_noun_abbreviation_or
        WordGroup.NounSubgroup.AR -> R.string.lang_noun_abbreviation_ar
        WordGroup.NounSubgroup.ER -> R.string.lang_noun_abbreviation_er
        WordGroup.NounSubgroup.R -> R.string.lang_noun_abbreviation_r
        WordGroup.NounSubgroup.N -> R.string.lang_noun_abbreviation_n
        WordGroup.NounSubgroup.UNCHANGED_ETT,
        WordGroup.NounSubgroup.UNCHANGED_EN,
        -> R.string.lang_noun_abbreviation_unchanged

        WordGroup.NounSubgroup.SPECIAL -> R.string.lang_noun_abbreviation_special
        WordGroup.NounSubgroup.UNDEFINED -> R.string.lang_noun_abbreviation_undefined
    }

    return stringResource(id)
}

@ReadOnlyComposable
@Composable
fun WordGroup.VerbSubgroup.abbreviation(): String {
    val id = when (this) {
        WordGroup.VerbSubgroup.GROUP_1 -> R.string.lang_verb_abbreviation_1
        WordGroup.VerbSubgroup.GROUP_2A -> R.string.lang_verb_abbreviation_2a
        WordGroup.VerbSubgroup.GROUP_2B -> R.string.lang_verb_abbreviation_2b
        WordGroup.VerbSubgroup.GROUP_3 -> R.string.lang_verb_abbreviation_3
        WordGroup.VerbSubgroup.GROUP_4_SPECIAL -> R.string.lang_verb_abbreviation_special
        WordGroup.VerbSubgroup.UNDEFINED -> R.string.lang_verb_abbreviation_undefined
    }

    return stringResource(id)
}
