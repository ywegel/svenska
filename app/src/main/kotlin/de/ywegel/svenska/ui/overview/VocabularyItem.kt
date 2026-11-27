package de.ywegel.svenska.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.vocabularies
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.ui.common.HorizontalSpacerXS
import de.ywegel.svenska.ui.common.vocabulary.abbreviation
import de.ywegel.svenska.ui.common.vocabulary.annotatedWord
import de.ywegel.svenska.ui.common.vocabulary.wordGroupBadge.StaticWordGroupBadgeExtended
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

// TODO: Refactor Vocabulary Items (both normal and compact)
@Suppress("detekt:LongMethod")
@Composable
fun VocabularyListItem(
    vocabulary: Vocabulary,
    modifier: Modifier = Modifier,
    poolIndicator: PoolIndicator = PoolIndicator.NotInPool,
    containerColor: Color = SvenskaTheme.colors.surfaceVariant,
    onClick: (Vocabulary) -> Unit = {},
) {
    val annotatedWord = vocabulary.annotatedWord
    val showEnding = vocabulary.ending.isNotBlank()
    val showGender = vocabulary.wordGroup is WordGroup.Noun && vocabulary.gender != null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacings.xs))
            .clickable { onClick(vocabulary) },
        tonalElevation = Spacings.xxxs,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier
                .padding(start = Spacings.s, end = Spacings.xs, top = Spacings.xs, bottom = Spacings.xs)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // WordGroup-Badge
            StaticWordGroupBadgeExtended(vocabulary.wordGroup)

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
                            text = vocabulary.gender.abbreviation(),
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

                    if (vocabulary.formsNeedReview) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = stringResource(R.string.accessibility_vocabulary_forms_need_review),
                            tint = SvenskaTheme.colors.error,
                            modifier = Modifier
                                .padding(start = Spacings.xxs)
                                .size(STATUS_ICON_SIZE),
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

            PoolStatusSlot(poolIndicator, Modifier.padding(start = Spacings.xs))
        }
    }
}

/** Fixed width, so the endings line up whether a word has a status or not. */
@Composable
private fun PoolStatusSlot(indicator: PoolIndicator, modifier: Modifier = Modifier) {
    Box(modifier = modifier.width(STATUS_ICON_SIZE), contentAlignment = Alignment.Center) {
        when (indicator) {
            PoolIndicator.NotInPool -> Unit
            PoolIndicator.Queued -> PoolStatusIcon(
                icon = Icons.Outlined.Schedule,
                description = stringResource(R.string.accessibility_vocabulary_pool_queued),
            )
            PoolIndicator.Paused -> PoolStatusIcon(
                icon = Icons.Rounded.Pause,
                description = stringResource(R.string.accessibility_vocabulary_pool_paused),
            )
            is PoolIndicator.Active -> FacetDots(indicator.unlockedFacets)
        }
    }
}

@Composable
private fun PoolStatusIcon(icon: ImageVector, description: String) {
    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = SvenskaTheme.colors.onSurfaceVariant,
        modifier = Modifier.size(STATUS_ICON_SIZE),
    )
}

/** Vertical, filled from the bottom like a level meter: bottom = SV→DE, middle = DE→SV, top = forms. */
@Composable
private fun FacetDots(unlocked: Int) {
    val description = stringResource(R.string.accessibility_vocabulary_pool_active, unlocked, FACET_COUNT)
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        for (level in FACET_COUNT downTo 1) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(
                        color = if (level <= unlocked) {
                            SvenskaTheme.colors.primary
                        } else {
                            SvenskaTheme.colors.outlineVariant
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

private const val FACET_COUNT = 3
private val STATUS_ICON_SIZE = 14.dp

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
private fun PrefixAndSuffixPreview() {
    SvenskaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Spacings.xs)) {
            VocabularyListItem(
                vocabulary = vocabulary(word = "dröm", translation = "größter Traum").copy(wordPrefix = "högsta"),
            )
            VocabularyListItem(
                vocabulary = vocabulary(word = "känna", translation = "sich fühlen").copy(wordSuffix = "sig"),
            )
        }
    }
}

@Preview(widthDp = 380)
@Composable
private fun PoolStatusPreview() {
    SvenskaTheme {
    val noun = WordGroup.Noun(WordGroup.NounSubgroup.OR)
    val verb = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A)
    val rows = listOf(
        vocabulary(word = "lampa", translation = "Lampe", wordGroup = noun) to PoolIndicator.NotInPool,
        vocabulary(word = "flicka", translation = "Mädchen", wordGroup = noun) to PoolIndicator.Queued,
        vocabulary(word = "känna", translation = "sich fühlen", wordGroup = verb)
            .copy(wordSuffix = "sig", ending = "er de t") to PoolIndicator.Active(1),
        vocabulary(word = "dröm", translation = "größter Traum", wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR))
            .copy(wordPrefix = "högsta", ending = "men mar marna") to PoolIndicator.Active(2),
        vocabulary(word = "gata", translation = "Straße", wordGroup = noun)
            to PoolIndicator.Active(unlockedFacets = FACET_COUNT),
        vocabulary(word = "klass", translation = "Klasse", wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL))
            .copy(ending = "en er arna", formsNeedReview = true) to PoolIndicator.Active(2),
        vocabulary(word = "kyrka", translation = "Kirche", wordGroup = noun) to PoolIndicator.Paused,
    )

        Surface {
            Column(modifier = Modifier.padding(Spacings.xs), verticalArrangement = Arrangement.spacedBy(Spacings.xs)) {
                rows.forEach { (vocabulary, indicator) ->
                    VocabularyListItem(vocabulary = vocabulary, poolIndicator = indicator)
                }
            }
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
