package de.ywegel.svenska.domain.quiz.session

import de.ywegel.svenska.data.model.quiz.QuizCard
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.formsKey
import one.felsen.fsrskt.fsrs6.FsrsCard
import kotlin.time.Instant

// Pulled forward, so the forms come together with their DE_SV card instead of a few days later on their own
private const val FORMS_PULL_FORWARD_SHARE = 0.8

fun planDueCards(
    cards: List<QuizCard>,
    now: Instant,
    startOfToday: Instant,
): List<QuizItem> {
    val byFacet = cards.groupBy { it.facet }
    val deSvCards = byFacet[QuizFacet.DE_SV].orEmpty()
    val deSvByWord = deSvCards.associateBy { it.vocabulary.id }
    val formsByKey = byFacet[QuizFacet.FORMS].orEmpty().associateBy { checkNotNull(it.vocabulary.formsKey) }

    // Duplicates share one forms card, it is asked only with the first of them
    val askedFormsKeys = mutableSetOf<String>()

    val deSvItems = deSvCards.filter { it.isDue(now) }.map { deSv ->
        val formsKey = deSv.vocabulary.formsKey
        val forms = formsKey?.let { formsByKey[it] }
            ?.takeIf { it.fsrs.isAlmostDue(now) && askedFormsKeys.add(formsKey) }
        QuizItem.DeSv(vocabulary = deSv.vocabulary, withForms = forms != null)
    }

    val formsItems = formsByKey
        .filter { (formsKey, forms) -> forms.isDue(now) && formsKey !in askedFormsKeys }
        .map { (_, forms) -> QuizItem.Forms(vocabulary = forms.vocabulary) }

    val svDeItems = byFacet[QuizFacet.SV_DE].orEmpty()
        .filter { it.isDue(now) && deSvByWord[it.vocabulary.id]?.holdsBackSvDe(now, startOfToday) != true }
        .map { QuizItem.SvDe(vocabulary = it.vocabulary) }

    return deSvItems + formsItems + svDeItems
}

private fun QuizCard.isDue(now: Instant): Boolean = fsrs.due <= now

private fun FsrsCard.isAlmostDue(now: Instant): Boolean {
    val lastReview = lastReview ?: return due <= now
    return now - lastReview >= (due - lastReview) * FORMS_PULL_FORWARD_SHARE
}

// Typing the Swedish word already shows both sides, so SV_DE right after would be a free answer
private fun QuizCard.holdsBackSvDe(now: Instant, startOfToday: Instant): Boolean =
    isDue(now) || fsrs.lastReview?.let { it >= startOfToday } == true
