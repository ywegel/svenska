package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.quiz.QuizTranslationDirection
import de.ywegel.svenska.domain.quiz.compare.AnswerVerdict
import de.ywegel.svenska.domain.quiz.compare.FormsVerdict
import one.felsen.fsrskt.fsrs6.FsrsRating

/**
 * A typo is good when typing German, as only the Swedish spelling is learned, and hard when typing Swedish.
 */
fun AnswerVerdict.toRating(direction: QuizTranslationDirection): FsrsRating = when (this) {
    AnswerVerdict.Correct -> FsrsRating.GOOD
    is AnswerVerdict.Typo -> when (direction) {
        QuizTranslationDirection.SV_DE -> FsrsRating.GOOD
        QuizTranslationDirection.DE_SV -> FsrsRating.HARD
    }
    AnswerVerdict.Wrong -> FsrsRating.AGAIN
}

/**
 * Forms don't tolerate typos.
 */
fun FormsVerdict.toRating(): FsrsRating = if (isCorrect) FsrsRating.GOOD else FsrsRating.AGAIN

/**
 * Lets the user correct the automatic rating, e.g. a synonym the app doesn't know. Applying it makes [rating] the final
 * rating.
 */
enum class RatingOverride(val rating: FsrsRating) {
    ACTUALLY_CORRECT(FsrsRating.GOOD),
    EASY(FsrsRating.EASY),
}

/**
 * The override the UI offers for this rating, `null` if there is none. Each rating has at most one.
 */
val FsrsRating.availableOverride: RatingOverride?
    get() = when (this) {
        FsrsRating.AGAIN, FsrsRating.HARD -> RatingOverride.ACTUALLY_CORRECT
        FsrsRating.GOOD -> RatingOverride.EASY
        FsrsRating.EASY -> null
    }
