package de.ywegel.svenska.ui.addEdit.models

import de.ywegel.svenska.common.streamOf
import de.ywegel.svenska.data.model.WordGroup
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class ViewWordGroupTest {

    companion object {
        @JvmStatic
        fun validMappings() = streamOf(
            Arguments.of(
                ViewWordGroup.Noun,
                ViewWordSubGroup.Noun(WordGroup.NounSubgroup.OR),
                WordGroup.Noun(WordGroup.NounSubgroup.OR),
            ),
            Arguments.of(
                ViewWordGroup.Verb,
                ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_1),
                WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_1),
            ),
            Arguments.of(ViewWordGroup.Adjective, ViewWordSubGroup.None, WordGroup.Adjective),
            Arguments.of(ViewWordGroup.Other, ViewWordSubGroup.None, WordGroup.Other),
        )

        @JvmStatic
        fun mismatchedMappings() = streamOf(
            Arguments.of(ViewWordGroup.Noun, ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_1), null),
            Arguments.of(ViewWordGroup.Verb, ViewWordSubGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_EN), null),
            Arguments.of(ViewWordGroup.Noun, ViewWordSubGroup.None, null),
            Arguments.of(ViewWordGroup.Verb, ViewWordSubGroup.None, null),
        )

        @JvmStatic
        fun reverseMappings() = streamOf(
            Arguments.of(WordGroup.Noun(WordGroup.NounSubgroup.ER), ViewWordGroup.Noun),
            Arguments.of(WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A), ViewWordGroup.Verb),
            Arguments.of(WordGroup.Adjective, ViewWordGroup.Adjective),
            Arguments.of(WordGroup.Other, ViewWordGroup.Other),
        )
    }

    @ParameterizedTest
    @MethodSource("validMappings")
    fun `toWordGroup maps correctly`(group: ViewWordGroup, subgroup: ViewWordSubGroup, expected: WordGroup) {
        val result = group.toWordGroup(subgroup)
        expectThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("mismatchedMappings")
    fun `toWordGroup returns null for invalid combinations`(
        group: ViewWordGroup,
        subgroup: ViewWordSubGroup,
        ignored: WordGroup?,
    ) {
        val result = group.toWordGroup(subgroup)
        expectThat(result).isNull()
    }

    @ParameterizedTest
    @MethodSource("reverseMappings")
    fun `fromWordGroup maps correctly`(wordGroup: WordGroup, expected: ViewWordGroup) {
        val result = ViewWordGroup.fromWordGroup(wordGroup)
        expectThat(result).isEqualTo(expected)
    }
}
