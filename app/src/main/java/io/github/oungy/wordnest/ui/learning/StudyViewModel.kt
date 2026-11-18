package io.github.oungy.wordnest.ui.learning

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.oungy.wordnest.data.local.study.StudyDashboard
import io.github.oungy.wordnest.data.local.study.StudyDatabase
import io.github.oungy.wordnest.data.local.study.StudyMode
import io.github.oungy.wordnest.data.local.study.StudyRepository
import io.github.oungy.wordnest.domain.Rating
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class ExerciseSession(
    val mode: StudyMode,
    val day: LocalDate,
    val reviewTotal: Int = 0,
    val reviewed: Int = 0,
)

internal data class StudyUiState(
    val dashboard: StudyDashboard? = null,
    val session: ExerciseSession? = null,
    val busy: Boolean = false,
    val error: StudyError? = null,
)

internal enum class StudyError { Load, Save, DayChanged }

internal class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = StudyDatabase.open(application)
    private val repository = StudyRepository(database)
    private val mutableState = MutableStateFlow(StudyUiState())
    val state = mutableState.asStateFlow()

    fun refresh() = runAction(StudyError.Load) {
        publish(repository.dashboard(LocalDate.now()))
    }

    fun refreshIfDateChanged() {
        val day = state.value.dashboard?.day ?: return
        if (day != LocalDate.now()) refresh()
    }

    fun selectPacks(ids: Set<String>) = runAction(StudyError.Save) {
        repository.selectPacks(ids)
        publish(repository.dashboard(LocalDate.now()))
    }

    fun setDailyLimit(limit: Int) = runAction(StudyError.Save) {
        repository.setDailyLimit(limit)
        publish(repository.dashboard(LocalDate.now()))
    }

    fun start(mode: StudyMode) = runAction(StudyError.Load) {
        val day = LocalDate.now()
        val dashboard = when (mode) {
            StudyMode.New -> repository.ensurePlan(day)
            StudyMode.Review -> repository.dashboard(day)
        }
        publish(dashboard)
        val available = when (mode) {
            StudyMode.New -> dashboard.nextNewWord != null
            StudyMode.Review -> dashboard.dueWords.isNotEmpty()
        }
        if (available) {
            val reviewTotal = if (mode == StudyMode.Review) dashboard.dueWords.size else 0
            mutableState.update { it.copy(session = ExerciseSession(mode, day, reviewTotal)) }
        }
    }

    fun rate(wordId: String, rating: Rating) = runAction(StudyError.Save) {
        val session = state.value.session ?: return@runAction
        val day = LocalDate.now()
        if (day != session.day) {
            publish(repository.dashboard(day))
            return@runAction
        }
        val result = repository.rate(day, session.mode, wordId, rating)
        mutableState.update {
            val reviewed = if (result.accepted && session.mode == StudyMode.Review && it.session == session) {
                session.copy(reviewed = session.reviewed + 1)
            } else {
                it.session
            }
            it.copy(dashboard = result.dashboard, session = reviewed)
        }
    }

    fun leaveSession() {
        mutableState.update { it.copy(session = null, error = null) }
    }

    private fun publish(dashboard: StudyDashboard) {
        mutableState.update {
            if (it.session != null && it.session.day != dashboard.day) {
                it.copy(dashboard = dashboard, session = null, error = StudyError.DayChanged)
            } else {
                it.copy(dashboard = dashboard)
            }
        }
    }

    private fun runAction(error: StudyError, action: suspend () -> Unit) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                Log.e("WordNestStudy", "Study operation failed", failure)
                mutableState.update { it.copy(error = error) }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }

    override fun onCleared() {
        database.close()
    }
}
