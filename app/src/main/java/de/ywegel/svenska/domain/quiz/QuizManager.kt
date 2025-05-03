package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.UserAnswer

class QuizManager<A : UserAnswer, AnswerResult : Any>(
    private val strategy: QuizStrategy<A, AnswerResult>,
    private val vocabularyRepository: VocabularyRepository,
    private val containerId: Int?,
) {
    private val vocabularyList: MutableList<Vocabulary> = mutableListOf()
    private var currentIndex = 0

    suspend fun startQuiz() {
        vocabularyList.clear()
        vocabularyList.addAll(
            vocabularyRepository.getAllVocabulariesSnapshot(containerId).shuffled(),
        )
        currentIndex = 0
    }

    fun hasMoreQuestions(): Boolean = currentIndex < vocabularyList.size - 1

    fun hasPreviousQuestion(): Boolean = currentIndex > 0

    fun getCurrentQuestion(): QuizQuestion<A> {
        // TODO: Do a index check here. The vocabularyList could be empty
        // TODO: Write tests for QuizManager
        return strategy.generateQuestion(vocabularyList[currentIndex])
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
}
