package io.github.oungy.wordnest.data.local.study

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert

@Database(
    entities = [
        StudyWord::class,
        StudyPack::class,
        StudyPackWord::class,
        SelectedStudyPack::class,
        StudyPreferences::class,
        DailyPlan::class,
        DailyPlanPack::class,
        DailyPlanWord::class,
        LearningEvent::class,
        ReviewState::class,
        Notebook::class,
        NotebookWord::class,
        SavedWord::class,
    ],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
internal abstract class StudyDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao
    abstract fun libraryDao(): LibraryDao

    companion object {
        fun open(context: Context, name: String = "wordnest-study.db"): StudyDatabase =
            Room.databaseBuilder(context.applicationContext, StudyDatabase::class.java, name).build()
    }
}

internal data class PackProgress(val id: String, val learned: Int, val total: Int)

@Dao
internal interface StudyDao {
    @Insert
    suspend fun insertWords(words: List<StudyWord>)

    @Insert
    suspend fun insertPacks(packs: List<StudyPack>)

    @Insert
    suspend fun insertPackWords(entries: List<StudyPackWord>)

    @Query("SELECT EXISTS(SELECT 1 FROM study_packs)")
    suspend fun hasCatalog(): Boolean

    @Query(
        """
        SELECT pack.id, COUNT(review.wordId) AS learned, COUNT(entry.wordId) AS total
        FROM study_packs AS pack
        LEFT JOIN study_pack_words AS entry ON entry.packId = pack.id
        LEFT JOIN review_states AS review ON review.wordId = entry.wordId
        GROUP BY pack.id
        ORDER BY pack.position
        """,
    )
    suspend fun packProgress(): List<PackProgress>

    @Query(
        """
        SELECT pack.id FROM study_packs AS pack
        INNER JOIN selected_study_packs AS selected ON pack.id = selected.packId
        ORDER BY pack.position
        """,
    )
    suspend fun selectedPackIds(): List<String>

    @Query("DELETE FROM selected_study_packs")
    suspend fun clearPackSelection()

    @Insert
    suspend fun insertSelection(packs: List<SelectedStudyPack>)

    @Query("SELECT dailyLimit FROM study_preferences WHERE id = 0")
    suspend fun dailyLimit(): Int?

    @Upsert
    suspend fun savePreferences(preferences: StudyPreferences)

    @Query(
        """
        SELECT entry.wordId FROM study_pack_words AS entry
        WHERE entry.packId = :packId
          AND NOT EXISTS (SELECT 1 FROM review_states AS review WHERE review.wordId = entry.wordId)
        ORDER BY entry.position
        LIMIT :limit
        """,
    )
    suspend fun unlearnedPackWords(packId: String, limit: Int): List<String>

    @Query("SELECT * FROM daily_plans WHERE day = :day")
    suspend fun plan(day: Long): DailyPlan?

    @Insert
    suspend fun insertPlan(plan: DailyPlan)

    @Insert
    suspend fun insertPlanPacks(planPacks: List<DailyPlanPack>)

    @Query("SELECT packId FROM daily_plan_packs WHERE day = :day ORDER BY position")
    suspend fun planPackIds(day: Long): List<String>

    @Insert
    suspend fun insertPlanWords(planWords: List<DailyPlanWord>)

    @Query("SELECT COUNT(*) FROM daily_plan_words WHERE day = :day")
    suspend fun planSize(day: Long): Int

    @Query(
        """
        SELECT word.* FROM daily_plan_words AS entry
        INNER JOIN study_words AS word ON word.id = entry.wordId
        WHERE entry.day = :day
          AND NOT EXISTS (SELECT 1 FROM review_states AS review WHERE review.wordId = entry.wordId)
        ORDER BY entry.position
        """,
    )
    suspend fun pendingPlanWords(day: Long): List<StudyWord>

    @Query(
        """
        SELECT word.* FROM review_states AS review
        INNER JOIN study_words AS word ON word.id = review.wordId
        WHERE review.dueDay <= :day ORDER BY review.dueDay, review.wordId
        """,
    )
    suspend fun dueWords(day: Long): List<StudyWord>

    @Query("SELECT * FROM review_states WHERE wordId = :wordId")
    suspend fun reviewState(wordId: String): ReviewState?

    @Upsert
    suspend fun saveReviewState(state: ReviewState)

    @Insert
    suspend fun insertEvent(event: LearningEvent)
}
