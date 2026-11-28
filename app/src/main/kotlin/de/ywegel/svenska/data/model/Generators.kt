@file:Suppress(
    "detekt:MatchingDeclarationName",
    "detekt:MagicNumber",
    "detekt:TooManyFunctions",
    "LongParameterList",
)

package de.ywegel.svenska.data.model

import androidx.annotation.VisibleForTesting
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.VocabularyPoolState
import de.ywegel.svenska.data.model.quiz.VocabularyWithPoolState

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
    wordGroup: WordGroup = WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED),
    lastEdited: Long = System.currentTimeMillis(),
    created: Long = System.currentTimeMillis(),
): Vocabulary = Vocabulary(
    word = word,
    wordHighlights = listOf(1 to 2),
    translation = translation,
    notes = notes,
    wordGroup = wordGroup,
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
        wordHighlights = listOf(1 to 2),
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
        wordHighlights = listOf(1 to 2),
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
        wordHighlights = listOf(1 to 3),
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
        wordHighlights = listOf(2 to 3),
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

/**
 * One row per possible [VocabularyPoolState], each on a word it can occur on. Active words have SV_DE from their
 * introduction on, so these are all combinations of the facets: SV_DE alone, + DE_SV, + FORMS (after a reset, which
 * deletes DE_SV but keeps the shared forms card) and all three. Additionally a word with `formsNeedReview` and a phrase
 * without forms. The ids are distinct, as lists use them as keys.
 */
@VisibleForTesting
fun vocabulariesWithPoolState(containerId: Int = 1): List<VocabularyWithPoolState> {
    val orNoun = WordGroup.Noun(WordGroup.NounSubgroup.OR)
    val arNoun = WordGroup.Noun(WordGroup.NounSubgroup.AR)
    val specialNoun = WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)
    val verb = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A)
    val translations = VocabularyPoolState.Active(setOf(QuizFacet.SV_DE, QuizFacet.DE_SV))

    return listOf(
        vocabulary(id = 1, word = "lampa", translation = "lamp", wordGroup = orNoun)
            to VocabularyPoolState.NotInPool,
        vocabulary(id = 2, word = "flicka", translation = "girl", wordGroup = orNoun)
            to VocabularyPoolState.Queued,
        vocabulary(id = 3, word = "känna", translation = "feel", wordGroup = verb)
            .copy(wordSuffix = "sig", gender = null, ending = "er de t")
            to VocabularyPoolState.Active(setOf(QuizFacet.SV_DE)),
        vocabulary(id = 4, word = "dröm", translation = "biggest dream", wordGroup = arNoun)
            .copy(wordPrefix = "högsta", ending = "men mar marna")
            to translations,
        vocabulary(id = 5, word = "bil", translation = "car", wordGroup = arNoun)
            .copy(ending = "en ar arna")
            to VocabularyPoolState.Active(setOf(QuizFacet.SV_DE, QuizFacet.FORMS)),
        vocabulary(id = 6, word = "gata", translation = "street", wordGroup = orNoun)
            to VocabularyPoolState.Active(QuizFacet.entries.toSet()),
        vocabulary(id = 7, word = "klass", translation = "class", wordGroup = specialNoun)
            .copy(ending = "en er arna", formsNeedReview = true)
            to translations,
        vocabulary(id = 8, word = "tack så mycket", translation = "thank you very much", wordGroup = WordGroup.Other)
            .copy(gender = null, ending = "")
            to translations,
        vocabulary(id = 9, word = "kyrka", translation = "church", wordGroup = orNoun)
            to VocabularyPoolState.Paused,
    ).map { (vocabulary, poolState) ->
        VocabularyWithPoolState(vocabulary.copy(containerId = containerId), poolState)
    }
}

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
