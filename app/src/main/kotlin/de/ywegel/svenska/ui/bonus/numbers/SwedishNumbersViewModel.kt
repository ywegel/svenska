package de.ywegel.svenska.ui.bonus.numbers

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.ywegel.svenska.ui.common.stringWithHighlightSeparators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Disclaimer:
 * Yes, i know. Having androidX and ApplicationContext is a bad design choice. This feature was
 * build in 20 minutes and will probably get reworked anyways. I just don't have the time to think
 * of something cleaner.
 */

data class NumberItem(
    val number: Int,
    val annotatedString: AnnotatedString,
)

data class SwedishNumbersUiState(
    val regularNumbers: List<NumberItem> = emptyList(),
    val tensNumbers: List<NumberItem> = emptyList(),
)

/**
 * This ViewModel calculates all numbers with their annotation in advance and caches them
 */
// TODO: Refactor to move android logic out of viewmodel. Then remove MagicNumber suppresion
@Suppress("detekt:MagicNumber")
@HiltViewModel
class SwedishNumbersViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SwedishNumbersUiState())
    val uiState: StateFlow<SwedishNumbersUiState> = _uiState.asStateFlow()

    init {
        processAllNumbers()
    }

    private fun processAllNumbers() {
        // Process regular numbers (1-23)
        val regularNumbers = (1..23).map { number ->
            NumberItem(
                number = number,
                annotatedString = applicationContext.getSwedishNumber(number)
                    .let(
                        // TODO: move this to the composable somehow and use a color from the theme to highlight, instead of bold
                        ::stringWithHighlightSeparators,
                    ),
            )
        }

        // Process tens numbers (10, 20, 30, ..., 100)
        val tensNumbers = (1..10).map { it * 10 }.map { number ->
            NumberItem(
                number = number,
                annotatedString = applicationContext.getSwedishNumber(number)
                    .let(::stringWithHighlightSeparators),
            )
        }

        _uiState.value = SwedishNumbersUiState(
            regularNumbers = regularNumbers,
            tensNumbers = tensNumbers,
        )
    }
}
