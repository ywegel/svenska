package de.ywegel.svenska.ui.addEdit.models

import de.ywegel.svenska.data.model.WordGroup

sealed interface ViewWordSubGroup {
    data class Noun(val type: WordGroup.NounSubgroup) : ViewWordSubGroup
    data class Verb(val type: WordGroup.VerbSubgroup) : ViewWordSubGroup
    data object None : ViewWordSubGroup

    companion object {
        fun fromWordGroup(wordGroup: WordGroup): ViewWordSubGroup {
            return when (wordGroup) {
                is WordGroup.Noun -> Noun(wordGroup.subgroup)
                is WordGroup.Verb -> Verb(wordGroup.subgroup)
                else -> None
            }
        }
    }
}
