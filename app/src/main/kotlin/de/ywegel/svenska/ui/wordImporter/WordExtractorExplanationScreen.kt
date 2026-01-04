@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.wordImporter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.WordImporterScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.navigation.SvenskaGraph
import de.ywegel.svenska.ui.common.TopAppTextBar
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.VerticalSpacerXS
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination<SvenskaGraph>
@Composable
fun WordExtractorExplanationScreen(navigator: DestinationsNavigator) {
    WordExtractorExplanationScreen(
        navigateUp = navigator::navigateUp,
        navigateToWordImporterScreen = { navigator.navigate(WordImporterScreenDestination) },
    )
}

@Composable
private fun WordExtractorExplanationScreen(navigateUp: () -> Unit, navigateToWordImporterScreen: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppTextBar(
                title = stringResource(R.string.wordExtractor_title),
                onNavigateUp = navigateUp,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(Spacings.m),
            contentPadding = PaddingValues(bottom = Spacings.m, start = Spacings.xs, end = Spacings.m),
        ) {
            item {
                Body(R.string.wordExtractor_currently_only_rivstart)
            }
            item {
                Title(R.string.wordExtractor_important_legal_note)
                Body(R.string.wordExtractor_legal_copyright)
                VerticalSpacerM()
                Body(R.string.wordExtractor_legal_disclaimer)
            }

            item {
                Title(R.string.wordExtractor_prerequisites)
                BulletPointList {
                    BulletPointWithLink(
                        prefix = stringResource(R.string.wordExtractor_prereq_python),
                        linkText = stringResource(R.string.wordExtractor_prereq_python_link),
                        url = LinkUrls.PYTHON,
                    )
                    BulletPointWithLink(
                        prefix = stringResource(R.string.wordExtractor_prereq_rivstart),
                        linkText = stringResource(R.string.wordExtractor_prereq_rivstart_link),
                        url = LinkUrls.RIVSTART,
                    )
                    BulletPointText(stringResource(R.string.wordExtractor_prereq_scripts))
                }
            }

            item {
                Title(R.string.wordExtractor_step1_title)
                Body(R.string.wordExtractor_step1_pymupdf)
                CodeBlock("pip install pymupdf")

                Body(R.string.wordExtractor_step1_alternative_uv)
                BulletPointList {
                    BulletPointWithLink(
                        prefix = stringResource(R.string.wordExtractor_step1_uv_prefix),
                        linkText = stringResource(R.string.wordExtractor_step1_uv_link),
                        url = LinkUrls.UV,
                        suffix = stringResource(R.string.wordExtractor_step1_uv_suffix),
                    )
                }
                CodeBlock("uv run rivstart_second_edition_wordlist_extractor.py")
            }

            item {
                Title(R.string.wordExtractor_step2_title)
                Body(R.string.wordExtractor_step2_supported_books)
                BulletPointList {
                    BulletPointText(stringResource(R.string.wordExtractor_step2_book_a1a2))
                    BulletPointText(stringResource(R.string.wordExtractor_step2_book_b1b2))
                }
                Body(R.string.wordExtractor_step2_download_scripts)
                BulletPointList {
                    BulletPointWithLink(
                        prefix = stringResource(R.string.wordExtractor_step2_repo_prefix),
                        linkText = stringResource(R.string.wordExtractor_step2_repo_link),
                        url = LinkUrls.SVENSKA_REPOSITORY,
                    )
                    BulletPointText(stringResource(R.string.wordExtractor_step2_download_py_files))
                }
                Body(R.string.wordExtractor_step2_place_pdf)
                BulletPointList {
                    BulletPointText(stringResource(R.string.wordExtractor_step2_edition_2nd))
                    CodeBlock("python3 rivstart_second_edition_wordlist_extractor.py")
                    BulletPointText(stringResource(R.string.wordExtractor_step2_edition_3rd))
                    CodeBlock("python3 rivstart_third_edition_wordlist_extractor.py")
                }
                Body(R.string.wordExtractor_step2_prompt)
            }
            item {
                Title(R.string.wordExtractor_step3_title)
                Body(R.string.wordExtractor_step3_transfer)
                VerticalSpacerXS()
                Button(onClick = navigateToWordImporterScreen, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.wordExtractor_step3_button))
                }
            }
        }
    }
}

@Composable
private fun Title(titleId: Int) {
    Text(
        modifier = Modifier.padding(bottom = Spacings.xxs),
        text = stringResource(titleId),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun Body(bodyId: Int) {
    Text(
        text = stringResource(bodyId),
        style = MaterialTheme.typography.bodyLarge,
    )
}

interface BulletPointListScope

private object BulletPointListScopeInstance : BulletPointListScope

@Composable
fun BulletPointList(modifier: Modifier = Modifier, content: @Composable BulletPointListScope.() -> Unit) {
    Column(
        Modifier
            .padding(vertical = Spacings.xs)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(Spacings.xxs),
    ) {
        BulletPointListScopeInstance.content()
    }
}

@Composable
private fun BulletPointWithLink(prefix: String, linkText: String, url: String, suffix: String = "") {
    Row(verticalAlignment = Alignment.Top) {
        Text("• ", color = MaterialTheme.colorScheme.onSurface)

        Text(
            text = buildAnnotatedString {
                append(prefix)

                val link = LinkAnnotation.Url(
                    url = url,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ),
                )

                withLink(link) {
                    append(linkText)
                }

                append(suffix)
            },
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun BulletPointText(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
    ) {
        Text("• ", color = MaterialTheme.colorScheme.onSurface)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CodeBlock(code: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacings.xxs),
    ) {
        Text(
            text = code,
            style = SvenskaTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.padding(Spacings.s),
        )
    }
}

private object LinkUrls {
    const val RIVSTART = "https://nokportalen.se/"
    const val SVENSKA_REPOSITORY = "https://github.com/ywegel/svenska/tree/main/scripts"
    const val PYTHON = "https://www.python.org/downloads/"
    const val UV = "https://docs.astral.sh/uv/"
}

@Preview
@Composable
private fun WordExtractorExplanationScreenPreview() {
    SvenskaTheme {
        WordExtractorExplanationScreen(
            navigateUp = {},
            navigateToWordImporterScreen = {},
        )
    }
}
