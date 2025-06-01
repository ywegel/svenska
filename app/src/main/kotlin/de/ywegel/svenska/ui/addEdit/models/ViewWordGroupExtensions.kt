package de.ywegel.svenska.ui.addEdit.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.vocabulary.abbreviation

@ReadOnlyComposable
@Composable
fun ViewWordGroup.mainGroupAbbreviation(): String {
    val id = when (this) {
        ViewWordGroup.Noun -> R.string.lang_noun_abbreviation
        ViewWordGroup.Verb -> R.string.lang_verb_abbreviation
        ViewWordGroup.Adjective -> R.string.lang_adjective_abbreviation
        ViewWordGroup.Other -> R.string.lang_other_abbreviation
    }

    return stringResource(id)
}

@ReadOnlyComposable
@Composable
fun ViewWordGroup.subGroupAbbreviation(subGroup: ViewWordSubGroup): String? {
    return when (this) {
        ViewWordGroup.Noun,
        ViewWordGroup.Verb,
        -> subGroup.abbreviation()

        else -> null
    }
}

@ReadOnlyComposable
@Composable
fun ViewWordSubGroup.abbreviation(): String? {
    return when (this) {
        is ViewWordSubGroup.Noun -> this.type.abbreviation()
        is ViewWordSubGroup.Verb -> this.type.abbreviation()
        ViewWordSubGroup.None -> null
    }
}
