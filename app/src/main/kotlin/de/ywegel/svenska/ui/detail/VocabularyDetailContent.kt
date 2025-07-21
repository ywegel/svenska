@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.detail

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.ywegel.svenska.R
import de.ywegel.svenska.data.GeneratorConstants
import de.ywegel.svenska.data.container
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.vocabulary
import de.ywegel.svenska.ui.common.HorizontalSpacerS
import de.ywegel.svenska.ui.common.VerticalSpacerXS
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.common.vocabulary.abbreviation
import de.ywegel.svenska.ui.common.vocabulary.article
import de.ywegel.svenska.ui.common.vocabulary.mainGroupAbbreviation
import de.ywegel.svenska.ui.common.vocabulary.mainGroupUserFacingString
import de.ywegel.svenska.ui.common.vocabulary.subGroupAbbreviation
import de.ywegel.svenska.ui.common.vocabulary.wordGroupBadge.StaticWordGroupBadgeExtended
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
internal fun VocabularyDetailContent(
    vocabulary: Vocabulary,
    isFavorite: Boolean,
    showContainerInformation: Boolean = false,
    container: VocabularyContainer?,
    sheetState: SheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false,
    ),
    onDismiss: () -> Unit,
    onEditClick: (Vocabulary) -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    navigateToWordGroupScreen: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
        ) {
            Header(vocabulary, isFavorite, onFavoriteChange)

            // Chips Section
            WordInfoChips(
                vocabulary = vocabulary,
                navigateToWordGroupScreen = {
                    onDismiss()
                    navigateToWordGroupScreen()
                },
            )

            Column(Modifier.padding(horizontal = Spacings.m)) {
                // Pronunciation
                if (!vocabulary.irregularPronunciation.isNullOrBlank()) {
                    WordInfoSection(
                        title = stringResource(R.string.vocabulary_detail_section_pronunciation),
                        content = vocabulary.irregularPronunciation,
                    )
                }

                // Notes
                if (vocabulary.notes.isNotBlank()) {
                    WordInfoSection(stringResource(R.string.vocabulary_detail_section_notes), vocabulary.notes)
                }

                // Metadata
                MetadataSection(
                    vocabulary = vocabulary,
                    containerName = container?.name,
                    showContainerInformation = showContainerInformation,
                )

                VerticalSpacerXXS()

                // Edit Button
                Button(
                    onClick = { onEditClick(vocabulary) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = SvenskaIcons.Edit,
                        contentDescription = null,
                    )
                    HorizontalSpacerS()
                    Text(stringResource(R.string.vocabulary_detail_edit_btn))
                }
            }
        }
    }
}

@Composable
private fun Header(vocabulary: Vocabulary, isFavorite: Boolean, onFavoriteChange: (Boolean) -> Unit) {
    Column(Modifier.padding(horizontal = Spacings.m)) {
        // Native word + Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = vocabulary.word,
                style = SvenskaTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Spacings.s),
                maxLines = 3, // TODO: #38 Make long texts expandable in VocabularyDetailScreen
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
            )

            IconToggleButton(
                checked = isFavorite,
                onCheckedChange = onFavoriteChange,
            ) {
                Icon(
                    imageVector = if (isFavorite) SvenskaIcons.Favorite else SvenskaIcons.FavoriteBorder,
                    contentDescription = stringResource(
                        id = if (isFavorite) {
                            R.string.vocabulary_detail_remove_favorite
                        } else {
                            R.string.vocabulary_detail_add_favorite
                        },
                    ),
                )
            }
        }

        // Translation
        Text(
            text = vocabulary.translation,
            style = SvenskaTheme.typography.titleMedium,
            color = SvenskaTheme.colors.onSurfaceVariant,
            maxLines = 3, // TODO: #38 Make long texts expandable in VocabularyDetailScreen
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WordInfoChips(vocabulary: Vocabulary, navigateToWordGroupScreen: () -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacings.m),
        horizontalArrangement = Arrangement.spacedBy(Spacings.xs),
    ) {
        item {
            AssistChip(
                onClick = navigateToWordGroupScreen,
                label = {
                    Text(
                        text = with(vocabulary.wordGroup) {
                            subGroupAbbreviation()?.let {
                                stringResource(R.string.vocabulary_detail_chip_group, mainGroupUserFacingString(), it)
                            } ?: mainGroupUserFacingString()
                        },
                    )
                },
                leadingIcon = {
                    StaticWordGroupBadgeExtended(
                        mainWordGroup = vocabulary.wordGroup.mainGroupAbbreviation(),
                        subWordGroup = vocabulary.wordGroup.subGroupAbbreviation(),
                    )
                },
            )
        }

        if (vocabulary.wordGroup is WordGroup.Noun && vocabulary.gender != null) {
            item {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = with(vocabulary.gender) {
                                stringResource(R.string.vocabulary_detail_chip_gender, abbreviation(), article())
                            },
                        )
                    },
                )
            }
        }

        if (vocabulary.ending.isNotBlank()) {
            item {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(stringResource(R.string.vocabulary_detail_chip_endings, vocabulary.ending))
                    },
                )
            }
        }
    }
}

