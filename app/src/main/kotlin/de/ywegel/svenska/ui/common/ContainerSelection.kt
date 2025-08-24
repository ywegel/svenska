package de.ywegel.svenska.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.model.containers
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun ContainerSelection(
    containers: List<VocabularyContainer>,
    selectedContainer: VocabularyContainer?,
    onContainerSelected: (VocabularyContainer) -> Unit,
) {
    Column {
        containers.forEach {
            ContainerItem(it, selectedContainer, onContainerSelected)
        }
    }
}

@Composable
private fun ContainerItem(
    container: VocabularyContainer,
    selectedContainer: VocabularyContainer?,
    onClick: (VocabularyContainer) -> Unit,
) {
    val isSelected = container == selectedContainer

    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick(container) }
            .padding(horizontal = Spacings.l, vertical = Spacings.s)
            // necessary, to avoid larger height for selected rows
            .defaultMinSize(minHeight = ICON_ROW_MINIMUM_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(modifier = Modifier.weight(1f), text = container.name)
        HorizontalSpacerM()
        if (isSelected) {
            Icon(imageVector = SvenskaIcons.Done, contentDescription = null)
        }
    }
}

private val ICON_ROW_MINIMUM_HEIGHT = 24.dp

@Preview
@Composable
private fun ContainerSelectionPreview() {
    SvenskaTheme {
        ContainerSelection(
            containers = containers(),
            selectedContainer = containers()[0],
            onContainerSelected = {},
        )
    }
}
