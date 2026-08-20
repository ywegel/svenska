package de.ywegel.svenska

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import org.junit.jupiter.api.Test

class ArchitectureTest {

    @Test
    fun `data does not depend on domain`() {
        Konsist.scopeFromProduction()
            .files
            .withPackage("de.ywegel.svenska.data..")
            .forEach { file ->
                file.imports.forEach { import ->
                    check(!import.name.startsWith("de.ywegel.svenska.domain")) {
                        "${file.path} imports '${import.name}' - data/ must not depend on domain/. " +
                            "Move the shared type into data/ instead."
                    }
                }
            }
    }
}
