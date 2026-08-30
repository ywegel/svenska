@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.settings.subscreens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.SharedUrlConstants
import de.ywegel.svenska.navigation.SettingsNavGraph
import de.ywegel.svenska.ui.common.ClickableText
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.common.SwitchWithText
import de.ywegel.svenska.ui.common.TopAppTextBar
import de.ywegel.svenska.ui.common.VerticalSpacerXS
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.common.VerticalSpacerXXXS
import de.ywegel.svenska.ui.common.rememberColumnScaffoldInsets
import de.ywegel.svenska.ui.privacy.PrivacyDisclosureCard
import de.ywegel.svenska.ui.settings.SettingsCallbacks
import de.ywegel.svenska.ui.settings.SettingsCallbacksFake
import de.ywegel.svenska.ui.settings.SettingsUiState
import de.ywegel.svenska.ui.settings.SettingsViewModel
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme
import java.text.DateFormat
import java.util.Date

@Destination<SettingsNavGraph>
@Composable
fun PrivacySettingsScreen(navigator: DestinationsNavigator, viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PrivacySettingsScreen(
        uiState = uiState,
        callbacks = viewModel,
        navigateUp = navigator::navigateUp,
    )
}

@Composable
private fun PrivacySettingsScreen(
    uiState: SettingsUiState,
    callbacks: SettingsCallbacks,
    navigateUp: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppTextBar(
                title = stringResource(R.string.settings_privacy_title),
                onNavigateUp = navigateUp,
                navigationIcon = Icons.AutoMirrored.Default.ArrowBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val paddings = rememberColumnScaffoldInsets(innerPadding)

        Column(
            Modifier
                .padding(paddings)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.settings_privacy_diagnostics_section),
                style = SvenskaTheme.typography.labelLarge,
                color = SvenskaTheme.colors.primary,
                modifier = Modifier.padding(horizontal = Spacings.m, vertical = Spacings.s),
            )

            SwitchWithText(
                title = stringResource(R.string.settings_crash_reporting_title),
                description = stringResource(R.string.settings_crash_reporting_description),
                checked = uiState.crashReportingEnabled,
                onCheckedChange = callbacks::updateCrashReportingEnabled,
            )

            VerticalSpacerXXXS()

            Text(
                text = consentCaption(
                    timestamp = uiState.crashReportingConsentTimestamp,
                    enabled = uiState.crashReportingEnabled,
                ),
                style = SvenskaTheme.typography.bodySmall,
                color = SvenskaTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacings.m),
            )

            ExpandablePrivacyDisclosureCard()

            HorizontalDivider()
            VerticalSpacerXXS()

            ClickableText(
                title = stringResource(R.string.settings_navigate_privacy_policy_title),
                onClick = { uriHandler.openUri(SharedUrlConstants.SVENSKA_PRIVACY_POLICY) },
            )
        }
    }
}

@Composable
private fun ExpandablePrivacyDisclosureCard(modifier: Modifier = Modifier, initialExpansionState: Boolean = false) {
    var expanded by remember { mutableStateOf(initialExpansionState) }

    Column(modifier.padding(vertical = Spacings.s)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(horizontal = Spacings.m),
        ) {
            Text(
                text = stringResource(R.string.settings_privacy_what_data_is_collected_title),
                modifier = Modifier.weight(1f),
            )
            IconButton(
                icon = if (expanded) SvenskaIcons.ArrowDropUp else SvenskaIcons.ArrowDropDown,
                contentDescription = stringResource(
                    R.string.settings_privacy_what_data_is_collected_expand_content_description,
                ),
            ) {
                expanded = !expanded
            }
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(horizontal = Spacings.m)) {
                VerticalSpacerXS()
                PrivacyDisclosureCard()
                VerticalSpacerXS()
                PrivacyPolicyReferenceText()
            }
        }
    }
}

@Composable
private fun PrivacyPolicyReferenceText(modifier: Modifier = Modifier) {
    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = SvenskaTheme.colors.primary,
            textDecoration = TextDecoration.Underline,
        ),
    )

    Text(
        modifier = modifier.padding(horizontal = Spacings.xxs),
        text = buildAnnotatedString {
            append(stringResource(R.string.settings_privacy_disclosure_policy_prefix))
            withLink(LinkAnnotation.Url(SharedUrlConstants.SVENSKA_PRIVACY_POLICY, linkStyles)) {
                append(stringResource(R.string.settings_navigate_privacy_policy_title))
            }
            append(stringResource(R.string.settings_privacy_disclosure_policy_suffix))
        },
        style = SvenskaTheme.typography.bodySmall,
        color = SvenskaTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun consentCaption(timestamp: Long?, enabled: Boolean): String {
    if (timestamp == null) return stringResource(R.string.settings_privacy_consent_undecided)
    val formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))
    val template = if (enabled) {
        R.string.settings_privacy_consent_enabled
    } else {
        R.string.settings_privacy_consent_disabled
    }
    return stringResource(template, formattedDate)
}

@Preview
@Composable
private fun PrivacySettingsScreenEnabledPreview() {
    SvenskaTheme {
        PrivacySettingsScreen(
            uiState = SettingsUiState(
                crashReportingEnabled = true,
                crashReportingConsentTimestamp = 1_756_339_200_000,
            ),
            callbacks = SettingsCallbacksFake,
        )
    }
}

@Preview
@Composable
private fun PrivacySettingsScreenDisabledPreview() {
    SvenskaTheme {
        PrivacySettingsScreen(
            uiState = SettingsUiState(
                crashReportingEnabled = false,
                crashReportingConsentTimestamp = 1_756_339_200_000,
            ),
            callbacks = SettingsCallbacksFake,
        )
    }
}

@Preview
@Composable
private fun ExpandablePrivacyDisclosureCardPreview() {
    SvenskaTheme {
        ExpandablePrivacyDisclosureCard(initialExpansionState = false)
    }
}

@Preview
@Composable
private fun ExpandablePrivacyDisclosureCardExpandedPreview() {
    SvenskaTheme {
        ExpandablePrivacyDisclosureCard(initialExpansionState = true)
    }
}
