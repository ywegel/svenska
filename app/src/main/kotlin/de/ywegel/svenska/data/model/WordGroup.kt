package de.ywegel.svenska.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class WordGroup : Parcelable {
    data class Noun(val subgroup: NounSubgroup) : WordGroup()

    data class Verb(val subgroup: VerbSubgroup) : WordGroup()

    data object Adjective : WordGroup()
    data object Other : WordGroup()

    enum class NounSubgroup {
        OR,
        AR,
        ER,
        R,
        N,
        UNCHANGED_ETT,
        UNCHANGED_EN,
        SPECIAL,
        UNDEFINED,
    }

    enum class VerbSubgroup {
        GROUP_1,
        GROUP_2A,
        GROUP_2B,
        GROUP_3,
        GROUP_4_SPECIAL,
        UNDEFINED,
    }

    companion object {
        val default = Other

        // TODO: Remove lazy once "Adjective" is used more often. If we remove lazy, proguard will remove Adjective and I could not find any keep rule to avoid this
        val abstractWordGroups by lazy {
            setOf(Noun(NounSubgroup.UNDEFINED), Verb(VerbSubgroup.UNDEFINED), Adjective, Other)
        }
    }
}
