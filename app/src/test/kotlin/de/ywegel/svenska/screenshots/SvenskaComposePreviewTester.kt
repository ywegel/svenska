package de.ywegel.svenska.screenshots

import com.github.takahirom.roborazzi.AndroidComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester.TestParameter.JUnit4TestParameter.AndroidPreviewJUnit4TestParameter
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import java.io.File

// Infinitely looping animation previews only ever capture an empty first frame
private val excludedPreviews = setOf(
    "de.ywegel.svenska.navigation.transitions.LateralTransitionKt.LateralTransitionPreview",
    "de.ywegel.svenska.navigation.transitions.TemporaryHierarchicalTransitionStyleKt.HierarchicalTransitionPreview",
)

private val unsafeFileNameChars = Regex("[^a-zA-Z0-9_.-]")

@OptIn(ExperimentalRoborazziApi::class)
class SvenskaComposePreviewTester(
    private val delegate: AndroidComposePreviewTester = AndroidComposePreviewTester(capturer = SafeFileNameCapturer()),
) : ComposePreviewTester<AndroidPreviewJUnit4TestParameter> by delegate {

    override fun testParameters(): List<AndroidPreviewJUnit4TestParameter> = delegate.testParameters().filterNot {
        "${it.preview.declaringClass}.${it.preview.methodName}" in excludedPreviews
    }
}

@OptIn(ExperimentalRoborazziApi::class)
private class SafeFileNameCapturer(
    private val defaultCapturer: AndroidComposePreviewTester.Capturer = AndroidComposePreviewTester.DefaultCapturer(),
) : AndroidComposePreviewTester.Capturer {

    override fun capture(parameter: AndroidComposePreviewTester.CaptureParameter) {
        val file = File(parameter.filePath)
        val safeFile = File(file.parent, file.name.replace(unsafeFileNameChars, "_"))
        defaultCapturer.capture(parameter.copy(filePath = safeFile.path))
    }
}
