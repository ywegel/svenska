package de.ywegel.svenska.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
internal fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.onboarding_page_indicator, currentPage + 1, pageCount),
            style = SvenskaTheme.typography.bodyMedium,
        )
    }
}
