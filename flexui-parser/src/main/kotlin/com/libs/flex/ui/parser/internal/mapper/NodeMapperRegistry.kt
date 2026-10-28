package com.libs.flex.ui.parser.internal.mapper

import com.libs.flex.ui.parser.internal.mapper.atomic.ButtonNodeMapper
import com.libs.flex.ui.parser.internal.mapper.atomic.CheckNodeMapper
import com.libs.flex.ui.parser.internal.mapper.atomic.CustomNodeMapper
import com.libs.flex.ui.parser.internal.mapper.atomic.ImageNodeMapper
import com.libs.flex.ui.parser.internal.mapper.atomic.InputNodeMapper
import com.libs.flex.ui.parser.internal.mapper.atomic.LoaderNodeMapper
import com.libs.flex.ui.parser.internal.mapper.atomic.RadioGroupNodeMapper
import com.libs.flex.ui.parser.internal.mapper.atomic.SelectNodeMapper
import com.libs.flex.ui.parser.internal.mapper.atomic.SliderNodeMapper
import com.libs.flex.ui.parser.internal.mapper.atomic.SwitchNodeMapper
import com.libs.flex.ui.parser.internal.mapper.atomic.TextNodeMapper
import com.libs.flex.ui.parser.internal.mapper.atomic.ToastNodeMapper
import com.libs.flex.ui.parser.internal.mapper.layout.CardContainerNodeMapper
import com.libs.flex.ui.parser.internal.mapper.layout.CarouselLayoutNodeMapper
import com.libs.flex.ui.parser.internal.mapper.layout.FloatingButtonLayoutNodeMapper
import com.libs.flex.ui.parser.internal.mapper.layout.GridLayoutNodeMapper
import com.libs.flex.ui.parser.internal.mapper.layout.HorizontalLayoutNodeMapper
import com.libs.flex.ui.parser.internal.mapper.layout.ListLayoutNodeMapper
import com.libs.flex.ui.parser.internal.mapper.layout.ScrollLayoutNodeMapper
import com.libs.flex.ui.parser.internal.mapper.layout.VerticalLayoutNodeMapper

/**
 * Registro centralizado indexado en memoria de todos los [NodeMapper] disponibles en FlexUI.
 *
 * Mapea cada nombre de componente oficial y sus aliases a su correspondiente implementación.
 *
 * **Diseño y Reutilización:**
 * El registro es completamente inmutable y agnóstico a las opciones de parsing ([com.libs.flex.ui.parser.ParserOptions]).
 * Cada mapper registrado es envuelto con [TolerantNodeMapperDecorator], el cual consulta dinámicamente
 * el modo (`options.mode`) en tiempo de ejecución. Esto permite que una única instancia de [NodeMapperRegistry]
 * sea compartida y reutilizada (singleton) entre múltiples llamadas de parsing concurrentes o con distintos modos.
 *
 * **Manejo de Errores al Iniciar:**
 * Las validaciones en [create] lanzan [IllegalArgumentException] o [IllegalStateException] como mecanismo
 * de "fail-fast de configuración" (errores de programación detectables al inicializar la aplicación, tales como
 * colisiones de alias o mappers sin tipos declarados), distinguiéndose de los errores de datos de entrada
 * que son gestionados funcionalmente mediante `Either`.
 */
internal class NodeMapperRegistry private constructor(
    private val mappers: Map<String, NodeMapper>
) {

    /**
     * Resuelve el mapeador asociado al tipo o alias de componente especificado.
     *
     * @param type Nombre primario o alias del componente.
     * @return El [NodeMapper] correspondiente, o null si no está registrado.
     */
    operator fun get(type: String): NodeMapper? = mappers[type]

    /**
     * Verifica si el tipo o alias de componente se encuentra registrado.
     */
    fun contains(type: String): Boolean = mappers.containsKey(type)

    /**
     * Conjunto inmutable de todos los tipos y aliases registrados.
     */
    val supportedTypes: Set<String> get() = mappers.keys

    internal companion object {

        /**
         * Crea el registro predeterminado con todos los 20 mappers atómicos y de layout del framework.
         *
         * @return Instancia inmutable y validada de [NodeMapperRegistry].
         * @throws IllegalStateException Si se detecta colisión de tipos o aliases entre mappers.
         */
        fun createDefault(): NodeMapperRegistry {
            val defaultMappers: List<NodeMapper> = listOf(
                // Mappers atómicos (12)
                TextNodeMapper(),
                ButtonNodeMapper(),
                InputNodeMapper(),
                CheckNodeMapper(),
                RadioGroupNodeMapper(),
                SelectNodeMapper(),
                SliderNodeMapper(),
                SwitchNodeMapper(),
                ImageNodeMapper(),
                LoaderNodeMapper(),
                ToastNodeMapper(),
                CustomNodeMapper(),

                // Mappers de disposición / contenedores (8)
                VerticalLayoutNodeMapper(),
                HorizontalLayoutNodeMapper(),
                ScrollLayoutNodeMapper(),
                ListLayoutNodeMapper(),
                FloatingButtonLayoutNodeMapper(),
                CarouselLayoutNodeMapper(),
                CardContainerNodeMapper(),
                GridLayoutNodeMapper()
            )

            return create(defaultMappers)
        }

        /**
         * Crea un registro personalizado a partir de una lista de mappers.
         *
         * Valida que cada mapper declare al menos un tipo y que no existan colisiones entre mappers.
         * Envuelve cada mapper con [TolerantNodeMapperDecorator] para soporte de tolerancia en tiempo de ejecución.
         *
         * @param mappers Lista de instancias de [NodeMapper].
         * @return Instancia inmutable y validada de [NodeMapperRegistry].
         * @throws IllegalArgumentException Si algún mapper declara una lista vacía de tipos.
         * @throws IllegalStateException Si se detectan colisiones de tipos entre dos o más mappers.
         */
        fun create(mappers: List<NodeMapper>): NodeMapperRegistry {
            mappers.forEach { mapper ->
                require(mapper.types.isNotEmpty()) {
                    val name = mapper::class.qualifiedName ?: mapper::class.java.name
                    "NodeMapper $name must declare at least one supported type"
                }
            }

            val collisions = mappers
                .flatMap { mapper -> mapper.types.map { type -> type to mapper } }
                .groupBy({ it.first }, { it.second })
                .filter { it.value.size > 1 }

            check(collisions.isEmpty()) {
                val details = collisions.entries.joinToString(separator = "; ") { (type, conflictingMappers) ->
                    val mapperNames = conflictingMappers.joinToString { it::class.qualifiedName ?: it::class.java.name }
                    "'$type' declared by [$mapperNames]"
                }
                "Duplicate type registration collisions detected: $details"
            }

            val registryMap = buildMap {
                for (mapper in mappers) {
                    val decorated = mapper as? TolerantNodeMapperDecorator ?: TolerantNodeMapperDecorator(mapper)
                    for (type in mapper.types) {
                        put(type, decorated)
                    }
                }
            }

            return NodeMapperRegistry(registryMap)
        }
    }
}
