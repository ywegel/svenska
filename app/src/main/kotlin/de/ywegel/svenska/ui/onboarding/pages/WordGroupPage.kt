package de.ywegel.svenska.ui.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.ui.common.HorizontalSpacerS
import de.ywegel.svenska.ui.common.vocabulary.mainGroupAbbreviation
import de.ywegel.svenska.ui.common.vocabulary.subGroupAbbreviation
import de.ywegel.svenska.ui.common.vocabulary.wordGroupBadge.AnimatedWordGroupBadgeExtended
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme
import kotlinx.coroutines.delay

@Composable
fun WordGroupPage(navigateToWordGroupScreen: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacings.m),
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(R.string.onboarding_page_content_word_groups_description),
            style = SvenskaTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.onboarding_page_content_word_groups_description_extended),
            style = SvenskaTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        AnimatedBadgeExplanationRow()
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Button(navigateToWordGroupScreen) {
                Text(stringResource(R.string.onboarding_page_content_word_groups_find_out_more))
            }
        }
        Text(
            text = stringResource(R.string.onboarding_page_content_word_groups_or),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = SvenskaTheme.typography.labelMedium,
        )
    }
}

@Composable
fun AnimatedBadgeExplanationRow(letterAnimationDurationMillis: Long = 500, letterRestingDurationMillis: Long = 500) {
    val shuffleList = remember {
        buildList {
            for (noun in (WordGroup.NounSubgroup.entries - WordGroup.NounSubgroup.UNCHANGED_ETT)) {
                add(WordGroup.Noun(noun))
            }
            for (noun in (WordGroup.VerbSubgroup.entries - WordGroup.VerbSubgroup.GROUP_2B)) {
                add(WordGroup.Verb(noun))
            }
            add(WordGroup.Adjective)
            add(WordGroup.Other)
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentGroup = shuffleList[currentIndex]

    LaunchedEffect(Unit) {
        while (true) {
            delay(letterAnimationDurationMillis)
            delay(letterRestingDurationMillis)
            currentIndex = (currentIndex + 1) % shuffleList.size
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedWordGroupBadgeExtended(currentGroup.mainGroupAbbreviation(), currentGroup.subGroupAbbreviation())
        HorizontalSpacerS()
        Text(stringResource(R.string.onboarding_page_content_word_groups_badge_text))
    }
}
