package de.ywegel.svenska.domain.quiz.compare

/**
 * Levenshtein distance that also counts two swapped neighbouring letters as one edit, as long as no letter is edited
 * twice (Damerau-Levenshtein, optimal string alignment variant). Both words are compared in NFC and ignoring case.
 *
 * OSA is no metric: CA -> ABC is 3, although CA -> AC -> ABC takes 2. So nothing built on top of it may rely on the
 * triangle inequality, e.g. BK-trees.
 */
object OptimalStringAlignment {
    fun distance(typed: String, expected: String): Int = AlignmentTable(typed.toNfc(), expected.toNfc()).distance

    fun alignment(typed: String, expected: String): List<EditStep> =
        AlignmentTable(typed.toNfc(), expected.toNfc()).alignment()
}

/**
 * Cell (i, j) holds the distance between the first i typed and the first j expected letters (Wagner & Fischer 1974).
 * Filling and walking back use the same [movesInto], so the alignment always matches the distance.
 */
private class AlignmentTable(private val typed: String, private val expected: String) {
    private val table = Array(typed.length + 1) { IntArray(expected.length + 1) }

    init {
        for (i in 0..typed.length) {
            for (j in 0..expected.length) {
                table[i][j] = movesInto(i, j).minOfOrNull { it.distanceVia() } ?: 0
            }
        }
    }

    val distance: Int get() = table[typed.length][expected.length]

    fun alignment(): List<EditStep> {
        val steps = ArrayDeque<EditStep>()
        var i = typed.length
        var j = expected.length
        while (i > 0 || j > 0) {
            val move = movesInto(i, j).first { it.distanceVia() == table[i][j] }
            steps.addFirst(move.step)
            i = move.fromTyped
            j = move.fromExpected
        }
        return steps.toList()
    }

    // The order decides between equally short alignments: a swap explains a typo better than two wrong letters
    private fun movesInto(i: Int, j: Int): List<Move> = buildList {
        val hasBothLetters = i > 0 && j > 0
        val isMatch = hasBothLetters && sameLetter(typed[i - 1], expected[j - 1])
        val isSwap = i > 1 && j > 1 &&
            sameLetter(typed[i - 1], expected[j - 2]) &&
            sameLetter(typed[i - 2], expected[j - 1])

        if (isMatch) add(Move(i - 1, j - 1, EditStep.Match(typedLetter(i), expectedLetter(j))))
        if (isSwap) add(Move(i - 2, j - 2, EditStep.SwappedLetters(typedPair(i), expectedPair(j))))
        if (hasBothLetters && !isMatch) add(Move(i - 1, j - 1, EditStep.WrongLetter(typedLetter(i), expectedLetter(j))))
        if (j > 0) add(Move(i, j - 1, EditStep.MissingLetter(expectedLetter(j))))
        if (i > 0) add(Move(i - 1, j, EditStep.ExtraLetter(typedLetter(i))))
    }

    private fun typedLetter(i: Int): String = typed[i - 1].toString()

    private fun expectedLetter(j: Int): String = expected[j - 1].toString()

    private fun typedPair(i: Int): String = typed.substring(i - 2, i)

    private fun expectedPair(j: Int): String = expected.substring(j - 2, j)

    private fun sameLetter(a: Char, b: Char): Boolean = a.equals(b, ignoreCase = true)

    private fun Move.distanceVia(): Int = table[fromTyped][fromExpected] + cost
}

private class Move(val fromTyped: Int, val fromExpected: Int, val step: EditStep) {
    val cost: Int get() = if (step is EditStep.Match) 0 else 1
}
