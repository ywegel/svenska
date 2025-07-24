package de.ywegel.svenska.domain.quiz.strategies

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.domain.quiz.QuizStrategy
import de.ywegel.svenska.domain.quiz.model.AdditionalInfo
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.UserAnswer

class OnlyEndingsQuizStrategy : QuizStrategy<UserAnswer.OnlyEndingsAnswer, Boolean> {
    override fun generateQuestion(vocabulary: Vocabulary): QuizQuestion<UserAnswer.OnlyEndingsAnswer> {
        return QuizQuestion(
            vocabularyId = vocabulary.id,
            prompt = vocabulary.word,
            expectedAnswer = UserAnswer.OnlyEndingsAnswer(vocabulary.ending),
            // Show the additional info in the solution, to not spoiler the user about the ending
            promptData = AdditionalInfo.SolutionInfo(
                wordGroup = vocabulary.wordGroup,
                endings = vocabulary.ending,
                gender = vocabulary.gender,
            ),
        )
    }

    override fun validateAnswer(
        question: QuizQuestion<UserAnswer.OnlyEndingsAnswer>,
        userAnswer: UserAnswer.OnlyEndingsAnswer,
    ): Boolean {
        return ComparisonHelpers.compareEndings(
            expected = question.expectedAnswer.endings,
            userInput = userAnswer.endings,
        )
    }
}
