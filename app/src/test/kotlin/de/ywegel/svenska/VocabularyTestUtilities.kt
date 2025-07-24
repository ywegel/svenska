package de.ywegel.svenska

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import de.ywegel.svenska.data.model.Vocabulary

fun assertVocabularyEqualsIgnoringTimestamps(expected: Vocabulary, actual: Vocabulary) {
    assertThat(expected).isEqualTo(
        actual.copy(
            lastEdited = expected.lastEdited,
            created = expected.created,
        ),
    )
}

fun assertVocabularyListEqualsIgnoringTimestamps(
    expectedList: List<Vocabulary>,
    actualList: List<Vocabulary>,
    sort: Boolean = false,
) {
    assertThat(actualList).isNotNull().given { actual ->
        assertThat(actualList.size).isEqualTo(expectedList.size)

        val comparisonPairs = if (sort) {
            actual
                .sortedBy { it.word + it.translation }
                .zip(
                    expectedList.sortedBy { it.word + it.translation },
                )
        } else {
            actual.zip(expectedList)
        }

        comparisonPairs.forEach { (expectedItem, actualItem) ->
            assertVocabularyEqualsIgnoringTimestamps(expectedItem, actualItem)
        }
    }
}
