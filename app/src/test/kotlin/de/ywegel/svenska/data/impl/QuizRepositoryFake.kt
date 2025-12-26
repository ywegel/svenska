package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.QuizRepository
import de.ywegel.svenska.data.model.Vocabulary

class QuizRepositoryFake(private val initialNouns: List<Vocabulary>) : QuizRepository {
    override suspend fun getAllNouns(containerId: Int?): List<Vocabulary> = initialNouns
}
