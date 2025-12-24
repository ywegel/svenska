package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Vocabulary

interface QuizRepository {
    suspend fun getAllNouns(containerId: Int?): List<Vocabulary>
}
