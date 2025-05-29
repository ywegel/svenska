package de.ywegel.svenska.ui.addEdit.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.WordGroup

enum class ViewWordGroup {
    Noun,
    Verb,
    Adjective,
    Other,
    ;

    fun toWordGroup(subGroup: ViewWordSubGroup): WordGroup? {
        return when (this) {
            Noun -> if (subGroup is ViewWordSubGroup.Noun) WordGroup.Noun(subGroup.type) else null
            Verb -> if (subGroup is ViewWordSubGroup.Verb) WordGroup.Verb(subGroup.type) else null
            Adjective -> WordGroup.Adjective
            Other -> WordGroup.Other
        }
    }

    companion object {
        fun fromWordGroup(wordGroup: WordGroup): ViewWordGroup {
            return when (wordGroup) {
                is WordGroup.Noun -> Noun
                is WordGroup.Verb -> Verb
                WordGroup.Adjective -> Adjective
                WordGroup.Other -> Other
            }
        }
    }
}

@ReadOnlyComposable
@Composable
fun ViewWordGroup.userFacingString(): String {
    val id = when (this) {
        ViewWordGroup.Noun -> R.string.lang_noun
        ViewWordGroup.Verb -> R.string.lang_verb
        ViewWordGroup.Adjective -> R.string.lang_adjective
        ViewWordGroup.Other -> R.string.lang_other
    }

    return stringResource(id)
}
