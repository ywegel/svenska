package de.ywegel.svenska.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ConfirmableComponent(
    dialogTitle: String,
    dialogText: String,
    onConfirm: () -> Unit,
    clickableComponent: @Composable (onClick: () -> Unit) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    clickableComponent {
        showDialog = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(dialogTitle) },
            text = { Text(dialogText) },
            confirmButton = {
                ConfirmButton {
                    showDialog = false
                    onConfirm()
                }
            },
            dismissButton = {
                DismissButton { showDialog = false }
            },
        )
    }
}
