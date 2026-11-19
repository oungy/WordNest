package io.github.oungy.wordnest.data.local.study

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "notebooks", indices = [Index(value = ["nameKey"], unique = true)])
internal data class Notebook(@PrimaryKey val id: String, val name: String, val nameKey: String)

@Entity(
    tableName = "notebook_words",
    primaryKeys = ["notebookId", "wordId"],
    indices = [Index("wordId")],
    foreignKeys = [
        ForeignKey(
            entity = Notebook::class, parentColumns = ["id"], childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(entity = StudyWord::class, parentColumns = ["id"], childColumns = ["wordId"]),
    ],
)
internal data class NotebookWord(val notebookId: String, val wordId: String)

@Entity(
    tableName = "saved_words",
    foreignKeys = [ForeignKey(entity = StudyWord::class, parentColumns = ["id"], childColumns = ["wordId"])],
)
internal data class SavedWord(@PrimaryKey val wordId: String)

internal data class LibraryWord(
    @Embedded val word: StudyWord,
    val saved: Boolean,
    val mistaken: Boolean,
    val learned: Boolean,
    val mastered: Boolean,
    val needsPractice: Boolean,
)

@Dao
internal interface LibraryDao {
    @Query(
        """
        WITH history AS (
            SELECT wordId, MAX(rowid) AS lastAnswer, MAX(rating = :forgot) AS mistaken
            FROM learning_events GROUP BY wordId
        )
        SELECT w.*, s.wordId IS NOT NULL AS saved, COALESCE(h.mistaken, 0) AS mistaken,
               h.wordId IS NOT NULL AS learned, COALESCE(r.intervalDays >= :masteryDays, 0) AS mastered,
               COALESCE(h.mistaken AND recent.rating IN (:forgot, :unsure), 0) AS needsPractice
        FROM study_words w
        LEFT JOIN saved_words s ON s.wordId = w.id
        LEFT JOIN review_states r ON r.wordId = w.id
        LEFT JOIN history h ON h.wordId = w.id
        LEFT JOIN learning_events AS recent ON recent.rowid = h.lastAnswer
        ORDER BY w.spelling COLLATE NOCASE, w.id
        """,
    )
    suspend fun words(forgot: String, unsure: String, masteryDays: Int): List<LibraryWord>

    @Query("SELECT * FROM notebooks ORDER BY nameKey, id")
    suspend fun notebooks(): List<Notebook>

    @Query("SELECT * FROM notebook_words ORDER BY notebookId, wordId")
    suspend fun memberships(): List<NotebookWord>

    @Insert
    suspend fun create(notebook: Notebook)

    @Query("UPDATE notebooks SET name = :name, nameKey = :nameKey WHERE id = :id")
    suspend fun rename(id: String, name: String, nameKey: String): Int

    @Query("DELETE FROM notebooks WHERE id = :id")
    suspend fun deleteNotebook(id: String)

    @Query("DELETE FROM notebook_words WHERE notebookId = :id")
    suspend fun clearWords(id: String)

    @Query("DELETE FROM notebook_words WHERE wordId = :id")
    suspend fun clearNotebooks(id: String)

    @Insert
    suspend fun addMemberships(entries: List<NotebookWord>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveWord(entry: SavedWord)

    @Query("DELETE FROM saved_words WHERE wordId = :id")
    suspend fun unsaveWord(id: String)
}
