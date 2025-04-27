package de.ywegel.svenska.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Abc
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.data.GeneratorConstants
import de.ywegel.svenska.ui.common.HorizontalSpacerM
import de.ywegel.svenska.ui.common.SwitchWithText
import de.ywegel.svenska.ui.common.TopAppTextBar
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.destinations.WordImporterScreenDestination
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination
@Composable
fun SettingsScreen(navigator: DestinationsNavigator, viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState,
        viewModel::toggleShowCompactVocabularyItem,
        navigateUp = navigator::navigateUp,
        navigateToWordImporter = { navigator.navigate(WordImporterScreenDestination) },
    )
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    onShowCompactVocabularyItemChanged: (Boolean) -> Unit,
    navigateUp: () -> Unit,
    navigateToWordImporter: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppTextBar(
                title = stringResource(R.string.settings_title),
                onNavigateUp = navigateUp,
                navigationIcon = Icons.AutoMirrored.Default.ArrowBack,
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(horizontal = Spacings.s),
        ) {
            SwitchWithText(
                text = stringResource(R.string.settings_compact_vocabulary_item),
                switchChecked = uiState.showCompactVocabularyItem,
                onSwitchChanged = onShowCompactVocabularyItemChanged,
            )

            VerticalSpacerM()

            SettingsButton(
                buttonText = stringResource(R.string.settings_naviagate_word_importer_screen),
                icon = SvenskaIcons.Abc,
                onClick = navigateToWordImporter,
            )
        }
    }
}

@Composable
private fun SettingsButton(buttonText: String, icon: ImageVector? = null, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null)
        } else {
            Spacer(DefaultIconSizeModifier)
        }

        HorizontalSpacerM()

        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = buttonText, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// Default from IconButtonTokens
private val DefaultIconSizeModifier = Modifier.size(24.0.dp)

@Preview
@Composable
private fun SettingsButtonPreview(
    @PreviewParameter(SettingsButtonPreviewProvider::class) data: SettingsButtonPreviewData,
) {
    SvenskaTheme {
        SettingsButton(
            buttonText = data.buttonText,
            icon = data.imageVector,
        ) { }
    }
}

private class SettingsButtonPreviewProvider : PreviewParameterProvider<SettingsButtonPreviewData> {
    override val values: Sequence<SettingsButtonPreviewData> = listOf(
        SettingsButtonPreviewData(
            buttonText = "Short text",
            imageVector = null,
        ),
        SettingsButtonPreviewData(
            buttonText = "Long text: ${GeneratorConstants.LONG_STRING}",
            imageVector = null,
        ),
        SettingsButtonPreviewData(
            buttonText = "Short text",
            imageVector = SvenskaIcons.Info,
        ),
        SettingsButtonPreviewData(
            buttonText = "Long text: ${GeneratorConstants.LONG_STRING}",
            imageVector = SvenskaIcons.Info,
        ),
    ).asSequence()
}

private data class SettingsButtonPreviewData(
    val buttonText: String,
    val imageVector: ImageVector?,
)
