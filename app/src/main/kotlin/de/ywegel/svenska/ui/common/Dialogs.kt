package de.ywegel.svenska.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun ConfirmButton(text: String = stringResource(R.string.general_ok), enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled) { Text(text = text) }
}

@Composable
fun DismissButton(text: String = stringResource(R.string.general_cancel), onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) { Text(text = text) }
}

@Composable
fun SimpleDialog(
    onDismiss: () -> Unit,
    title: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Dialog(onDismiss) {
        Surface(shape = SvenskaTheme.shapes.medium, modifier = modifier) {
            Column {
                Column(Modifier.padding(Spacings.xl)) {
                    title.invoke()
                    Spacer(Modifier.size(Spacings.m))
                    content.invoke()
                }
                Spacer(Modifier.size(Spacings.xxs))
                Row(
                    modifier = Modifier
                        .padding(Spacings.xs)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacings.xs, Alignment.End),
                ) {
                    dismissButton?.invoke()
                    confirmButton.invoke()
                }
            }
        }
    }
}
