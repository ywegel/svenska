package de.ywegel.svenska.data.db

import de.ywegel.svenska.data.model.WordGroup
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class WordGroupConverterTest {
    private val converter = WordGroupConverter()

    // Tests for toString
    @Test
    fun `toString converts Verb with subgroup correctly`() {
        val wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_1)
        val result = converter.toString(wordGroup)
        expectThat(result).isEqualTo("VERB:GROUP_1")
    }

    @Test
    fun `toString converts Noun with subgroup correctly`() {
        val wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.OR)
        val result = converter.toString(wordGroup)
        expectThat(result).isEqualTo("NOUN:OR")
    }

    @Test
    fun `toString converts Adjective correctly`() {
        val wordGroup = WordGroup.Adjective
        val result = converter.toString(wordGroup)
        expectThat(result).isEqualTo("ADJECTIVE")
    }

    @Test
    fun `toString converts Other correctly`() {
        val wordGroup = WordGroup.Other
        val result = converter.toString(wordGroup)
        expectThat(result).isEqualTo("OTHER")
    }

    // Tests for fromString
    @Test
    fun `fromString converts VERB with subgroup correctly`() {
        val result = converter.fromString("VERB:GROUP_2A")
        expectThat(result).isEqualTo(WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A))
    }

    @Test
    fun `fromString converts NOUN with subgroup correctly`() {
        val result = converter.fromString("NOUN:AR")
        expectThat(result).isEqualTo(WordGroup.Noun(WordGroup.NounSubgroup.AR))
    }

    @Test
    fun `fromString converts ADJECTIVE correctly`() {
        val result = converter.fromString("ADJECTIVE")
        expectThat(result).isEqualTo(WordGroup.Adjective)
    }

    @Test
    fun `fromString converts OTHER correctly`() {
        val result = converter.fromString("OTHER")
        expectThat(result).isEqualTo(WordGroup.Other)
    }

    // Roundtrip tests (toString -> fromString)
    @Test
    fun `roundtrip Verb with subgroup`() {
        val original = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_3)
        val string = converter.toString(original)
        val result = converter.fromString(string)
        expectThat(result).isEqualTo(original)
    }

    @Test
    fun `roundtrip Noun with subgroup`() {
        val original = WordGroup.Noun(WordGroup.NounSubgroup.ER)
        val string = converter.toString(original)
        val result = converter.fromString(string)
        expectThat(result).isEqualTo(original)
    }

    @Test
    fun `roundtrip Adjective`() {
        val original = WordGroup.Adjective
        val string = converter.toString(original)
        val result = converter.fromString(string)
        expectThat(result).isEqualTo(original)
    }

    @Test
    fun `roundtrip Other`() {
        val original = WordGroup.Other
        val string = converter.toString(original)
        val result = converter.fromString(string)
        expectThat(result).isEqualTo(original)
    }

    // Tests for errors
    @Test
    fun `fromString handles invalid type by returning Other`() {
        val result = converter.fromString("INVALID:UNKNOWN")
        expectThat(result).isEqualTo(WordGroup.Other)
    }

    @Test
    fun `fromString handles malformed string by returning Other`() {
        val result = converter.fromString("abc")
        expectThat(result).isEqualTo(WordGroup.Other)
    }

    @Test
    fun `fromString handles malformed verb string by returning UNDEFINED`() {
        val result = converter.fromString("VERB")
        expectThat(result).isEqualTo(WordGroup.Verb(WordGroup.VerbSubgroup.UNDEFINED))
    }

    @Test
    fun `fromString handles malformed noun string by returning UNDEFINED`() {
        val result = converter.fromString("NOUN")
        expectThat(result).isEqualTo(WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED))
    }

    @Test
    fun `fromString handles empty string by returning Other`() {
        val result = converter.fromString("")
        expectThat(result).isEqualTo(WordGroup.Other)
    }

    @Test
    fun `fromString throws IllegalArgumentException for invalid VerbSubgroup`() {
        expectThrows<IllegalArgumentException> {
            converter.fromString("VERB:INVALID_SUBGROUP")
        }
    }

    @Test
    fun `fromString throws IllegalArgumentException for invalid NounSubgroup`() {
        expectThrows<IllegalArgumentException> {
            converter.fromString("NOUN:INVALID_SUBGROUP")
        }
    }
}
