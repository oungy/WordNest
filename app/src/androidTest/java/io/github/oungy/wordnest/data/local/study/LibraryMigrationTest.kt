package io.github.oungy.wordnest.data.local.study

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryMigrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val name = "library-migration-v1.db"

    @After
    fun removeFixture() {
        context.deleteDatabase(name)
    }

    @Test
    fun versionOneUpgradesWithItsPendingPlanReviewAndMistakeIntact() = runBlocking {
        val today = LocalDate.ofEpochDay(100)
        createVersionOneDatabase()
        val database = StudyDatabase.open(context, name)
        try {
            val study = StudyRepository(database)
            val dashboard = study.dashboard(today)
            assertEquals(listOf("foundations"), dashboard.selectedPackIds)
            assertEquals(2, dashboard.dailyLimit)
            assertEquals(listOf("foundations"), dashboard.planPackIds)
            assertEquals(2, dashboard.newTotal)
            assertEquals(1, dashboard.newCompleted)
            assertEquals("w2", dashboard.nextNewWord?.id)
            assertEquals(listOf("w1"), study.dashboard(today.plusDays(1)).dueWords.map { it.id })
            val library = LibraryRepository(database)
            val initial = library.snapshot()
            assertTrue(initial.notebooks.isEmpty())
            assertTrue(initial.words.single { it.word.id == "w1" }.mistaken)
            library.saveNotebook(null, "Migrated")
            val notebook = library.snapshot().notebooks.single()
            library.setWords(notebook.id, setOf("w1"))
            library.setSaved("w1", true)
            assertEquals(listOf(NotebookWord(notebook.id, "w1")), library.snapshot().memberships)
        } finally {
            database.close()
        }
    }

    private fun createVersionOneDatabase() {
        val path = "${StudyDatabase::class.java.name}/1.json"
        val schema = instrumentation.context.assets.open(path).bufferedReader().use {
            JSONObject(it.readText()).getJSONObject("database")
        }
        context.deleteDatabase(name)
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { db ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                fun create(sql: String) = db.execSQL(sql.replace("\${TABLE_NAME}", table))
                create(entity.getString("createSql"))
                val indices = entity.optJSONArray("indices") ?: continue
                for (j in 0 until indices.length()) create(indices.getJSONObject(j).getString("createSql"))
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) db.execSQL(setup.getString(i))
            db.execSQL("INSERT INTO study_words VALUES ('w1','one','n.','one','一','One.','一。'), ('w2','two','n.','two','二','Two.','二。')")
            db.execSQL("INSERT INTO study_packs VALUES ('foundations',0,1)")
            db.execSQL("INSERT INTO study_pack_words VALUES ('foundations','w1',0), ('foundations','w2',1)")
            db.execSQL("INSERT INTO selected_study_packs VALUES ('foundations')")
            db.execSQL("INSERT INTO study_preferences VALUES (0,2)")
            db.execSQL("INSERT INTO daily_plans VALUES (100,2)")
            db.execSQL("INSERT INTO daily_plan_packs VALUES (100,'foundations',0)")
            db.execSQL("INSERT INTO daily_plan_words VALUES (100,'w1',0), (100,'w2',1)")
            db.execSQL("INSERT INTO learning_events VALUES (100,'New','w1','Forgot',1)")
            db.execSQL("INSERT INTO review_states VALUES ('w1',101,1,1)")
            db.version = 1
        }
    }
}
