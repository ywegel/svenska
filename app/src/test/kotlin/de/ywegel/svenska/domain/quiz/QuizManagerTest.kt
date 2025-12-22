package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.vocabularies
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import de.ywegel.svenska.domain.quiz.strategies.TranslationWithoutEndingsQuizStrategy
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import kotlin.random.Random

class QuizManagerTest {

    @Test
    fun `Shuffled QuizManager should shuffle words`() {
        val vocabularies = vocabularies()
        val manager = QuizManager(
            TranslationWithoutEndingsQuizStrategy(TranslateMode.NativeToSwedish),
            loadVocabularies = {
                vocabularies
            },
            containerId = 1,
            shuffleWords = true,
            random = Random(5),
        )

        runBlocking { manager.startQuiz() }

        expectThat(manager.vocabularyList).containsExactly(vocabularies.shuffled(Random(5)))
    }

    @Test
    fun `Non-shuffle QuizManager should not shuffle words`() {
        val vocabularies = vocabularies()
        val manager = QuizManager(
            TranslationWithoutEndingsQuizStrategy(TranslateMode.NativeToSwedish),
            loadVocabularies = {
                vocabularies
            },
            containerId = 1,
            shuffleWords = false,
            random = Random(5),
        )

        runBlocking { manager.startQuiz() }

        expectThat(manager.vocabularyList).containsExactly(vocabularies)
    }
}
