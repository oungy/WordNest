package io.github.oungy.wordnest.data.local.study

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.oungy.wordnest.domain.Rating
import java.time.LocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "library-instrumentation.db"
    private val today = LocalDate.of(2026, 11, 19)
    private lateinit var database: StudyDatabase
    private lateinit var library: LibraryRepository

    @Before
    fun open() {
        context.deleteDatabase(databaseName)
        database = StudyDatabase.open(context, databaseName)
        library = LibraryRepository(database)
    }

    @After
    fun close() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun normalizedNamesAreUniqueAcrossConcurrentCreatesAndRenames() = runBlocking {
        val outcomes = listOf("　旅行Ａ ", "旅行a").map { name ->
            async { runCatching { library.saveNotebook(null, name) } }
        }.awaitAll()
        assertEquals(1, outcomes.count { it.isSuccess })
        assertTrue(outcomes.single { it.isFailure }.exceptionOrNull() is SQLiteConstraintException)
        val first = library.snapshot().notebooks.single()
        library.saveNotebook(null, "课堂")
        assertTrue(runCatching { library.saveNotebook(first.id, " 课堂 ") }.exceptionOrNull() is SQLiteConstraintException)
        assertTrue(runCatching { library.saveNotebook(null, " ") }.exceptionOrNull() is IllegalArgumentException)
        library.saveNotebook(first.id, " 旅行A ")
        assertEquals("旅行A", library.snapshot().notebooks.single { it.id == first.id }.name)
    }

    @Test
    fun changingMembershipAndDeletingNotebookPreserveOtherOwnersAndHistory() = runBlocking {
        val study = StudyRepository(database)
        study.selectPacks(setOf("foundations"))
        val word = study.ensurePlan(today).nextNewWord!!.id
        study.rate(today, StudyMode.New, word, Rating.Forgot)
        library.saveNotebook(null, "A")
        library.saveNotebook(null, "B")
        val (a, b) = library.snapshot().notebooks
        val sense = "book-verb"
        library.setWords(a.id, setOf(word, "book-noun", sense))
        library.setWords(a.id, setOf(word, sense))
        repeat(2) { library.setNotebooks(word, setOf(a.id, b.id)) }
        assertEquals(
            setOf(NotebookWord(a.id, word), NotebookWord(a.id, sense), NotebookWord(b.id, word)),
            library.snapshot().memberships.toSet(),
        )
        assertTrue(runCatching { library.setWords(a.id, setOf("missing-word")) }.isFailure)
        assertEquals(2, library.snapshot().memberships.count { it.notebookId == a.id })
        repeat(2) { library.setSaved(word, true) }
        library.deleteNotebook(a.id)
        database.close()
        database = StudyDatabase.open(context, databaseName)
        library = LibraryRepository(database)
        val reopened = library.snapshot()
        assertEquals(listOf(NotebookWord(b.id, word)), reopened.memberships)
        assertEquals(2, reopened.words.count { it.word.spelling == "book" })
        val entry = reopened.words.single { it.word.id == word }
        assertTrue(entry.saved && entry.learned && entry.mistaken && entry.needsPractice)
        assertFalse(entry.mastered)
        val afterDelete = StudyRepository(database)
        afterDelete.selectPacks(emptySet())
        assertEquals(listOf(word), afterDelete.dashboard(today.plusDays(1)).dueWords.map { it.id })
        library.setSaved(word, false)
        library.setNotebooks(word, emptySet())
        val removed = library.snapshot()
        assertTrue(removed.memberships.isEmpty())
        assertFalse(removed.words.single { it.word.id == word }.saved)
        assertEquals(1, afterDelete.dashboard(today).newCompleted)
    }

    @Test
    fun successfulReviewsClearCurrentPracticeButKeepTheMistakeRecord() = runBlocking {
        val study = StudyRepository(database)
        study.selectPacks(setOf("foundations"))
        val id = study.ensurePlan(today).nextNewWord!!.id
        study.rate(today, StudyMode.New, id, Rating.Forgot)
        var date = today.plusDays(1)
        for (interval in listOf(3, 6, 12, 24)) {
            assertTrue(study.rate(date, StudyMode.Review, id, Rating.Remembered).accepted)
            val entry = library.snapshot().words.single { it.word.id == id }
            assertTrue(entry.mistaken && entry.learned)
            assertFalse(entry.needsPractice)
            assertEquals(interval >= 14, entry.mastered)
            date = date.plusDays(interval.toLong())
        }
        assertTrue(study.rate(date, StudyMode.Review, id, Rating.Unsure).accepted)
        val entry = library.snapshot().words.single { it.word.id == id }
        assertTrue(entry.needsPractice)
        assertFalse(entry.mastered)
    }
}
