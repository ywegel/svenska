package de.ywegel.svenska.ui.privacy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import de.ywegel.svenska.ConsentStep
import de.ywegel.svenska.ui.common.FixedModalBottomSheet
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun PrivacyConsentSheet(step: ConsentStep, viewModel: PrivacyConsentViewModel = hiltViewModel()) {
    PrivacyConsentSheet(step = step, callbacks = viewModel)
}

@Composable
private fun PrivacyConsentSheet(step: ConsentStep, callbacks: PrivacyConsentCallbacks) {
    FixedModalBottomSheet {
        Column(
            modifier = Modifier
                .padding(horizontal = Spacings.xl),
        ) {
            PrivacyProgressIndicator(activeStepIndex = if (step == ConsentStep.Policy) 0 else 1)
            VerticalSpacerM()
            when (step) {
                ConsentStep.Policy -> PrivacyPolicyStep(onContinueClicked = callbacks::onPolicyAcknowledged)
                ConsentStep.CrashReporting -> PrivacyCrashReportingStep(
                    onDecision = callbacks::onCrashReportingDecision,
                )
                ConsentStep.Done -> Unit
            }
        }
    }
}

@Preview
@Composable
private fun PrivacyConsentSheetPolicyPreview() {
    SvenskaTheme {
        PrivacyConsentSheet(step = ConsentStep.Policy, callbacks = PrivacyConsentCallbacksFake)
    }
}

@Preview
@Composable
private fun PrivacyConsentSheetCrashReportingPreview() {
    SvenskaTheme {
        PrivacyConsentSheet(step = ConsentStep.CrashReporting, callbacks = PrivacyConsentCallbacksFake)
    }
}
