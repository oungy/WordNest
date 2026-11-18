package io.github.oungy.wordnest.data.local.study

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "study_words")
internal data class StudyWord(
    @PrimaryKey val id: String,
    val spelling: String,
    val partOfSpeech: String,
    val definition: String,
    val chineseDefinition: String,
    val example: String,
    val chineseExample: String,
)

@Entity(tableName = "study_packs")
internal data class StudyPack(
    @PrimaryKey val id: String,
    val position: Int,
    val contentVersion: Int,
)

@Entity(
    tableName = "study_pack_words",
    primaryKeys = ["packId", "wordId"],
    foreignKeys = [
        ForeignKey(entity = StudyPack::class, parentColumns = ["id"], childColumns = ["packId"]),
        ForeignKey(entity = StudyWord::class, parentColumns = ["id"], childColumns = ["wordId"]),
    ],
    indices = [Index("wordId"), Index(value = ["packId", "position"], unique = true)],
)
internal data class StudyPackWord(val packId: String, val wordId: String, val position: Int)

@Entity(
    tableName = "selected_study_packs",
    foreignKeys = [
        ForeignKey(entity = StudyPack::class, parentColumns = ["id"], childColumns = ["packId"]),
    ],
)
internal data class SelectedStudyPack(@PrimaryKey val packId: String)

@Entity(tableName = "study_preferences")
internal data class StudyPreferences(@PrimaryKey val id: Int = 0, val dailyLimit: Int)

@Entity(tableName = "daily_plans")
internal data class DailyPlan(@PrimaryKey val day: Long, val goal: Int)

@Entity(
    tableName = "daily_plan_packs",
    primaryKeys = ["day", "packId"],
    foreignKeys = [
        ForeignKey(entity = DailyPlan::class, parentColumns = ["day"], childColumns = ["day"]),
        ForeignKey(entity = StudyPack::class, parentColumns = ["id"], childColumns = ["packId"]),
    ],
    indices = [Index("packId")],
)
internal data class DailyPlanPack(val day: Long, val packId: String, val position: Int)

@Entity(
    tableName = "daily_plan_words",
    primaryKeys = ["day", "wordId"],
    foreignKeys = [
        ForeignKey(entity = DailyPlan::class, parentColumns = ["day"], childColumns = ["day"]),
        ForeignKey(entity = StudyWord::class, parentColumns = ["id"], childColumns = ["wordId"]),
    ],
    indices = [Index("wordId"), Index(value = ["day", "position"], unique = true)],
)
internal data class DailyPlanWord(val day: Long, val wordId: String, val position: Int)

@Entity(
    tableName = "learning_events",
    primaryKeys = ["day", "mode", "wordId"],
    foreignKeys = [
        ForeignKey(entity = StudyWord::class, parentColumns = ["id"], childColumns = ["wordId"]),
    ],
    indices = [Index("wordId")],
)
internal data class LearningEvent(
    val day: Long,
    val mode: String,
    val wordId: String,
    val rating: String,
    val recordedAtMillis: Long,
)

@Entity(
    tableName = "review_states",
    foreignKeys = [
        ForeignKey(entity = StudyWord::class, parentColumns = ["id"], childColumns = ["wordId"]),
    ],
    indices = [Index("dueDay")],
)
internal data class ReviewState(
    @PrimaryKey val wordId: String,
    val dueDay: Long,
    val intervalDays: Int,
    val ruleVersion: Int,
)
