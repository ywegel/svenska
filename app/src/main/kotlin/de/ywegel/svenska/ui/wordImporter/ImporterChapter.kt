package de.ywegel.svenska.ui.wordImporter

import kotlinx.serialization.Serializable

@Serializable
data class ImporterChapter(
    val chapter: String,
    val words: List<List<String>>,
)
