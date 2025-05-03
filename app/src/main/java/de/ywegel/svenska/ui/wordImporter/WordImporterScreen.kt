package de.ywegel.svenska.ui.wordImporter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.ui.common.VerticalSpacerM

@Destination(
    deepLinks = [
        DeepLink(
            action = Intent.ACTION_VIEW,
            mimeType = "application/json",
        ),
    ],
)
@Composable
fun WordImporterScreen(navigator: DestinationsNavigator) {
    val viewModel: WordImporterViewModel = hiltViewModel()
    val importerState by viewModel.importerState.collectAsStateWithLifecycle()
    val loading by viewModel.isLoading.collectAsStateWithLifecycle()

    HandleJsonImportFromIntent { uri ->
        viewModel.onFilePicked(uri)
    }

    WordImporterScreen(
        importerState = importerState,
        loadingState = loading,
        onNavigateUp = navigator::navigateUp,
        onFilePicked = viewModel::onFilePicked,
        saveWords = viewModel::saveWords,
        onRestartClicked = viewModel::onRestartClicked,
    )
}

@Composable
private fun WordImporterScreen(
    importerState: ImporterState,
    loadingState: Boolean,
    onNavigateUp: () -> Unit,
    onFilePicked: (uri: Uri) -> Unit,
    saveWords: () -> Unit,
    onRestartClicked: () -> Unit,
) {
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onFilePicked(it) }
    }

    Scaffold { padding ->
        Box(Modifier.padding(padding)) {
            if (loadingState) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    importerState.loadingText()?.let {
                        Text(it)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (importerState) {
                        ImporterState.Idle -> IdleScreen { filePickerLauncher.launch("*/*") }
                        is ImporterState.Parsed -> ParsedScreen(importerState, saveWords)
                        is ImporterState.Importing -> ImportingScreen(importerState)
                        is ImporterState.Finished -> FinishedScreen(importerState, onRestartClicked, onNavigateUp)
                    }
                }
            }
        }
    }
}

@Composable
private fun IdleScreen(launchFilePicker: () -> Unit) {
    Text("Import Words from a File")
    VerticalSpacerM()
    Button(onClick = launchFilePicker) {
        Text("Select File")
    }
}

@Composable
private fun ParsedScreen(state: ImporterState.Parsed, onProceed: () -> Unit) {
    Text("Found ${state.words} words in ${state.chapters} chapters.")
    VerticalSpacerM()
    Button(onClick = onProceed) {
        Text("Proceed")
    }
}

@Composable
fun ImportingScreen(state: ImporterState.Importing) {
    LinearProgressIndicator(
        progress = { state.progress },
        modifier = Modifier.fillMaxWidth(),
    )
    VerticalSpacerM()
    Text("Importing words...")
}

@Composable
fun FinishedScreen(state: ImporterState.Finished, onRestartClicked: () -> Unit, onNavigateUp: () -> Unit) {
    state.error?.let {
        Text("An error occurred: $it")
    } ?: Text("${state.wordCount} words imported!")
    VerticalSpacerM()
    Button(onRestartClicked) {
        Text(text = state.error?.let { "Retry" } ?: "Import again")
    }
    OutlinedButton(onNavigateUp) {
        Text(text = "Exit")
    }
}

@ReadOnlyComposable
private fun ImporterState.loadingText(): String? {
    return when (this) {
        ImporterState.Idle -> "Parsing file..."
        else -> null
    }
}

@Composable
fun HandleJsonImportFromIntent(onJsonFileUriReceived: (Uri) -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uri = remember { activity?.intent?.data }
    val alreadyHandled = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uri) {
        if (!alreadyHandled.value && uri != null) {
            alreadyHandled.value = true
            onJsonFileUriReceived(uri)
        }
    }
}
