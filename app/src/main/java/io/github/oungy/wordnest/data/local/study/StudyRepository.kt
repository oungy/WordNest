package io.github.oungy.wordnest.data.local.study

import androidx.room.withTransaction
import io.github.oungy.wordnest.domain.Rating
import io.github.oungy.wordnest.domain.ReviewRuleVersion
import io.github.oungy.wordnest.domain.reviewInterval
import io.github.oungy.wordnest.domain.selectNewWords
import java.time.Instant
import java.time.LocalDate

internal const val DefaultDailyLimit = 5
internal val DailyLimits = 1..200

internal enum class StudyMode { New, Review }

internal data class StudyDashboard(
    val day: LocalDate,
    val packs: List<PackProgress>,
    val selectedPackIds: List<String>,
    val dailyLimit: Int,
    val plan: DailyPlan?,
    val planPackIds: List<String>,
    val newTotal: Int,
    val newCompleted: Int,
    val nextNewWord: StudyWord?,
    val dueWords: List<StudyWord>,
)

internal data class StudyAnswer(val accepted: Boolean, val dashboard: StudyDashboard)

internal class StudyRepository(private val database: StudyDatabase) {
    private val dao = database.studyDao()

    suspend fun dashboard(day: LocalDate): StudyDashboard = database.withTransaction {
        seedCatalog()
        readDashboard(day)
    }

    suspend fun selectPacks(packIds: Set<String>) = database.withTransaction {
        require(packIds.all(SampleCatalog.packIds::contains))
        seedCatalog()
        dao.clearPackSelection()
        dao.insertSelection(packIds.map(::SelectedStudyPack))
    }

    suspend fun setDailyLimit(limit: Int) {
        require(limit in DailyLimits)
        dao.savePreferences(StudyPreferences(dailyLimit = limit))
    }

    suspend fun ensurePlan(day: LocalDate): StudyDashboard = database.withTransaction {
        seedCatalog()
        val date = day.toEpochDay()
        if (dao.plan(date) == null) {
            val selected = dao.selectedPackIds()
            val goal = dao.dailyLimit() ?: DefaultDailyLimit
            val words = availableWords(selected, goal)
            if (words.isNotEmpty()) {
                dao.insertPlan(DailyPlan(date, goal))
                dao.insertPlanPacks(selected.mapIndexed { position, id -> DailyPlanPack(date, id, position) })
                dao.insertPlanWords(words.mapIndexed { position, id -> DailyPlanWord(date, id, position) })
            }
        }
        readDashboard(day)
    }

    suspend fun rate(day: LocalDate, mode: StudyMode, wordId: String, rating: Rating): StudyAnswer =
        database.withTransaction {
            val date = day.toEpochDay()
            val expectedWord = when (mode) {
                StudyMode.New -> dao.pendingPlanWords(date).firstOrNull()
                StudyMode.Review -> dao.dueWords(date).firstOrNull()
            }
            val accepted = expectedWord?.id == wordId
            if (accepted) {
                val previous = if (mode == StudyMode.Review) dao.reviewState(wordId) else null
                val interval = reviewInterval(rating, previous?.intervalDays)
                dao.insertEvent(LearningEvent(date, mode.name, wordId, rating.name, Instant.now().toEpochMilli()))
                dao.saveReviewState(ReviewState(wordId, date + interval, interval, ReviewRuleVersion))
            }
            StudyAnswer(accepted, readDashboard(day))
        }

    private suspend fun seedCatalog() {
        if (!dao.hasCatalog()) SampleCatalog.insertInto(dao)
    }

    private suspend fun availableWords(selected: List<String>, limit: Int): List<String> =
        selectNewWords(selected.map { dao.unlearnedPackWords(it, limit) }, limit)

    private suspend fun readDashboard(day: LocalDate): StudyDashboard {
        val date = day.toEpochDay()
        val selected = dao.selectedPackIds()
        val goal = dao.dailyLimit() ?: DefaultDailyLimit
        val plan = dao.plan(date)
        val pending = if (plan != null) dao.pendingPlanWords(date) else emptyList()
        val total = if (plan != null) dao.planSize(date) else availableWords(selected, goal).size
        return StudyDashboard(
            day = day,
            packs = dao.packProgress(),
            selectedPackIds = selected,
            dailyLimit = goal,
            plan = plan,
            planPackIds = if (plan == null) emptyList() else dao.planPackIds(date),
            newTotal = total,
            newCompleted = if (plan == null) 0 else total - pending.size,
            nextNewWord = pending.firstOrNull(),
            dueWords = dao.dueWords(date),
        )
    }
}
