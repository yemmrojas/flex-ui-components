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

    @Test
    fun `core module should never depend on dependency injection frameworks`() {
        Konsist
            .scopeFromProject()
            .files
            .withPackage("com.libs.flex.ui.core..")
            .assertTrue { file ->
                file.imports.none {
                    it.name.startsWith("dagger.") ||
                        it.name.startsWith("com.google.dagger.") ||
                        it.name.startsWith("javax.inject.") ||
                        it.name.startsWith("hilt.") ||
                        it.name.startsWith("koin.") ||
                        it.name.startsWith("org.koin.")
                }
            }
    }

    @Test
    fun `core production classes and interfaces should have explicit visibility modifier`() {
        Konsist
            .scopeFromProduction()
            .classesAndInterfaces()
            .withPackage("com.libs.flex.ui.core..")
            .assertTrue { declaration ->
                declaration.hasPublicModifier ||
                    declaration.hasInternalModifier ||
                    declaration.hasPrivateModifier ||
                    declaration.hasProtectedModifier
            }
    }
}
