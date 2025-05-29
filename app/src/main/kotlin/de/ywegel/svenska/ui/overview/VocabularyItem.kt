package de.ywegel.svenska.ui.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.vocabularies
import de.ywegel.svenska.data.vocabulary
import de.ywegel.svenska.ui.common.HorizontalSpacerXS
import de.ywegel.svenska.ui.common.vocabulary.WordGroupBadgeExtended
import de.ywegel.svenska.ui.common.vocabulary.mainGroupAbbreviation
import de.ywegel.svenska.ui.common.vocabulary.subGroupAbbreviation
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

// TODO: Refactor Vocabulary Items (both normal and compact)
@Suppress("detekt:LongMethod")
@Composable
fun VocabularyListItem(vocabulary: Vocabulary, modifier: Modifier = Modifier, onClick: (Vocabulary) -> Unit = {}) {
    val annotatedWord = vocabulary.annotatedWord
    val showEnding = vocabulary.ending.isNotBlank()
    val showGender = vocabulary.wordGroup is WordGroup.Noun && vocabulary.gender != null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacings.xs))
            .clickable { onClick(vocabulary) },
        tonalElevation = Spacings.xxxs,
        color = SvenskaTheme.colors.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = Spacings.s, vertical = Spacings.xs)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // WordGroup-Badge
            WordGroupBadgeExtended(
                mainWordGroup = vocabulary.wordGroup.mainGroupAbbreviation(),
                subWordGroup = vocabulary.wordGroup.subGroupAbbreviation(),
            )

            HorizontalSpacerXS()

            Column(modifier = Modifier.weight(1f)) {
                // First line: Swedish word + endings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = annotatedWord,
                        style = SvenskaTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    if (showGender) {
                        Text(
                            text = when (vocabulary.gender) {
                                // TODO: String ressources
                                Gender.Ultra -> "U"
                                Gender.Neutra -> "N"
                            },
                            style = SvenskaTheme.typography.labelMedium,
                            color = SvenskaTheme.colors.primary,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }

                    if (showEnding) {
                        Text(
                            text = "(${vocabulary.ending})",
                            style = SvenskaTheme.typography.labelMedium,
                            color = SvenskaTheme.colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }

                Spacer(Modifier.height(Spacings.xxxs))

                // Translation
                Text(
                    text = vocabulary.translation,
                    style = SvenskaTheme.typography.bodySmall,
                    color = SvenskaTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 1.dp),
                )
            }
        }
    }
}

@Composable
fun VocabularyItemCompact(vocabulary: Vocabulary, modifier: Modifier = Modifier, onClick: (Vocabulary) -> Unit = {}) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(vocabulary) },
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacings.xs, horizontal = Spacings.s),
                horizontalArrangement = Arrangement.spacedBy(Spacings.s),
            ) {
                // Gender
                vocabulary.gender?.let { gender ->
                    Text(
                        text = stringResource(gender.userFacingString()),
                        modifier = Modifier.weight(GENDER_WEIGHT),
                        style = SvenskaTheme.typography.bodyMedium,
                        color = SvenskaTheme.colors.primary.copy(alpha = 0.8f),
                    )
                }

                // Swedish word
                Text(
                    text = vocabulary.annotatedWord,
                    modifier = Modifier.weight(WORD_WEIGHT),
                    style = SvenskaTheme.typography.bodyMedium,
                    color = SvenskaTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Translation
                Text(
                    text = vocabulary.translation,
                    modifier = Modifier.weight(TRANSLATION_WEIGHT),
                    style = SvenskaTheme.typography.bodyMedium,
                    color = SvenskaTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Endings
                if (vocabulary.ending.isNotBlank()) {
                    Text(
                        text = vocabulary.ending,
                        modifier = Modifier.weight(ENDINGS_WEIGHT),
                        style = SvenskaTheme.typography.bodyMedium,
                        color = SvenskaTheme.colors.secondary.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Spacings.xs),
                color = SvenskaTheme.colors.outlineVariant,
            )
        }
    }
}

private const val GENDER_WEIGHT = 10f
private const val WORD_WEIGHT = 30f
private const val TRANSLATION_WEIGHT = 30f
private const val ENDINGS_WEIGHT = 25f

// TODO: All kinds of previews

@Preview(showBackground = true)
@Composable
fun SingleWordPreview() {
    SvenskaTheme {
        Surface {
            VocabularyItemCompact(
                vocabulary = vocabulary(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SingleWordPreviewNewCreative() {
    SvenskaTheme {
        Surface {
            VocabularyListItem(
                vocabulary = vocabulary(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WordListPreview() {
    val sampleWords = vocabularies()

    SvenskaTheme {
        Surface {
            LazyColumn {
                items(sampleWords) { word ->
                    VocabularyItemCompact(vocabulary = word)
                }
            }
        }
    }
}

fun Gender.userFacingString(): Int {
    return when (this) {
        Gender.Ultra -> R.string.lang_common
        Gender.Neutra -> R.string.lang_neuter
    }
}
