package de.ywegel.svenska.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import de.ywegel.svenska.ui.theme.Spacings

/**
 * A fixed ModalBottomSheet, that the user can't dismiss by himself. It has no draghandle and must be closed
 * programmatically
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedModalBottomSheet(
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        containerColor = containerColor,
        onDismissRequest = { /* Modal should not be hideable */ },
        dragHandle = {},
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false), // Modal should not be hideable
        sheetState = rememberModalBottomSheetState(
            confirmValueChange = {
                // Modal should not be hideable
                false
            },
        ),
    ) {
        Column(
            Modifier
                .padding(vertical = Spacings.s)
                .navigationBarsPadding(),
        ) {
            VerticalSpacerS()
            content()
        }
    }
}
