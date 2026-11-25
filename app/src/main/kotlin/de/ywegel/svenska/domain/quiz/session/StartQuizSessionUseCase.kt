package de.ywegel.svenska.domain.quiz.session

import de.ywegel.svenska.data.QuizCardRepository
import de.ywegel.svenska.data.QuizPoolRepository
import de.ywegel.svenska.data.model.quiz.formsKey
import de.ywegel.svenska.domain.quiz.DAY_CUTOFF_HOUR
import de.ywegel.svenska.domain.quiz.FORMS_UNLOCK_STREAK
import de.ywegel.svenska.domain.quiz.formsQuestion
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import one.felsen.fsrskt.helper.DateTimeHelper.toDayNumber
import javax.inject.Inject
import kotlin.time.Instant

// TODO: Make both a setting
private const val NEW_PER_DAY = 10
private const val BACKLOG_LIMIT = 100

/**
 * Plans the items of a new quiz session. First introduces the new cards the daily limit allows, forms cards before
 * queued words, as those words are already known. Then plans the due cards together with the new ones, so a new forms
 * card is asked along with a due DE_SV card of its word.
 */
class StartQuizSessionUseCase @Inject constructor(
    private val quizCardRepository: QuizCardRepository,
    private val quizPoolRepository: QuizPoolRepository,
    private val timeZone: TimeZone,
) {
    /**
     * @param now The start of the session, truncated to milliseconds.
     */
    suspend operator fun invoke(now: Instant): List<QuizItem> {
        val startOfToday = now.startOfDay()
        val cards = quizCardRepository.getActiveCards()

        val budget = newWordsAllowedToday(
            introducedToday = quizPoolRepository.countIntroducedSince(startOfToday),
            dueCount = cards.count { it.fsrs.due <= now },
            newPerDay = NEW_PER_DAY,
            backlogLimit = BACKLOG_LIMIT,
        )
        val formsToIntroduce = quizPoolRepository.formsCandidates(minSvDeStreak = FORMS_UNLOCK_STREAK)
            .filter { it.formsQuestion() != null }
            // Rows that share their forms introduce one card through the smallest id. Filtered before, so that a row
            // without forms to ask doesn't hide another row with the same forms key
            .groupBy { it.formsKey }
            .values
            .map { rows -> rows.minBy { it.id } }
            .take(budget)
        val wordsToIntroduce = quizPoolRepository.queuedWords(limit = budget - formsToIntroduce.size)
        val introduced = quizPoolRepository.introduce(wordsToIntroduce, formsToIntroduce, now)

        return planDueCards(cards + introduced, now, startOfToday)
    }

    private fun Instant.startOfDay(): Instant =
        LocalDate.fromEpochDays(toDayNumber(DAY_CUTOFF_HOUR, timeZone)).atTime(DAY_CUTOFF_HOUR, 0).toInstant(timeZone)
}
