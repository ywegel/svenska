@file:Suppress(
    "detekt:MatchingDeclarationName",
    "detekt:MagicNumber",
    "detekt:TooManyFunctions",
    "LongParameterList",
)

package de.ywegel.svenska.data.model

import androidx.annotation.VisibleForTesting
import de.ywegel.svenska.ui.wordImporter.ImporterChapter

// TODO: create annotation VisibleForTestingAndPreviews

object GeneratorConstants {
    @VisibleForTesting
    const val LONG_STRING: String =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et " +
            "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut " +
            "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse " +
            "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
            "culpa qui officia deserunt mollit anim id est laborum."

    @VisibleForTesting
    const val TEST_DATE = 1735686000000
}

@VisibleForTesting
fun vocabulary(
    id: Int = 1,
    word: String = "lampa",
    translation: String = "lamp",
    containerId: Int = 1,
    isFavorite: Boolean = false,
    irregularPronunciation: String? = null,
    notes: String = "",
    lastEdited: Long = System.currentTimeMillis(),
    created: Long = System.currentTimeMillis(),
): Vocabulary = Vocabulary(
    word = word,
    wordHighlights = listOf(1, 2),
    translation = translation,
    notes = notes,
    wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED),
    gender = Gender.Ultra,
    ending = "n or orna",
    containerId = containerId,
    id = id,
    isFavorite = isFavorite,
    irregularPronunciation = irregularPronunciation,
    lastEdited = lastEdited,
    created = created,
)

@VisibleForTesting
fun vocabularies(containerId: Int = 1): List<Vocabulary> = listOf(
    Vocabulary(
        word = "lampa",
        wordHighlights = listOf(1, 2),
        translation = "lamp",
        notes = "",
        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED),
        gender = Gender.Ultra,
        ending = "n or orna",
        containerId = containerId,
        id = 1,
    ),
    Vocabulary(
        word = "lång",
        wordHighlights = listOf(1, 2),
        translation = "long",
        notes = "",
        wordGroup = WordGroup.Adjective,
        gender = null,
        ending = "t a",
        containerId = containerId,
        id = 2,
    ),
    Vocabulary(
        word = "eller",
        wordHighlights = listOf(1, 3),
        translation = "or",
        notes = "",
        wordGroup = WordGroup.Other,
        gender = null,
        ending = "",
        containerId = containerId,
        id = 3,
    ),
    Vocabulary(
        word = "fråga",
        wordHighlights = listOf(2, 3),
        translation = "ask",
        notes = "",
        wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.UNDEFINED),
        gender = null,
        ending = "r de t",
        containerId = containerId,
        isFavorite = true,
        id = 5,
    ),
    Vocabulary(
        word = "kan",
        wordHighlights = emptyList(),
        translation = "can",
        notes = "",
        wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.UNDEFINED),
        gender = null,
        ending = "",
        containerId = containerId,
        irregularPronunciation = "kannn",
        id = 6,
    ),
)

@VisibleForTesting
fun container(id: Int = 1): VocabularyContainer = VocabularyContainer(id, "Swedish vocabulary")

@VisibleForTesting
fun containers(): List<VocabularyContainer> = listOf(
    VocabularyContainer(1, "Chapter 1"),
    VocabularyContainer(2, "Slang"),
    VocabularyContainer(3, "Simple sentences"),
)

@VisibleForTesting
fun importerChapter(chapter: Int = 1): ImporterChapter = ImporterChapter(
    chapter = "Kapitel $chapter",
    words = listOf(listOf("word1", "translation1"), listOf("word2", "translation1")),
)
