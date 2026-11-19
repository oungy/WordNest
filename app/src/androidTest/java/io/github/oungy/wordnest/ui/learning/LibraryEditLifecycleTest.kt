package io.github.oungy.wordnest.ui.learning

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.oungy.wordnest.data.local.study.StudyDatabase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryEditLifecycleTest {
    @Test
    fun confirmedEditSurvivesScreenCancellationAndIsVisibleOnReentry() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "library-edit-lifecycle.db"
        context.deleteDatabase(name)
        val db = StudyDatabase.open(context, name)
        val model = StudyViewModel(context.applicationContext as Application, db)
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        try {
            withTimeout(10_000) {
                val edit = model.submitLibraryEdit {
                    entered.complete(Unit)
                    release.await()
                    saveNotebook(null, "Trip")
                }
                entered.await()
                val leaving = async(start = CoroutineStart.UNDISPATCHED) { model.awaitLibraryEdit(edit) }
                leaving.cancelAndJoin()
                val reopened = async(start = CoroutineStart.UNDISPATCHED) { model.readLibrary() }
                release.complete(Unit)
                assertEquals("Trip", reopened.await().notebooks.single().name)
                assertEquals("Trip", model.awaitLibraryEdit(edit)?.notebooks?.single()?.name)
            }
        } finally {
            release.complete(Unit)
            model.viewModelScope.cancel()
            db.close()
            context.deleteDatabase(name)
        }
    }
}
