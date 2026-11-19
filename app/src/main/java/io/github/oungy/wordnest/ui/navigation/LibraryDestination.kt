package io.github.oungy.wordnest.ui.navigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.data.local.study.LibrarySnapshot
import io.github.oungy.wordnest.ui.components.StudyButton
import io.github.oungy.wordnest.ui.learning.StudyError
import io.github.oungy.wordnest.ui.learning.StudyStatus
import io.github.oungy.wordnest.ui.learning.StudyViewModel
import io.github.oungy.wordnest.ui.library.LibraryScreen
import kotlinx.coroutines.CancellationException

@Composable
internal fun LibraryDestination(
    modifier: Modifier,
    study: StudyViewModel,
    useGlass: Boolean,
    onBack: () -> Unit,
    onSheetVisible: (Boolean) -> Unit,
) {
    var snapshot by remember { mutableStateOf<LibrarySnapshot?>(null) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(study, attempt) {
        failed = false
        try {
            snapshot = study.readLibrary()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e("WordNestLibrary", "Could not load library", error)
            failed = true
        }
    }
    val current = snapshot
    if (current == null) {
        BackHandler(onBack = onBack)
        Column(modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (failed) {
                StudyStatus(StudyError.Load)
                StudyButton(stringResource(R.string.try_again), { attempt++ })
            } else {
                Text(stringResource(R.string.study_loading))
            }
        }
    } else {
        LibraryScreen(modifier, current, useGlass, onBack, onSheetVisible, study::submitLibraryEdit) { id ->
            try {
                val updated = study.awaitLibraryEdit(id)
                if (updated != null) snapshot = updated
                updated != null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e("WordNestLibrary", "Could not save library change", error)
                false
            }
        }
    }
}
