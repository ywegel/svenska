package de.ywegel.svenska.domain.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class OnlineSearchType {
    @Serializable
    @SerialName("DictCC")
    data object DictCC : OnlineSearchType()

    @Serializable
    @SerialName("Pons")
    data object Pons : OnlineSearchType()

    @Serializable
    @SerialName("DeepL")
    data object DeepL : OnlineSearchType()

    @Serializable
    @SerialName("GoogleTranslate")
    data object GoogleTranslate : OnlineSearchType()

    @Serializable
    @SerialName("Custom")
    data class Custom(val url: String) : OnlineSearchType()
}
