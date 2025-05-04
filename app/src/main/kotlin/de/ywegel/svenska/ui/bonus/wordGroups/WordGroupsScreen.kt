package de.ywegel.svenska.ui.bonus.wordGroups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.NavigationBarSpacer
import de.ywegel.svenska.ui.common.TabsScaffold
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination<RootGraph>
@Composable
fun WordGroupsScreen(navigator: DestinationsNavigator) {
    WordGroupsScreen(onNavigateUp = navigator::navigateUp)
}

@Composable
private fun WordGroupsScreen(onNavigateUp: () -> Unit) {
    TabsScaffold(
        toolbarTitle = R.string.word_groups_title,
        tabTitleResources = listOf(R.string.nouns_tab, R.string.verbs_tab),
        pages = listOf(
            { NounGroupsScreen() },
            { VerbGroupsScreen() },
        ),
        onNavigateUp = onNavigateUp,
    )
}

@Composable
fun VerbGroupsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacings.m),
        verticalArrangement = Arrangement.spacedBy(Spacings.m),
    ) {
        Text(
            text = stringResource(R.string.verbs_title),
            style = SvenskaTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        // Group 1
        GrammarItem(
            group = stringResource(R.string.group_1),
            rule = stringResource(R.string.group_1_rule),
            example = stringResource(R.string.group_1_example),
        )

        // Group 2a
        GrammarItem(
            group = stringResource(R.string.group_2a),
            rule = stringResource(R.string.group_2a_rule),
            example = stringResource(R.string.group_2a_example),
        )

        // Group 2b
        GrammarItem(
            group = stringResource(R.string.group_2b),
            rule = stringResource(R.string.group_2b_rule),
            example = stringResource(R.string.group_2b_example),
        )

        // Group 3
        GrammarItem(
            group = stringResource(R.string.group_3),
            rule = stringResource(R.string.group_3_rule),
            example = stringResource(R.string.group_3_example),
        )

        // Group 4 (Special)
        GrammarItem(
            group = stringResource(R.string.group_4),
            rule = stringResource(R.string.group_4_rule),
            example = stringResource(R.string.group_4_example),
        )

        NavigationBarSpacer()
    }
}

@Composable
fun NounGroupsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacings.m),
        verticalArrangement = Arrangement.spacedBy(Spacings.m),
    ) {
        Text(
            text = stringResource(R.string.nouns_title),
            style = SvenskaTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        // Group 1 (-or)
        GrammarItem(
            group = stringResource(R.string.group_or),
            rule = stringResource(R.string.group_or_rule),
            example = stringResource(R.string.group_or_example),
        )

        // Group 2 (-ar)
        GrammarItem(
            group = stringResource(R.string.group_ar),
            rule = stringResource(R.string.group_ar_rule),
            example = stringResource(R.string.group_ar_example),
        )

        // Group 3 (-er)
        GrammarItem(
            group = stringResource(R.string.group_er),
            rule = stringResource(R.string.group_er_rule),
            example = stringResource(R.string.group_er_example),
        )

        // Group 4 (-r)
        GrammarItem(
            group = stringResource(R.string.group_r),
            rule = stringResource(R.string.group_r_rule),
            example = stringResource(R.string.group_r_example),
        )

        // Group 5 (-n)
        GrammarItem(
            group = stringResource(R.string.group_n),
            rule = stringResource(R.string.group_n_rule),
            example = stringResource(R.string.group_n_example),
        )

        // Group 6 (-)
        GrammarItem(
            group = stringResource(R.string.group_unchanged),
            rule = stringResource(R.string.group_unchanged_rule),
            example = stringResource(R.string.group_unchanged_example),
        )

        NavigationBarSpacer()
    }
}

@Composable
fun GrammarItem(group: String, rule: String, example: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacings.xxs),
    ) {
        Column(
            modifier = Modifier.padding(Spacings.m),
            verticalArrangement = Arrangement.spacedBy(Spacings.xs),
        ) {
            Text(
                text = group,
                style = SvenskaTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = rule,
                style = SvenskaTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.example_format, example),
                style = SvenskaTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
            )
        }
    }
}
