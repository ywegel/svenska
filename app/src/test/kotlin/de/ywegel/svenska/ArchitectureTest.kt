package de.ywegel.svenska

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import org.junit.jupiter.api.Test

class ArchitectureTest {

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
