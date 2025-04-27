package de.ywegel.svenska.data.db

import android.annotation.SuppressLint
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.containers
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.vocabularies
import de.ywegel.svenska.data.vocabulary
import de.ywegel.svenska.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [Vocabulary::class, VocabularyContainer::class],
    version = 1,
    exportSchema = false,
)
abstract class VocabularyDatabase : RoomDatabase() {

    abstract fun vocabulary(): VocabularyDao

    class Callback @Inject constructor(
        private val vocabularyRepository: Provider<VocabularyRepository>,
        @ApplicationScope private val applicationScope: CoroutineScope,
    ) : RoomDatabase.Callback() {
        @SuppressLint("VisibleForTests") // TODO: remove after implementing useful demo data
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val repository = vocabularyRepository.get()

            applicationScope.launch {
                vocabularies().forEach {
                    repository.upsertVocabulary(it)
                }
                repository.upsertVocabulary(vocabulary(id = 1, containerId = 2))
                containers().forEach {
                    repository.upsertContainer(it)
                }
            }
        }
    }
}
