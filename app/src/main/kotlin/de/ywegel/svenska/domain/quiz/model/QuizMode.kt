package de.ywegel.svenska.domain.quiz.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class QuizMode : Parcelable {
    abstract val shuffleWords: Boolean

    @Parcelize
    data class Translate(val mode: TranslateMode, override val shuffleWords: Boolean) : QuizMode()

    @Parcelize
    data class TranslateWithEndings(val mode: TranslateMode, override val shuffleWords: Boolean) : QuizMode()

    @Parcelize
    data class OnlyEndings(override val shuffleWords: Boolean) : QuizMode()
}
