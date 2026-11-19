package io.github.oungy.wordnest.data.local.study

import androidx.room.withTransaction
import io.github.oungy.wordnest.domain.Rating
import io.github.oungy.wordnest.domain.library.NotebookRules
import java.util.UUID

internal data class LibrarySnapshot(
    val words: List<LibraryWord>,
    val notebooks: List<Notebook>,
    val memberships: List<NotebookWord>,
)

internal class LibraryRepository(private val database: StudyDatabase) {
    private val dao = database.libraryDao()

    suspend fun snapshot(): LibrarySnapshot = database.withTransaction {
        val study = database.studyDao()
        if (!study.hasCatalog()) SampleCatalog.insertInto(study)
        LibrarySnapshot(
            words = dao.words(Rating.Forgot.name, Rating.Unsure.name, masteryDays = 14),
            notebooks = dao.notebooks(),
            memberships = dao.memberships(),
        )
    }

    suspend fun saveNotebook(id: String?, name: String) {
        require(NotebookRules.isValidName(name))
        val display = NotebookRules.normalizeName(name)
        val key = NotebookRules.nameKey(display)
        if (id == null) {
            dao.create(Notebook(UUID.randomUUID().toString(), display, key))
        } else {
            check(dao.rename(id, display, key) == 1)
        }
    }

    suspend fun deleteNotebook(id: String) = dao.deleteNotebook(id)

    suspend fun setWords(notebookId: String, ids: Set<String>) = database.withTransaction {
        dao.clearWords(notebookId)
        dao.addMemberships(ids.map { NotebookWord(notebookId, it) })
    }

    suspend fun setNotebooks(wordId: String, ids: Set<String>) = database.withTransaction {
        dao.clearNotebooks(wordId)
        dao.addMemberships(ids.map { NotebookWord(it, wordId) })
    }

    suspend fun setSaved(wordId: String, saved: Boolean) {
        if (saved) dao.saveWord(SavedWord(wordId)) else dao.unsaveWord(wordId)
    }
}
