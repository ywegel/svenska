package de.ywegel.svenska.screenshots

import com.github.takahirom.roborazzi.AndroidComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester.TestParameter.JUnit4TestParameter.AndroidPreviewJUnit4TestParameter
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi

// Infinitely looping animation previews only ever capture an empty first frame
private val excludedPreviews = setOf(
    "de.ywegel.svenska.navigation.transitions.LateralTransitionKt.LateralTransitionPreview",
    "de.ywegel.svenska.navigation.transitions.TemporaryHierarchicalTransitionStyleKt.HierarchicalTransitionPreview",
)

@OptIn(ExperimentalRoborazziApi::class)
class SvenskaComposePreviewTester(
    private val delegate: AndroidComposePreviewTester = AndroidComposePreviewTester(),
) : ComposePreviewTester<AndroidPreviewJUnit4TestParameter> by delegate {

    override fun testParameters(): List<AndroidPreviewJUnit4TestParameter> = delegate.testParameters().filterNot {
        "${it.preview.declaringClass}.${it.preview.methodName}" in excludedPreviews
    }
}
