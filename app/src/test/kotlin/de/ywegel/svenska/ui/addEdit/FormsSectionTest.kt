package de.ywegel.svenska.ui.addEdit

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.theme.SvenskaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Only the wiring of the hint, AddEditUiStateTest covers when there are too many endings
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.TARGET_SDK])
class FormsSectionTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val tooManyEndingsHint
        get() = composeRule.activity.resources.getQuantityString(R.plurals.addEdit_forms_too_many_endings, 3, 3)

    @Test
    fun `too many endings show a hint under the forms`() {
        // When
        setContent(nounPenna(endings = "-n -or -orna -x"))

        // Then
        composeRule.onNodeWithText(tooManyEndingsHint).assertExists()
    }

    @Test
    fun `as many endings as forms show no hint`() {
        // When
        setContent(nounPenna(endings = "-n -or -orna"))

        // Then
        composeRule.onNodeWithText(tooManyEndingsHint).assertDoesNotExist()
    }

    @Test
    fun `fewer endings than forms show no hint`() {
        // When
        setContent(nounPenna(endings = "-n -or"))

        // Then
        composeRule.onNodeWithText(tooManyEndingsHint).assertDoesNotExist()
    }

    private fun nounPenna(endings: String) = AddEditUiState(
        selectedWordGroup = ViewWordGroup.Noun,
        wordWithAnnotation = "penna",
        endings = endings,
    )

    private fun setContent(uiState: AddEditUiState) {
        composeRule.setContent {
            SvenskaTheme {
                FormsSection(uiState = uiState, callbacks = AddEditVocabularyCallbacksFake)
            }
        }
    }
}
