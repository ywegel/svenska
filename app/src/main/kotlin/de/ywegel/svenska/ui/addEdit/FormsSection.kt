package de.ywegel.svenska.ui.addEdit

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.BuildConfig
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.words.splitAtChange
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.userFacingString
import de.ywegel.svenska.ui.common.vocabulary.article
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

private val FormLabelWidth = 92.dp

/**
 * The forms of the changing word, either built from the endings or written out by the user.
 * Colors: the part that changes is primary, prefix and suffix stay greyed out, app suggestions are tertiary.
 */
@Composable
fun FormsSection(uiState: AddEditUiState, callbacks: AddEditVocabularyCallbacks, modifier: Modifier = Modifier) {
    val isWrittenOut = uiState.formsMode == FormsMode.WrittenOut
    var showSwitchToEndingsDialog by rememberSaveable { mutableStateOf(false) }

    if (showSwitchToEndingsDialog) {
        SwitchToEndingsDialog(
            onConfirm = {
                showSwitchToEndingsDialog = false
                callbacks.switchToEndings()
            },
            onDismiss = { showSwitchToEndingsDialog = false },
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacings.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.addEdit_forms_title),
                style = SvenskaTheme.typography.titleSmall,
                color = SvenskaTheme.colors.primary,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = {
                    when {
                        !isWrittenOut -> callbacks.switchToWrittenOutForms()
                        uiState.writtenFormsDifferFromEndings -> showSwitchToEndingsDialog = true
                        else -> callbacks.switchToEndings()
                    }
                },
            ) {
                Icon(SvenskaIcons.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(
                        if (isWrittenOut) R.string.addEdit_forms_use_endings else R.string.addEdit_forms_write_out,
                    ),
                    modifier = Modifier.padding(start = Spacings.xs),
                )
            }
        }

        if (!isWrittenOut) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.endings,
                onValueChange = callbacks::updateEndings,
                label = { Text(stringResource(R.string.addEdit_label_endings)) },
            )
            uiState.suggestedWordGroup?.let {
                WordGroupSuggestionChip(suggestion = it, onApply = callbacks::applySuggestedWordGroup)
            }
        }

        if (uiState.wordInput.word.isNotBlank()) {
            FormsList(uiState, callbacks, modifier = Modifier.padding(top = Spacings.xs))
        }

        uiState.writtenFormsError?.let { WrittenFormsErrorText(it) }
    }
}

@Composable
private fun WrittenFormsErrorText(error: WrittenFormsError) {
    val message = when (error) {
        WrittenFormsError.Gap -> R.string.addEdit_forms_gap_error
        WrittenFormsError.Space -> R.string.addEdit_forms_space_error
    }
    Text(
        text = stringResource(message),
        style = SvenskaTheme.typography.bodySmall,
        color = SvenskaTheme.colors.error,
        modifier = Modifier.padding(horizontal = Spacings.m),
    )
}

@Composable
private fun SwitchToEndingsDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.addEdit_forms_switch_dialog_title)) },
        text = { Text(stringResource(R.string.addEdit_forms_switch_dialog_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.addEdit_forms_switch_dialog_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        },
    )
}

@Composable
private fun WordGroupSuggestionChip(suggestion: WordGroup, onApply: () -> Unit) {
    AssistChip(
        onClick = onApply,
        label = { Text(stringResource(R.string.addEdit_forms_suggestion, suggestion.userFacingName())) },
        leadingIcon = {
            Icon(SvenskaIcons.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = SvenskaTheme.colors.tertiaryContainer,
            labelColor = SvenskaTheme.colors.onTertiaryContainer,
            leadingIconContentColor = SvenskaTheme.colors.tertiary,
        ),
        border = null,
    )
}

@Composable
private fun WordGroup.userFacingName(): String {
    val mainGroup = ViewWordGroup.fromWordGroup(this).userFacingString()
    val subGroup = when (this) {
        is WordGroup.Noun -> subgroup.selectorAbbreviation()
        is WordGroup.Verb -> subgroup.selectorAbbreviation()
        else -> null
    }
    return listOfNotNull(mainGroup, subGroup).joinToString(" · ")
}

@Composable
private fun FormsList(uiState: AddEditUiState, callbacks: AddEditVocabularyCallbacks, modifier: Modifier = Modifier) {
    val labels = formLabels(uiState.formWordGroup)
    val wordInput = uiState.wordInput
    val isWrittenOut = uiState.formsMode == FormsMode.WrittenOut

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacings.s),
        color = SvenskaTheme.colors.surfaceContainerLow,
    ) {
        Column {
            if (uiState.showReviewHint) {
                ReviewHint(onWriteOut = callbacks::switchToWrittenOutForms, onConfirm = callbacks::confirmForms)
            }
            Column(modifier = Modifier.padding(horizontal = Spacings.m, vertical = Spacings.xxs)) {
                FormRow(label = labels.base) {
                    Text(
                        text = baseFormText(uiState, wordInput),
                        style = SvenskaTheme.typography.titleLarge,
                    )
                }
                uiState.forms.forEachIndexed { index, form ->
                    HorizontalDivider(color = SvenskaTheme.colors.outlineVariant)
                    FormRow(label = labels.forms.getOrElse(index) { "" }) {
                        if (isWrittenOut) {
                            WrittenOutForm(
                                form = form,
                                prefix = wordInput.prefix,
                                suffix = wordInput.suffix,
                                onFormChange = { callbacks.updateWrittenForm(index, it) },
                            )
                        } else {
                            Text(
                                text = builtFormText(form, wordInput),
                                style = SvenskaTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Never blocks saving: the flag is set on purpose more often than needed, the user decides. */
@Composable
private fun ReviewHint(onWriteOut: () -> Unit, onConfirm: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SvenskaTheme.colors.tertiaryContainer,
        contentColor = SvenskaTheme.colors.onTertiaryContainer,
    ) {
        Column(modifier = Modifier.padding(start = Spacings.m, end = Spacings.xs, top = Spacings.s)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = SvenskaIcons.WarningAmber,
                    contentDescription = null,
                    tint = SvenskaTheme.colors.tertiary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.addEdit_forms_review_title),
                    style = SvenskaTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = Spacings.s),
                )
            }
            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onWriteOut) {
                    Text(
                        stringResource(R.string.addEdit_forms_review_write_out),
                        color = SvenskaTheme.colors.onTertiaryContainer,
                    )
                }
                TextButton(onClick = onConfirm) {
                    Icon(
                        imageVector = SvenskaIcons.Check,
                        contentDescription = null,
                        tint = SvenskaTheme.colors.onTertiaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.addEdit_forms_review_confirm),
                        color = SvenskaTheme.colors.onTertiaryContainer,
                        modifier = Modifier.padding(start = Spacings.xs),
                    )
                }
            }
        }
    }
}

