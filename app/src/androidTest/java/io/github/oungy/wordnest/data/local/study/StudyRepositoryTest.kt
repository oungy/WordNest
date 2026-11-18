package io.github.oungy.wordnest.data.local.study

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "study-instrumentation.db"
    private val today = LocalDate.of(2026, 11, 18)
    private lateinit var database: StudyDatabase
    private lateinit var repository: StudyRepository

    @Before
    fun openDatabase() {
        context.deleteDatabase(databaseName)
        reopen()
    }

    @After
    fun closeDatabase() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun selectionAndLimitChangesApplyAfterTheSavedPlanAndSurviveReopening() = runBlocking {
        assertTrue(repository.dashboard(today).selectedPackIds.isEmpty())
        assertNull(repository.ensurePlan(today).plan)
        repository.selectPacks(setOf("everyday", "foundations"))
        repository.setDailyLimit(3)
        val plans = List(2) { async { repository.ensurePlan(today) } }.awaitAll()
        assertEquals(plans[0], plans[1])
        assertEquals(listOf("notice-verb", "route-noun", "steady-adjective"), planWords(today))
        assertEquals(3, plans.first().plan?.goal)

        val answers = List(2) {
            async { repository.rate(today, StudyMode.New, "notice-verb", Rating.Forgot) }
        }.awaitAll()
        assertEquals(listOf(false, true), answers.map { it.accepted }.sorted())
        repository.selectPacks(setOf("everyday"))
        repository.setDailyLimit(1)
        database.close()
        reopen()

        val saved = repository.dashboard(today)
        assertEquals(listOf("everyday"), saved.selectedPackIds)
        assertEquals(1, saved.dailyLimit)
        assertEquals(listOf("foundations", "everyday"), saved.planPackIds)
        assertEquals(1, saved.newCompleted)
        assertEquals(3, saved.newTotal)
        assertEquals("route-noun", saved.nextNewWord?.id)
        assertEquals(listOf(1, 1), saved.packs.map { it.learned })
        assertTrue(saved.dueWords.isEmpty())
        assertEquals(1, countEvents())

        val tomorrow = today.plusDays(1)
        assertEquals(listOf("notice-verb"), repository.dashboard(tomorrow).dueWords.map { it.id })
        val nextPlan = repository.ensurePlan(tomorrow)
        assertEquals(1, nextPlan.newTotal)
        assertEquals(listOf("everyday"), nextPlan.planPackIds)
        assertEquals("route-noun", nextPlan.nextNewWord?.id)
    }

    @Test
    fun reviewSurvivesClearingSelectionAndAdvancesOnlyOnce() = runBlocking {
        repository.selectPacks(setOf("foundations"))
        repository.setDailyLimit(1)
        val first = repository.ensurePlan(today).nextNewWord!!.id
        assertTrue(repository.rate(today, StudyMode.New, first, Rating.Forgot).accepted)
        repository.selectPacks(emptySet())
        val tomorrow = today.plusDays(1)
        val due = repository.ensurePlan(tomorrow)
        assertNull(due.plan)
        assertEquals(listOf(first), due.dueWords.map { it.id })
        assertTrue(repository.rate(tomorrow, StudyMode.Review, first, Rating.Remembered).accepted)
        assertFalse(repository.rate(tomorrow, StudyMode.Review, first, Rating.Remembered).accepted)
        database.close()
        reopen()
        assertTrue(repository.dashboard(tomorrow).dueWords.isEmpty())
        assertEquals(listOf(first), repository.dashboard(tomorrow.plusDays(3)).dueWords.map { it.id })
        assertEquals(2, countEvents())
    }

    @Test
    fun exhaustedBooksShowZeroAndNeverScheduleLearnedWordsAsNew() = runBlocking {
        repository.selectPacks(setOf("foundations"))
        repository.setDailyLimit(200)
        var snapshot = repository.ensurePlan(today)
        assertNotNull(snapshot.plan)
        assertEquals(snapshot.packs.first().total, snapshot.newTotal)
        repeat(snapshot.newTotal) {
            snapshot = repository.rate(today, StudyMode.New, snapshot.nextNewWord!!.id, Rating.Remembered).dashboard
        }
        assertEquals(snapshot.newTotal, snapshot.newCompleted)
        val next = repository.ensurePlan(today.plusDays(1))
        assertNull(next.plan)
        assertEquals(0, next.newTotal)
        assertEquals(snapshot.newTotal, next.packs.first().learned)
    }

    private fun reopen() {
        database = StudyDatabase.open(context, databaseName)
        repository = StudyRepository(database)
    }

    private fun planWords(day: LocalDate): List<String> =
        database.openHelper.readableDatabase.query(
            "SELECT wordId FROM daily_plan_words WHERE day = ? ORDER BY position",
            arrayOf(day.toEpochDay()),
        ).use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
        }

    private fun countEvents(): Int =
        database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM learning_events").use { cursor ->
            check(cursor.moveToFirst())
            cursor.getInt(0)
        }
}
