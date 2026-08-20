package de.ywegel.svenska.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ImporterChapter(
    val chapter: String,
    val words: List<List<String>>,
)
