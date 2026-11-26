package de.ywegel.svenska.data.impl

import androidx.room.withTransaction
import de.ywegel.svenska.data.QuizPoolRepository
import de.ywegel.svenska.data.db.QuizPoolDao
import de.ywegel.svenska.data.db.ReviewDao
import de.ywegel.svenska.data.db.VocabularyDao
import de.ywegel.svenska.data.db.VocabularyDatabase
import de.ywegel.svenska.data.db.VocabularyForms
import de.ywegel.svenska.data.db.quiz.FsrsColumns
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.impl.quiz.toFsrsCard
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.PoolRemoval
import de.ywegel.svenska.data.model.quiz.QuizCard
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.formsKey
import javax.inject.Inject
import kotlin.time.Instant

class QuizPoolRepositoryImpl @Inject constructor(
    private val database: VocabularyDatabase,
    private val quizDao: QuizPoolDao,
    private val vocabularyDao: VocabularyDao,
    private val reviewDao: ReviewDao,
) : QuizPoolRepository {

    override suspend fun queueToPool(words: List<Vocabulary>, now: Instant) {
        database.withTransaction {
            vocabularyDao.updateForms(words.map { it.toForms() })
            quizDao.insertIgnoringExisting(words.map { QuizPoolEntry(it.id, now.toEpochMilliseconds()) })
        }
    }

    override suspend fun removeFromPool(vocabularyId: Int): PoolRemoval = quizDao.removeFromPool(vocabularyId)

    override suspend fun countIntroducedSince(since: Instant): Int =
        quizDao.countIntroducedSince(since.toEpochMilliseconds())

    // TODO(formsKey column): Filter the existing forms keys in getFormsCandidates, then this only passes the query through
    override suspend fun formsCandidates(minSvDeStreak: Int): List<Vocabulary> {
        val existingFormsKeys = reviewDao.getAllFormsCards().mapTo(HashSet()) { it.formsKey }
        return quizDao.getFormsCandidates(minSvDeStreak)
            .filter { it.formsKey != null && it.formsKey !in existingFormsKeys }
    }

    override suspend fun queuedWords(limit: Int): List<Vocabulary> =
        if (limit > 0) quizDao.getQueued(limit) else emptyList()

    override suspend fun introduce(words: List<Vocabulary>, forms: List<Vocabulary>, now: Instant): List<QuizCard> {
        if (words.isEmpty() && forms.isEmpty()) return emptyList()
        quizDao.introduce(
            wordIds = words.map { it.id },
            formsKeysByRow = forms.associate { it.id to checkNotNull(it.formsKey) },
            now = now.toEpochMilliseconds(),
        )
        val newCard = FsrsColumns.newCard(due = now.toEpochMilliseconds()).toFsrsCard()
        return words.map { QuizCard(it, QuizFacet.SV_DE, newCard) } + forms.map {
            QuizCard(
                vocabulary = it,
                facet = QuizFacet.FORMS,
                fsrs = newCard,
            )
        }
    }

    private fun Vocabulary.toForms(): VocabularyForms {
        return VocabularyForms(id, form1, form2, form3, formsNeedReview)
    }
}
