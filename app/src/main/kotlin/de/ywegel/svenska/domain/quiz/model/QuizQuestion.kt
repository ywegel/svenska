package de.ywegel.svenska.domain.quiz.model

import android.util.Log
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup

private const val TAG = "QuizQuestion.kt"

data class QuizQuestion<A : UserAnswer>(
    val vocabularyId: Int,
    val prompt: String,
    val expectedAnswer: A,
    val promptData: AdditionalInfo = AdditionalInfo.None,
)

sealed class AdditionalInfo {
    data class PromptInfo(
        val wordGroup: WordGroup?,
        val endings: String?,
        val gender: Gender? = null,
    ) : AdditionalInfo()

    data class SolutionInfo(
        val wordGroup: WordGroup?,
        val endings: String?,
        val gender: Gender? = null,
    ) : AdditionalInfo()

    object None : AdditionalInfo()

    companion object {
        fun createFromVocabulary(vocabulary: Vocabulary, translateMode: TranslateMode): AdditionalInfo {
            return when (translateMode) {
                TranslateMode.Random -> {
                    Log.e(
                        TAG,
                        "createFromVocabulary: The random case should never happen. Random should have been " +
                            "transformed to either SwedishToNative or NativeToSwedish before calling " +
                            "'createFromVocabulary'!",
                    )
                    None
                }

                TranslateMode.SwedishToNative -> PromptInfo(
                    wordGroup = vocabulary.wordGroup,
                    endings = vocabulary.ending,
                    gender = vocabulary.gender,
                )

                TranslateMode.NativeToSwedish -> SolutionInfo(
                    wordGroup = vocabulary.wordGroup,
                    endings = vocabulary.ending,
                    gender = vocabulary.gender,
                )
            }
        }
    }
}
