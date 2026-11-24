package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.db.quiz.QuizTranslationDirection
import de.ywegel.svenska.data.db.quiz.TranslationFacetCard
import de.ywegel.svenska.data.impl.quiz.toFsrsColumns
import de.ywegel.svenska.data.model.Vocabulary
import kotlinx.datetime.TimeZone
import one.felsen.fsrskt.fsrs6.FsrsCard
import one.felsen.fsrskt.fsrs6.FsrsRating
import one.felsen.fsrskt.helper.DateTimeHelper.startOfNextDay
import kotlin.time.Instant

private const val FORMS_UNLOCK_STREAK = 3

// A quiz day starts at 4 AM, so a late session still counts towards the previous day
const val DAY_CUTOFF_HOUR = 4

val FsrsRating.isPassing: Boolean
    get() = when (this) {
        FsrsRating.GOOD, FsrsRating.EASY -> true
        FsrsRating.AGAIN, FsrsRating.HARD -> false
    }

fun nextSvDeStreak(streak: Int, rating: FsrsRating): Int = if (rating.isPassing) streak + 1 else 0

fun QuizPoolEntry.isFormsReady(vocabulary: Vocabulary): Boolean =
    svDeStreak >= FORMS_UNLOCK_STREAK && !vocabulary.formsNeedReview && vocabulary.formsQuestion() != null

fun newDeSvCard(vocabularyId: Int, unlockedAt: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()) =
    TranslationFacetCard(
        vocabularyId = vocabularyId,
        direction = QuizTranslationDirection.DE_SV,
        fsrs = FsrsCard(due = unlockedAt.startOfNextDay(DAY_CUTOFF_HOUR, timeZone)).toFsrsColumns(),
    )