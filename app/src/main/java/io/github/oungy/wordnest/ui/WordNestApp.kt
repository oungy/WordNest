package io.github.oungy.wordnest.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.oungy.wordnest.R
import io.github.oungy.wordnest.ui.theme.WordNestTheme

@Composable
fun WordNestApp() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.welcome_message),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.development_notice),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun WordNestLightPreview() {
    WordNestTheme(darkTheme = false) { WordNestApp() }
}

@Preview(showBackground = true, widthDp = 320, fontScale = 1.5f)
@Composable
private fun WordNestDarkPreview() {
    WordNestTheme(darkTheme = true) { WordNestApp() }
}
