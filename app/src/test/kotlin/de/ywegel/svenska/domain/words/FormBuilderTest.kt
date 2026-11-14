package de.ywegel.svenska.domain.words

import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.testdata.ReferenceWords
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class FormBuilderTest {
    @ParameterizedTest
    @MethodSource("buildableReferenceWords", "formEdgeCases")
    fun `should build forms from endings`(
        word: String,
        endings: List<String>,
        wordGroup: WordGroup,
        expectedForms: List<String>,
    ) {
        // When
        val result = FormBuilder.build(word, endings, wordGroup)

        // Then
        assertEquals(expectedForms, result.forms)
    }

    @ParameterizedTest
    @MethodSource("referenceReviews", "reviewEdgeCases")
    fun `should flag forms that need review`(
        word: String,
        endings: List<String>,
        wordGroup: WordGroup,
        expectedNeedsReview: Boolean,
    ) {
        // When
        val result = FormBuilder.build(word, endings, wordGroup)

        // Then
        assertEquals(expectedNeedsReview, result.needsReview)
    }

    @ParameterizedTest
    @MethodSource("writtenOutReferenceWords")
    fun `should keep written out forms`(word: String, forms: List<String>, wordGroup: WordGroup) {
        // When
        val result = FormBuilder.build(word, forms, wordGroup)

        // Then
        assertEquals(forms, result.forms)
    }

    @Test
    fun `other words have no forms`() {
        // When
        val result = FormBuilder.build("hej då", emptyList(), WordGroup.Other)

        // Then
        assertEquals(BuiltForms(forms = emptyList(), needsReview = false), result)
    }

    companion object {
        private val noun = WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)
        private val verb = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)

        @JvmStatic
        fun buildableReferenceWords() = ReferenceWords.all.filter { !it.formsNeedReview }
            .map { Arguments.of(it.word, it.endings, it.wordGroup, it.forms) }

        @JvmStatic
        fun referenceReviews() = ReferenceWords.all
            .map { Arguments.of(it.word, it.endings, it.wordGroup, it.formsNeedReview) }

        @JvmStatic
        fun writtenOutReferenceWords() = ReferenceWords.all.map { Arguments.of(it.word, it.forms, it.wordGroup) }

        @JvmStatic
        fun formEdgeCases(): List<Arguments> = listOf(
            // Abbreviated piece of a compound
            Arguments.of(
                "trafikolycka", listOf("-n", "-olyckor", "-olyckorna"), noun,
                listOf("trafikolyckan", "trafikolyckor", "trafikolyckorna"),
            ),
            Arguments.of("examen", listOf("-", "examina", "examina"), noun, listOf("examen", "examina", "examina")),
            // Abbreviated piece with a vowel change
            Arguments.of(
                "storstad", listOf("-en", "-städer", "-städerna"), noun,
                listOf("storstaden", "storstäder", "storstäderna"),
            ),
            Arguments.of("bidra", listOf("-r", "-drog", "-dragit"), verb, listOf("bidrar", "bidrog", "bidragit")),
            // Written out forms that share no prefix with the word
            Arguments.of("vara", listOf("är", "var", "varit"), verb, listOf("är", "var", "varit")),
            Arguments.of("lägga", listOf("-er", "la(de)", "lagt"), verb, listOf("lägger", "la(de)", "lagt")),
            Arguments.of("annan", listOf("annat", "andra"), WordGroup.Adjective, listOf("annat", "andra")),
            // Nouns on -are lose the e in the definite plural
            Arguments.of("läkare", listOf("-n", "-", "-na"), noun, listOf("läkaren", "läkare", "läkarna")),
            Arguments.of("tränare", listOf("-n", "-", "-arna"), noun, listOf("tränaren", "tränare", "tränarna")),
            // Ending repeats the last letter of an adjective or verb
            Arguments.of("blyg", listOf("-gt", "-ga"), WordGroup.Adjective, listOf("blygt", "blyga")),
            Arguments.of("vit", listOf("-tt", "-a"), WordGroup.Adjective, listOf("vitt", "vita")),
            Arguments.of("sköta", listOf("-er", "-tte", "-tt"), verb, listOf("sköter", "skötte", "skött")),
            Arguments.of("blåsa", listOf("-er", "-ste", "-st"), verb, listOf("blåser", "blåste", "blåst")),
            // Ending doubles the last letter
            Arguments.of("get", listOf("-en", "-ter", "-terna"), noun, listOf("geten", "getter", "getterna")),
            Arguments.of("dum", listOf("-t", "-ma"), WordGroup.Adjective, listOf("dumt", "dumma")),
        )

        @JvmStatic
        fun reviewEdgeCases(): List<Arguments> = listOf(
            // Inconsistent plural
            Arguments.of("klass", listOf("-en", "-er", "-arna"), noun, true),
            Arguments.of("fisk", listOf("-en", "-", "-ar"), noun, true),
            // Malformed endings
            Arguments.of("block", listOf("et", "-", "-en"), noun, true),
            Arguments.of("tillfälle", listOf("-t", "-n", "- na"), noun, true),
            Arguments.of("spa", listOf("-et/-en", "-n", "-na"), noun, true),
            // Endings that don't fit the word
            Arguments.of("bil", listOf("-an", "-ar", "-arna"), noun, true),
            Arguments.of("bil", listOf("-n", "-ar", "-arna"), noun, true),
            Arguments.of("hus", listOf("-t", "-", "-en"), noun, true),
            Arguments.of("bil", listOf("-en", "-xar", "-xarna"), noun, true),
            // Irregular word
            Arguments.of("ingen", listOf("-t", "-a"), WordGroup.Adjective, true),
            // Irregular plural, but written out
            Arguments.of("examen", listOf("-", "examina", "examina"), noun, false),
            // Three equal letters in a row
            Arguments.of("vägg", listOf("-gen", "-ar", "-arna"), noun, true),
            // Unchanged plural with a full plural ending in the definite form
            Arguments.of("allergiker", listOf("-n", "-", "-erna"), noun, true),
            Arguments.of("tränare", listOf("-n", "-", "-arna"), noun, false),
            // Present without ending, written like group 1
            Arguments.of("höra", listOf("-r", "-de", "-t"), verb, true),
            Arguments.of("hyra", listOf("-r", "-de", "-t"), verb, true),
            Arguments.of("begära", listOf("-r", "-de", "-t"), verb, true),
            // Unclear if the e before l/n/r is stressed
            Arguments.of("exempel", listOf("-et", "-", "-en"), noun, true),
            Arguments.of("vapen", listOf("-et", "-", "-en"), noun, true),
            Arguments.of("himmel", listOf("-en", "-ar", "-arna"), noun, true),
            Arguments.of("nackdel", listOf("-en", "-ar", "-arna"), noun, false),
            Arguments.of("dataspel", listOf("-et", "-", "-en"), noun, false),
            Arguments.of("skorsten", listOf("-en", "-ar", "-arna"), noun, false),
            // Short piece that only matches the word by accident
            Arguments.of("kortfattad", listOf("-at", "-ade"), WordGroup.Adjective, true),
            // Repeated last letter is built safely
            Arguments.of("blyg", listOf("-gt", "-ga"), WordGroup.Adjective, false),
            Arguments.of("sköta", listOf("-er", "-tte", "-tt"), verb, false),
        )
    }
}
