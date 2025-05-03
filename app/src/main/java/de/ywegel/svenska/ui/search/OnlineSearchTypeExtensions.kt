package de.ywegel.svenska.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.search.OnlineSearchType

@ReadOnlyComposable
@Composable
fun OnlineSearchType.userFacingTitle(): String {
    val id = when (this) {
        OnlineSearchType.DictCC -> R.string.search_base_url_dict_cc
        OnlineSearchType.Pons -> R.string.search_base_url_pons
        OnlineSearchType.DeepL -> R.string.search_base_url_deepl
        OnlineSearchType.GoogleTranslate -> R.string.search_base_url_google_translate
        is OnlineSearchType.Custom -> R.string.search_base_url_custom
    }

    return stringResource(id)
}

fun OnlineSearchType.toUrl(): String {
    return when (this) {
        OnlineSearchType.DictCC -> "https://ensv.dict.cc/?s="
        OnlineSearchType.Pons -> "https://en.pons.com/text-translation/swedish-english?q="
        OnlineSearchType.DeepL -> "https://www.deepl.com/en/translator#sv/en-gb/"
        OnlineSearchType.GoogleTranslate -> "https://translate.google.com/?sl=sv&tl=en&op=translate&text="
        is OnlineSearchType.Custom -> this.url
    }
}
