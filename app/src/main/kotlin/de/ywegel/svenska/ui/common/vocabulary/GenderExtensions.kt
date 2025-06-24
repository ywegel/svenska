package de.ywegel.svenska.ui.common.vocabulary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Gender

@ReadOnlyComposable
@Composable
fun Gender.abbreviation(): String {
    return when (this) {
        Gender.Ultra -> stringResource(R.string.lang_noun_gender_abbreviation_ultra)
        Gender.Neutra -> stringResource(R.string.lang_noun_gender_abbreviation_neutra)
    }
}