@Composable
private fun FormRow(label: String, content: @Composable () -> Unit) {
    Row(modifier = Modifier.padding(vertical = Spacings.xs), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = SvenskaTheme.typography.labelMedium,
            color = SvenskaTheme.colors.onSurfaceVariant,
            modifier = Modifier.width(FormLabelWidth),
        )
        content()
    }
}

/**
 * The user types only the changing word, prefix and suffix are added automatically.
 * An underlined inline field instead of a TextField, so the rows keep the height of the built forms.
 */
@Composable
private fun WrittenOutForm(form: String, prefix: String, suffix: String, onFormChange: (String) -> Unit) {
    val affixStyle = SvenskaTheme.typography.bodyLarge.copy(color = SvenskaTheme.colors.onSurfaceVariant)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val underlineThickness = if (isFocused) 2.dp else 1.dp
    val underlineColor = if (isFocused) SvenskaTheme.colors.primary else SvenskaTheme.colors.outline

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (prefix.isNotBlank()) Text(prefix, style = affixStyle, modifier = Modifier.padding(end = Spacings.xs))
        BasicTextField(
            value = form,
            onValueChange = onFormChange,
            singleLine = true,
            textStyle = SvenskaTheme.typography.bodyLarge.copy(color = SvenskaTheme.colors.onSurface),
            cursorBrush = SolidColor(SvenskaTheme.colors.primary),
            interactionSource = interactionSource,
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Column {
                    Box(Modifier.padding(vertical = Spacings.xxs)) { innerTextField() }
                    HorizontalDivider(thickness = underlineThickness, color = underlineColor)
                }
            },
        )
        if (suffix.isNotBlank()) Text(suffix, style = affixStyle, modifier = Modifier.padding(start = Spacings.xs))
    }
}

@Composable
private fun baseFormText(uiState: AddEditUiState, wordInput: WordInput): AnnotatedString {
    val article = when (uiState.selectedWordGroup) {
        ViewWordGroup.Noun -> uiState.gender.article()
        ViewWordGroup.Verb -> "att"
        else -> ""
    }
    val leading = listOf(article, wordInput.prefix).filter { it.isNotBlank() }.joinToString(" ")
    return affixedText(leading, wordInput.word, changingPart = "", wordInput.suffix)
}

@Composable
private fun builtFormText(form: String, wordInput: WordInput): AnnotatedString {
    val (stem, change) = form.splitAtChange(wordInput.word)
    return affixedText(wordInput.prefix, stem, change, wordInput.suffix)
}

/** Greyed out prefix and suffix, the stem in the normal text color and the changing part in primary. */
@Composable
private fun affixedText(prefix: String, stem: String, changingPart: String, suffix: String): AnnotatedString {
    val affixStyle = SpanStyle(color = SvenskaTheme.colors.onSurfaceVariant)
    val changeStyle = SpanStyle(color = SvenskaTheme.colors.primary, fontWeight = FontWeight.Medium)
    return buildAnnotatedString {
        if (prefix.isNotBlank()) withStyle(affixStyle) { append("$prefix ") }
        append(stem)
        withStyle(changeStyle) { append(changingPart) }
        if (suffix.isNotBlank()) withStyle(affixStyle) { append(" $suffix") }
    }
}

private class FormLabels(val base: String, val forms: List<String>)

@Composable
private fun formLabels(wordGroup: WordGroup): FormLabels = when (wordGroup) {
    is WordGroup.Noun -> FormLabels(
        base = stringResource(R.string.addEdit_form_noun_indefinite),
        forms = listOf(
            stringResource(R.string.addEdit_form_noun_definite),
            stringResource(R.string.addEdit_form_noun_plural),
            stringResource(R.string.addEdit_form_noun_definite_plural),
        ),
    )

    is WordGroup.Verb -> FormLabels(
        base = stringResource(R.string.addEdit_form_verb_infinitive),
        forms = listOf(
            stringResource(R.string.addEdit_form_verb_present),
            stringResource(R.string.addEdit_form_verb_past),
            stringResource(R.string.addEdit_form_verb_supine),
        ),
    )

    WordGroup.Adjective -> FormLabels(
        base = stringResource(R.string.addEdit_form_adjective_base),
        forms = listOf(
            stringResource(R.string.addEdit_form_adjective_neuter),
            stringResource(R.string.addEdit_form_adjective_plural),
        ),
    )

    // Unreachable: FormsSection is only shown when uiState.hasForms, and Other has no forms
    WordGroup.Other -> {
        check(!BuildConfig.DEBUG) { "Form labels requested for WordGroup.Other" }
        FormLabels(base = "", forms = emptyList())
    }
}
