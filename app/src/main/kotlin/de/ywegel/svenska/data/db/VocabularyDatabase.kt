package de.ywegel.svenska.data.db

import android.annotation.SuppressLint
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.model.containers
import de.ywegel.svenska.data.model.vocabularies
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [Vocabulary::class, VocabularyContainer::class],
    version = 1,
    exportSchema = true,
)
abstract class VocabularyDatabase : RoomDatabase() {

    abstract fun vocabulary(): VocabularyDao

    abstract fun container(): ContainerDao

    abstract fun search(): SearchDao

    class Callback @Inject constructor(
        private val vocabularyRepositoryProvider: Provider<VocabularyRepository>,
        private val containerRepositoryProvider: Provider<ContainerRepository>,
        @ApplicationScope private val applicationScope: CoroutineScope,
    ) : RoomDatabase.Callback() {
        @SuppressLint("VisibleForTests") // TODO: remove after implementing useful demo data
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val vocabularyRepository = vocabularyRepositoryProvider.get()
            val containerRepository = containerRepositoryProvider.get()

            applicationScope.launch {
                vocabularies().forEach {
                    vocabularyRepository.upsertVocabulary(it)
                }
                vocabularyRepository.upsertVocabulary(vocabulary(id = 1, containerId = 2))
                containers().forEach {
                    containerRepository.upsertContainer(it)
                }
            }
        }
    }
}
