package de.ywegel.svenska

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withoutPackage
import org.junit.jupiter.api.Test

class ArchitectureTest {

    // Domain and UI address a quiz card as vocabulary + QuizFacet or QuizTranslationDirection. The card tables and the
    // mapping to FsrsKt are storage details. Later, as core:database and core:data, Gradle enforces this boundary.
    @Test
    fun `quiz entities and their mapping stay inside the data layer`() {
        val forbidden = listOf("de.ywegel.svenska.data.db.quiz.", "de.ywegel.svenska.data.impl.quiz.")
        Konsist.scopeFromProduction()
            .files
            .withoutPackage("de.ywegel.svenska.data.db..", "de.ywegel.svenska.data.impl..")
            .forEach { file ->
                file.imports.forEach { import ->
                    check(forbidden.none { import.name.startsWith(it) }) {
                        "${file.path} imports '${import.name}' - quiz entities and their mapping may only be used in " +
                            "data/db and data/impl. Use QuizFacet and the models from data/model/quiz instead."
                    }
                }
            }
    }

    @Test
    fun `data does not depend on domain`() {
        checkNoImports(layer = "data", forbidden = "domain", hint = "Move the shared type into data/ instead.")
    }

    @Test
    fun `data does not depend on ui`() {
        checkNoImports(layer = "data", forbidden = "ui", hint = "Move the shared type into data/ instead.")
    }

    @Test
    fun `domain does not depend on ui`() {
        checkNoImports(
            layer = "domain",
            forbidden = "ui",
            hint = "Pass plain values instead, or move the shared type into domain/.",
        )
    }

    private fun checkNoImports(layer: String, forbidden: String, hint: String) {
        Konsist.scopeFromProduction()
            .files
            .withPackage("de.ywegel.svenska.$layer..")
            .forEach { file ->
                file.imports.forEach { import ->
                    check(!import.name.startsWith("de.ywegel.svenska.$forbidden.")) {
                        "${file.path} imports '${import.name}' - $layer/ must not depend on $forbidden/. $hint"
                    }
                }
            }
    }
}
