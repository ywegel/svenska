package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import kotlin.random.Random

class QuizManager<A : UserAnswer, AnswerResult : Any>(
    private val strategy: QuizStrategy<A, AnswerResult>,
    private val loadVocabularies: suspend (containerId: Int?) -> List<Vocabulary>,
    private val containerId: Int?,
    private val shuffleWords: Boolean,
    private val random: Random = Random,
) {
    private val _vocabularyList: MutableList<Vocabulary> = mutableListOf()
    val vocabularyList: List<Vocabulary> = _vocabularyList
    private var currentIndex = 0

    suspend fun startQuiz() {
        _vocabularyList.clear()
        _vocabularyList.addAll(
            if (shuffleWords) {
                loadVocabularies(containerId).shuffled(random)
            } else {
                loadVocabularies(containerId)
            },
        )
        currentIndex = 0
    }

    fun hasMoreQuestions(): Boolean = currentIndex < _vocabularyList.size - 1

    fun hasPreviousQuestion(): Boolean = currentIndex > 0

    fun getCurrentQuestion(): QuizQuestion<A> {
        // TODO: Do a index check here. The vocabularyList could be empty
        // TODO: Write tests for QuizManager
        return strategy.generateQuestion(_vocabularyList[currentIndex])
    }

    fun goToPreviousQuestion() {
        if (currentIndex != 0) currentIndex--
    }

    /**
     * @return if a next question is available
     */
    fun goToNextQuestion(): Boolean {
        return if (hasMoreQuestions()) {
            currentIndex++
            true
        } else {
            false
        }
    }

    fun validateAnswer(userInput: A, question: QuizQuestion<A>): AnswerResult {
        return strategy.validateAnswer(question, userInput)
    }

    fun getQuizStatistics(): Pair<Int, Int> {
        return Pair(1, 1)
    }

    fun currentVocabularyIsFavorite(): Boolean? = _vocabularyList.getOrNull(currentIndex)?.isFavorite
}
