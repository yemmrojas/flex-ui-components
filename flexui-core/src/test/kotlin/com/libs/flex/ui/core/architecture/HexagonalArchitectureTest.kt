package com.libs.flex.ui.core.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class HexagonalArchitectureTest {

    @Test
    fun `domain layer should never depend on application layer`() {
        Konsist
            .scopeFromProject()
            .files
            .withPackage("com.libs.flex.ui.core.domain..")
            .assertTrue { file ->
                file.imports.none { it.name.startsWith("com.libs.flex.ui.core.application") }
            }
    }

    @Test
    fun `domain layer should never depend on infrastructure layer`() {
        Konsist
            .scopeFromProject()
            .files
            .withPackage("com.libs.flex.ui.core.domain..")
            .assertTrue { file ->
                file.imports.none { it.name.startsWith("com.libs.flex.ui.core.infrastructure") }
            }
    }

    @Test
    fun `core module should never depend on android or compose frameworks`() {
        Konsist
            .scopeFromProject()
            .files
            .withPackage("com.libs.flex.ui.core..")
            .assertTrue { file ->
                file.imports.none {
                    it.name.startsWith("android.") ||
                        it.name.startsWith("androidx.") ||
                        it.name.startsWith("androidx.compose")
                }
            }
    }
}
