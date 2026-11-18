package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class AddEditUiStateTest {

    @ParameterizedTest
    @MethodSource("endingCounts")
    fun `Hints at too many endings only when there are more endings than forms`(
        uiState: AddEditUiState,
        expected: Boolean,
    ) {
        // When
        val hasTooManyEndings = uiState.hasTooManyEndings

        // Then
        expectThat(hasTooManyEndings).isEqualTo(expected)
    }

    companion object {
        private fun withEndings(wordGroup: ViewWordGroup, endings: String) =
            AddEditUiState(selectedWordGroup = wordGroup, wordWithAnnotation = "ord", endings = endings)

        @JvmStatic
        fun endingCounts(): List<Arguments> = listOf(
            argumentSet("noun with three endings", withEndings(ViewWordGroup.Noun, "-et - -en"), false),
            argumentSet("noun with four endings", withEndings(ViewWordGroup.Noun, "-et - -en -x"), true),
            argumentSet("verb with four endings", withEndings(ViewWordGroup.Verb, "-er -de -t -x"), true),
            argumentSet("adjective with two endings", withEndings(ViewWordGroup.Adjective, "-t -a"), false),
            argumentSet("adjective with three endings", withEndings(ViewWordGroup.Adjective, "-t -a -x"), true),
            argumentSet("other has no forms", withEndings(ViewWordGroup.Other, "-a -b -c -d"), false),
            argumentSet(
                "written out forms ignore the endings",
                withEndings(ViewWordGroup.Noun, "-et - -en -x").copy(formsMode = FormsMode.WrittenOut),
                false,
            ),
        )
    }
}
