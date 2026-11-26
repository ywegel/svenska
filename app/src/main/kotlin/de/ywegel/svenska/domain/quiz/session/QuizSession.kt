package de.ywegel.svenska.domain.quiz.session

// A few cards in between, so a wrong answer is not just copied from memory
private const val REPEAT_GAP = 4

private const val ITEMS_PER_ROUND = 25

/**
 * The queue of one quiz session, held by the ViewModel while the session runs. It ends when all items are answered,
 * wrong ones included.
 */
class QuizSession(items: List<QuizItem>) {
    private val queue = ArrayDeque(items)
    private var answeredInRound = 0

    /** The item to ask now, `null` once the session is done. */
    val current: QuizItem? get() = queue.firstOrNull()

    /**
     * Whether to ask "Continue or finish?" before the [current] item, until [continueRound] is called. Open items stay
     * due, nothing is lost.
     */
    val isEndOfRound: Boolean get() = answeredInRound >= ITEMS_PER_ROUND && current != null

    fun continueRound() {
        answeredInRound = 0
    }

    /**
     * Moves on from the [current] item.
     *
     * @param repeat The part to ask again a few items later, e.g. [QuizItem.Forms] if only the forms were wrong.
     * `null` if everything was right. Must be of the same word.
     */
    fun answer(repeat: QuizItem? = null) {
        // Ignored once done, e.g. after a double tap on the last item
        val answered = queue.removeFirstOrNull() ?: return
        require(repeat == null || repeat.vocabulary.id == answered.vocabulary.id) { "Repeat of another word" }
        answeredInRound++
        repeat?.let { queue.add(minOf(REPEAT_GAP, queue.size), it) }
    }
}
