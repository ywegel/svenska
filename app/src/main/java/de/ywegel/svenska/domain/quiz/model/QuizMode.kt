package de.ywegel.svenska.domain.quiz.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class QuizMode : Parcelable {
    @Parcelize
    data class Translate(val mode: TranslateMode) : QuizMode()

    @Parcelize
    data class TranslateWithEndings(val mode: TranslateMode) : QuizMode()

    @Parcelize
    data object OnlyEndings : QuizMode()

    // @Parcelize
    // data class Endings(val mode: EndingsMode) : QuizMode()
}
