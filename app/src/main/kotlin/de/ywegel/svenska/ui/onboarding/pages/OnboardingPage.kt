package de.ywegel.svenska.ui.onboarding.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

/**
 * Enum representing onboarding pages
 */
enum class OnboardingPage {
    INTRODUCTION,
    CONTAINERS,
    BONUS,
    WORD_GROUP,
    QUIZ,
    IMPORTER,
    ;

    companion object {
        val entries = setOf(INTRODUCTION, CONTAINERS, BONUS, WORD_GROUP, QUIZ, IMPORTER)

        val COUNT = entries.size

        val LAST_INDEX = COUNT - 1

        fun fromIndex(index: Int): OnboardingPage = entries.elementAtOrElse(index) { INTRODUCTION }
    }

    @ReadOnlyComposable
    @Composable
    fun getTitle(): String {
        val id = when (this) {
            INTRODUCTION -> R.string.onboarding_page_title_introduction
            CONTAINERS -> R.string.onboarding_page_title_container
            BONUS -> R.string.onboarding_page_title_bonus_material
            WORD_GROUP -> R.string.onboarding_page_title_word_groups
            QUIZ -> R.string.onboarding_page_title_quiz
            IMPORTER -> R.string.onboarding_page_importer_title
        }
        return stringResource(id)
    }

    @ReadOnlyComposable
    @Composable
    fun getContentText(): String {
        val id = when (this) {
            INTRODUCTION -> R.string.onboarding_page_content_introduction
            CONTAINERS -> R.string.onboarding_page_content_container
            BONUS -> R.string.onboarding_page_content_bonus_material
            WORD_GROUP -> R.string.onboarding_page_content_word_groups_description
            QUIZ -> R.string.onboarding_page_content_quiz
            IMPORTER -> R.string.onboarding_page_content_importer
        }
        return stringResource(id)
    }
}

@Composable
internal fun OnboardingPage(page: OnboardingPage, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacings.l),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = page.getTitle(),
            style = SvenskaTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        VerticalSpacerM()

        content()
    }
}
