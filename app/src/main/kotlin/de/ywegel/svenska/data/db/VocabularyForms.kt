package de.ywegel.svenska.data.db

data class VocabularyForms(
    val id: Int,
    val form1: String?,
    val form2: String?,
    val form3: String?,
    val formsNeedReview: Boolean,
)
