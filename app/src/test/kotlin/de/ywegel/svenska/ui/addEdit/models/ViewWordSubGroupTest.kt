package de.ywegel.svenska.ui.addEdit.models

import de.ywegel.svenska.common.streamOf
import de.ywegel.svenska.data.model.WordGroup
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ViewWordSubGroupTest {

    companion object {
        @JvmStatic
        fun fromWordGroupMappings() = streamOf(
            Arguments.of(WordGroup.Noun(WordGroup.NounSubgroup.AR), ViewWordSubGroup.Noun(WordGroup.NounSubgroup.AR)),
            Arguments.of(
                WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
                ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
            ),
            Arguments.of(WordGroup.Adjective, ViewWordSubGroup.None),
            Arguments.of(WordGroup.Other, ViewWordSubGroup.None),
        )
    }

    @ParameterizedTest
    @MethodSource("fromWordGroupMappings")
    fun `fromWordGroup maps correctly`(input: WordGroup, expected: ViewWordSubGroup) {
        val result = ViewWordSubGroup.fromWordGroup(input)
        expectThat(result).isEqualTo(expected)
    }
}
