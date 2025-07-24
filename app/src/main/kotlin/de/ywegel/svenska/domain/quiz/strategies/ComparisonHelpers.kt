package de.ywegel.svenska.domain.quiz.strategies

object ComparisonHelpers {
    /**
     * Normalizes input by removing dashes and collapsing multiple whitespaces into a single space.
     * Examples: "-en -ar -arna" -> "en ar arna", "en   ar  arna" -> "en ar arna", "-et - -en" -> "et en".
     */
    fun compareEndings(expected: String, userInput: String): Boolean {
        val normalizedExpected = expected.replace(compareAlphabeticRegex, " ").trim()
        val normalizedUserInput = userInput.replace(compareAlphabeticRegex, " ").trim()
        return normalizedExpected.equals(normalizedUserInput, ignoreCase = true)
    }

    private val compareAlphabeticRegex = Regex("[\\s-]*(-)?[\\s-]*")
}
