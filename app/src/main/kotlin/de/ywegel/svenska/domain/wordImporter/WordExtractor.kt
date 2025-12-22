package de.ywegel.svenska.domain.wordImporter

object WordExtractor {
    fun extractWordAndEndings(word: String): Pair<String, List<String>> {
        val normalizedInput = word.normalizePdfDashes()

        val regex = Regex("""(.+?)\s*\(([^)]+)\)""")
        val matchResult = regex.find(normalizedInput)

        return if (matchResult != null) {
            val baseWord = matchResult.groupValues[1].trim()
            val endings = matchResult.groupValues[2]
                .split(",")
                .map { it.trim() }
            Pair(baseWord, endings)
        } else {
            Pair(normalizedInput.trim(), emptyList())
        }
    }

    /**
     * Replace the long dashes that are wrongly extracted from the pdf
     */
    fun String.normalizePdfDashes(): String {
        return this.replace('\u2212', '-')
            .replace('\u2212', '-')
            .replace('\u2013', '-')
            .replace('\u2014', '-')
            .replace('\u2010', '-')
    }
}
