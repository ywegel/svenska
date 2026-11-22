package de.ywegel.svenska.domain.quiz.compare

import java.text.Normalizer

// The only JVM dependency of the comparison, kept in one place so it can become an expect/actual function for KMP
internal fun String.toNfc(): String = Normalizer.normalize(this, Normalizer.Form.NFC)
