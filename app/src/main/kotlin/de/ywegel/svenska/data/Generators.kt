@file:Suppress(
    "detekt:MatchingDeclarationName",
    "detekt:MagicNumber",
    "detekt:TooManyFunctions",
)

package de.ywegel.svenska.data

import androidx.annotation.VisibleForTesting
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.model.WordGroup
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
}

@VisibleForTesting
fun vocabulary(id: Int = 1, containerId: Int = 1): Vocabulary = Vocabulary(
    word = "lampa",
    wordHighlights = listOf(1, 2),
    translation = "lamp",
    notes = "",
    wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED),
    gender = Gender.Ultra,
    ending = "n or orna",
    containerId = containerId,
    id = id,
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
fun container(): VocabularyContainer = VocabularyContainer(1, "Swedish vocabulary")

@VisibleForTesting
fun containers(): List<VocabularyContainer> = listOf(
    VocabularyContainer(1, "Words from class"),
    VocabularyContainer(2, "Slang"),
    VocabularyContainer(3, "Simple sentences"),
)

@VisibleForTesting
fun importerChapter(chapter: Int = 1): ImporterChapter = ImporterChapter(
    chapter = "Kapitel $chapter",
    words = listOf(listOf("word1", "translation1"), listOf("word2", "translation1")),
)
