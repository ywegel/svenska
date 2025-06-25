@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.detail

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import de.ywegel.svenska.data.model.Vocabulary
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
fun VocabularyDetailScreen(
    state: VocabularyDetailState,
    onDismiss: () -> Unit = {},
    onEditClick: (Vocabulary) -> Unit = {},
    navigateToWordGroupScreen: () -> Unit,
) {
    if (state is VocabularyDetailState.Visible) {
        val viewModel: VocabularyDetailViewModel = hiltViewModel()

        var isFavorite by remember { mutableStateOf(state.selectedVocabulary.isFavorite) }

        VocabularyDetailContent(
            vocabulary = state.selectedVocabulary,
            isFavorite = isFavorite,
            onDismiss = onDismiss,
            onEditClick = onEditClick,
            onFavoriteChange = { newValue ->
                isFavorite = newValue
                viewModel.toggleFavorite(state.selectedVocabulary.id, newValue)
            },
            navigateToWordGroupScreen = navigateToWordGroupScreen,
        )
    }
}

sealed interface VocabularyDetailState {
    data object Hidden : VocabularyDetailState
    data class Visible(val selectedVocabulary: Vocabulary) : VocabularyDetailState
}

@Composable
private fun VocabularyDetailContent(
    vocabulary: Vocabulary,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onEditClick: (Vocabulary) -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    sheetState: SheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false,
    ),
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
                    WordInfoSection("Pronunciation", vocabulary.irregularPronunciation)
                }

                // Notes
                if (vocabulary.notes.isNotBlank()) {
                    WordInfoSection("Notes", vocabulary.notes)
                }

                // Metadata
                Text(
                    text = "Metadata",
                    style = SvenskaTheme.typography.titleMedium,
                    color = SvenskaTheme.colors.primary,
                )
                Text(
                    text = "Created: ${vocabulary.createdDateFormatted}",
                    style = SvenskaTheme.typography.bodyMedium,
                    color = SvenskaTheme.colors.onSurfaceVariant,
                )
                Text(
                    text = "Last edited: ${vocabulary.lastEditedDateFormatted}",
                    style = SvenskaTheme.typography.bodyMedium,
                    color = SvenskaTheme.colors.onSurfaceVariant,
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
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
private fun Header(vocabulary: Vocabulary, isFavorite: Boolean, onFavoriteChange: (Boolean) -> Unit) {
    Column(Modifier.padding(horizontal = Spacings.m)) {
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
                maxLines = 3,
                softWrap = true,
            )

            IconToggleButton(
                checked = isFavorite,
                onCheckedChange = onFavoriteChange,
            ) {
                Icon(
                    imageVector = if (isFavorite) SvenskaIcons.Favorite else SvenskaIcons.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                )
            }
        }

        // Translation
        Text(
            text = vocabulary.translation,
            style = SvenskaTheme.typography.titleMedium,
            color = SvenskaTheme.colors.onSurfaceVariant,
            maxLines = 3,
            softWrap = true,
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
                            "${mainGroupUserFacingString()} (${subGroupAbbreviation()})"
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
                                "Gender: ${abbreviation()} (${article()})"
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
                        Text("Endings: ${vocabulary.ending}")
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
    )
    VerticalSpacerXS()
}

@Preview(showSystemUi = true)
@Composable
private fun VocabularyDetailScreenPreview() {
    SvenskaTheme {
        VocabularyDetailContent(
            vocabulary = vocabulary(
                notes = "Some simple test notes",
                irregularPronunciation = "special pronunciation",
            ),
            isFavorite = false,
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
