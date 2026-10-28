package com.libs.flex.ui.parser.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

/**
 * Pruebas arquitectónicas con Konsist para el módulo `:flexui-parser`.
 *
 * Valida la separación de capas, pureza Kotlin JVM, ausencia de dependencias de Android/Compose/DI,
 * cumplimiento de visibilidad explícita y tipos de retorno (explicitApi), y desacoplamiento SRP
 * entre mapeadores de componentes atómicos.
 */
class ParserArchitectureTest {

    @Test
    fun `parser module should never depend on android or compose frameworks`() {
        Konsist
            .scopeFromProject()
            .files
            .withPackage("com.libs.flex.ui.parser..")
            .assertTrue { file ->
                file.imports.none {
                    it.name.startsWith("android.") ||
                        it.name.startsWith("androidx.") ||
                        it.name.startsWith("androidx.compose")
                }
            }
    }

    @Test
    fun `parser module should never depend on dependency injection frameworks`() {
        Konsist
            .scopeFromProject()
            .files
            .withPackage("com.libs.flex.ui.parser..")
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
    fun `production classes and interfaces should have explicit visibility modifier`() {
        Konsist
            .scopeFromProduction()
            .classesAndInterfaces()
            .withPackage("com.libs.flex.ui.parser..")
            .assertTrue { declaration ->
                declaration.hasPublicModifier ||
                    declaration.hasInternalModifier ||
                    declaration.hasPrivateModifier ||
                    declaration.hasProtectedModifier
            }
    }

    @Test
    fun `public declarations in src main should have explicit visibility modifier and return type`() {
        val prodScope = Konsist
            .scopeFromProduction()

        // 1. Todas las clases, interfaces y objetos en producción deben tener visibilidad explícita
        prodScope
            .classesAndInterfacesAndObjects()
            .withPackage("com.libs.flex.ui.parser..")
            .assertTrue { declaration ->
                declaration.hasPublicModifier ||
                    declaration.hasInternalModifier ||
                    declaration.hasPrivateModifier ||
                    declaration.hasProtectedModifier
            }

        // 2. Todas las funciones públicas deben declarar explícitamente su tipo de retorno
        prodScope
            .functions()
            .withPackage("com.libs.flex.ui.parser..")
            .filter { it.hasPublicModifier }
            .assertTrue { function ->
                function.hasReturnType()
            }

        // 3. Todas las propiedades públicas deben declarar explícitamente su tipo
        prodScope
            .properties()
            .withPackage("com.libs.flex.ui.parser..")
            .filter { it.hasPublicModifier }
            .assertTrue { property ->
                property.type != null
            }
    }

    @Test
    fun `atomic node mappers should have no direct coupling between each other`() {
        val atomicClasses = Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("com.libs.flex.ui.parser.internal.mapper.atomic..")

        val atomicClassNames = atomicClasses.map { it.name }.toSet()

        atomicClasses.assertTrue { atomicClass ->
            val otherMappers = atomicClassNames - atomicClass.name
            otherMappers.none { otherMapper ->
                val wordBoundaryRegex = Regex("\\b$otherMapper\\b")
                atomicClass.containingFile.imports.any { it.name.endsWith(otherMapper) } ||
                    wordBoundaryRegex.containsMatchIn(atomicClass.text)
            }
        }
    }
}