@Composable
private fun WordInfoSection(title: String, content: String) {
    Text(
        text = title,
        style = SvenskaTheme.typography.titleMedium,
        color = SvenskaTheme.colors.primary,
    )
    Text(
        text = content,
        style = SvenskaTheme.typography.bodyLarge,
        maxLines = 3, // TODO: #38 Make long texts expandable in VocabularyDetailScreen
        softWrap = true,
        overflow = TextOverflow.Ellipsis,
    )
    VerticalSpacerXS()
}

@Composable
private fun MetadataSection(vocabulary: Vocabulary, containerName: String?, showContainerInformation: Boolean) {
    Text(
        text = stringResource(R.string.vocabulary_detail_section_metadata),
        style = SvenskaTheme.typography.titleMedium,
        color = SvenskaTheme.colors.primary,
    )
    // TODO: Add a "go to container" button #46
    if (showContainerInformation && containerName != null) {
        Text(
            text = stringResource(
                id = R.string.vocabulary_detail_section_metadata_container,
                containerName,
            ),
            style = SvenskaTheme.typography.bodyMedium,
            color = SvenskaTheme.colors.onSurfaceVariant,
        )
    }
    Text(
        text = stringResource(
            id = R.string.vocabulary_detail_section_metadata_created,
            vocabulary.createdDateFormatted,
        ),
        style = SvenskaTheme.typography.bodyMedium,
        color = SvenskaTheme.colors.onSurfaceVariant,
    )
    Text(
        text = stringResource(
            id = R.string.vocabulary_detail_section_metadata_edited,
            vocabulary.lastEditedDateFormatted,
        ),
        style = SvenskaTheme.typography.bodyMedium,
        color = SvenskaTheme.colors.onSurfaceVariant,
    )
}

@Preview(showSystemUi = true)
@Composable
private fun VocabularyDetailScreenPreview(
    @PreviewParameter(VocabularyDetailPreviewParameterProvider::class) vocabulary: Vocabulary,
) {
    SvenskaTheme {
        VocabularyDetailContent(
            vocabulary = vocabulary,
            container = container(),
            isFavorite = vocabulary.isFavorite,
            onDismiss = {},
            onEditClick = {},
            onFavoriteChange = {},
            sheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Expanded,
                skipHiddenState = true,
            ),
            navigateToWordGroupScreen = {},
        )
    }
}

@VisibleForTesting
private class VocabularyDetailPreviewParameterProvider : PreviewParameterProvider<Vocabulary> {
    override val values: Sequence<Vocabulary> = sequenceOf(
        vocabulary(
            notes = "Some simple test notes",
            irregularPronunciation = "special pronunciation",
            isFavorite = true,
            created = GeneratorConstants.TEST_DATE,
            lastEdited = GeneratorConstants.TEST_DATE,
        ),
        vocabulary(
            word = GeneratorConstants.LONG_STRING,
            translation = GeneratorConstants.LONG_STRING,
            notes = GeneratorConstants.LONG_STRING,
            irregularPronunciation = GeneratorConstants.LONG_STRING,
            created = GeneratorConstants.TEST_DATE,
            lastEdited = GeneratorConstants.TEST_DATE,
        ),
        vocabulary(
            created = GeneratorConstants.TEST_DATE,
            lastEdited = GeneratorConstants.TEST_DATE,
        ),
    )
}
