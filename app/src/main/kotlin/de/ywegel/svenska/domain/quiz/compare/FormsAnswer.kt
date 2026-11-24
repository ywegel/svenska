package de.ywegel.svenska.domain.quiz.compare

import de.ywegel.svenska.domain.quiz.FormsQuestion
import de.ywegel.svenska.domain.words.variants

sealed interface FormVerdict {
    data object Correct : FormVerdict

    /** Aligned against the closest variant, so the UI can mark the wrong letters. */
    data class Wrong(val alignment: List<EditStep>) : FormVerdict
}

/**
 * @property genderCorrect Whether the tapped gender is right, `null` if the question asks for no gender.
 * @property forms One verdict per form, in the order of [FormsQuestion.forms].
 */
data class FormsVerdict(val genderCorrect: Boolean?, val forms: List<FormVerdict>) {
    val isCorrect: Boolean get() = genderCorrect != false && forms.all { it == FormVerdict.Correct }
}

/**
 * Forms are compared exactly, without typo tolerance, as a single letter is what the forms card teaches: bilor is no
 * typo of bilar. Any variant counts, la and lade are both right for lade/la.
 */
fun FormsQuestion.check(typed: FormsQuestion): FormsVerdict {
    require(typed::class == this::class) { "Expected an answer to $this, but got $typed." }
    return FormsVerdict(
        genderCorrect = gender?.let { it == typed.gender },
        forms = typed.forms.zip(forms) { typedForm, expected -> typedForm.checkForm(expected) },
    )
}

private fun String.checkForm(expected: String): FormVerdict {
    val variants = expected.variants()
    val comparableTyped = comparable()
    if (variants.any { it.comparable() == comparableTyped }) return FormVerdict.Correct

    // TODO: Introduce variants splitting to both forms and whole words. It should be possible to share logic.
    // The alignment keeps the typed case for the UI, it ignores case and normalizes to NFC itself
    val closest = variants
        .map { OptimalStringAlignment.alignment(trim(), it.trim()) }
        .minBy { alignment -> alignment.count { it !is EditStep.Match } }
    return FormVerdict.Wrong(closest)
}
