package com.libs.flex.ui.parser.internal.mapper

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.model.node.UnsupportedFallbackNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserMode
import com.libs.flex.ui.parser.ParserOptions
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.fakes.FakePropertyReader
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
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.DefaultMappingScope
import com.libs.flex.ui.parser.internal.scope.MappingScope
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NodeMapperRegistryTest {

    // === TEST FIXTURE PROVIDERS (Object Mother Pattern) ===

    private fun givenDummyMapper(
        name: String = "dummy",
        declaredTypes: Set<String> = setOf("dummyType")
    ): NodeMapper = object : NodeMapper {
        override val types: Set<String> = declaredTypes

        override fun map(reader: PropertyReader, scope: MappingScope): Either<Nel<ParserError>, MappedNode> {
            val id = ComponentId.of(name).getOrNull()!!
            return Either.Right(MappedNode(TextNode(id = id, text = name)))
        }
    }

    private fun givenScope(
        mode: ParserMode,
        warnings: MutableList<ParserWarning> = mutableListOf()
    ): DefaultMappingScope = DefaultMappingScope(
        path = "root.button",
        depth = 1,
        options = ParserOptions(mode = mode),
        nodeResolver = { _, _ -> Either.Left(Nel.of(ParserError.UnknownComponentType("root", "unknown"))) },
        onWarn = { warnings.add(it) }
    )

    // =========================================================================
    // Tests
    // =========================================================================

    @Test
    fun `createDefault registers all 20 built-in mappers without collisions and resolves all types`() {
        val registry = NodeMapperRegistry.createDefault()

        // 1. Text
        assertNotNull(registry["flexText"])
        assertNotNull(registry["componentTextView"])
        assertNotNull(registry["text"])

        // 2. Button
        assertNotNull(registry["flexButton"])
        assertNotNull(registry["componentButton"])
        assertNotNull(registry["button"])

        // 3. Input
        assertNotNull(registry["flexInput"])
        assertNotNull(registry["input"])
        assertNotNull(registry["componentTextInput"])
        assertNotNull(registry["componentInput"])

        // 4. Check
        assertNotNull(registry["flexCheck"])
        assertNotNull(registry["checkbox"])
        assertNotNull(registry["componentCheckbox"])
        assertNotNull(registry["componentCheck"])
        assertNotNull(registry["check"])

        // 5. RadioGroup
        assertNotNull(registry["flexRadioGroup"])
        assertNotNull(registry["radioGroup"])
        assertNotNull(registry["componentRadioGroup"])
        assertNotNull(registry["radio"])

        // 6. Select
        assertNotNull(registry["flexSelect"])
        assertNotNull(registry["select"])
        assertNotNull(registry["dropdown"])
        assertNotNull(registry["componentSelect"])

        // 7. Slider
        assertNotNull(registry["flexSliderCheck"])
        assertNotNull(registry["componentSliderCheck"])
        assertNotNull(registry["slider"])

        // 8. Switch
        assertNotNull(registry["flexSwitch"])
        assertNotNull(registry["switch"])
        assertNotNull(registry["toggle"])
        assertNotNull(registry["componentSwitch"])

        // 9. Image
        assertNotNull(registry["flexImage"])
        assertNotNull(registry["image"])
        assertNotNull(registry["componentImageView"])
        assertNotNull(registry["componentImage"])

        // 10. Loader
        assertNotNull(registry["flexLoader"])
        assertNotNull(registry["componentLoader"])
        assertNotNull(registry["loader"])

        // 11. Toast
        assertNotNull(registry["flexToast"])
        assertNotNull(registry["componentToast"])
        assertNotNull(registry["toast"])

        // 12. Custom
        assertNotNull(registry["flexCustom"])
        assertNotNull(registry["custom"])

        // 13. VerticalLayout
        assertNotNull(registry["flexVertical"])
        assertNotNull(registry["contentVertical"])
        assertNotNull(registry["vertical"])

        // 14. HorizontalLayout
        assertNotNull(registry["flexHorizontal"])
        assertNotNull(registry["contentHorizontal"])
        assertNotNull(registry["horizontal"])

        // 15. ScrollLayout
        assertNotNull(registry["flexScroll"])
        assertNotNull(registry["contentScroll"])
        assertNotNull(registry["scroll"])

        // 16. ListLayout
        assertNotNull(registry["flexList"])
        assertNotNull(registry["contentList"])
        assertNotNull(registry["list"])
        assertNotNull(registry["componentListView"])

        // 17. FloatingButtonLayout
        assertNotNull(registry["flexFloatingButton"])
        assertNotNull(registry["contentWithFloatingButton"])

        // 18. CarouselLayout
        assertNotNull(registry["flexCarousel"])
        assertNotNull(registry["flexSlider"])
        assertNotNull(registry["contentSlider"])
        assertNotNull(registry["imageSlider"])

        // 19. CardContainer
        assertNotNull(registry["flexCard"])
        assertNotNull(registry["cardContainer"])
        assertNotNull(registry["card"])

        // 20. GridLayout
        assertNotNull(registry["flexGrid"])
        assertNotNull(registry["grid"])

        // Verify that every single mapper's declared types are in supportedTypes
        val allExpectedMappers = listOf(
            TextNodeMapper(), ButtonNodeMapper(), InputNodeMapper(), CheckNodeMapper(),
            RadioGroupNodeMapper(), SelectNodeMapper(), SliderNodeMapper(), SwitchNodeMapper(),
            ImageNodeMapper(), LoaderNodeMapper(), ToastNodeMapper(), CustomNodeMapper(),
            VerticalLayoutNodeMapper(), HorizontalLayoutNodeMapper(), ScrollLayoutNodeMapper(),
            ListLayoutNodeMapper(), FloatingButtonLayoutNodeMapper(), CarouselLayoutNodeMapper(),
            CardContainerNodeMapper(), GridLayoutNodeMapper()
        )
        for (m in allExpectedMappers) {
            assertTrue(registry.supportedTypes.containsAll(m.types))
        }
    }

    @Test
    fun `same registry instance is safely reused across STRICT and TOLERANT parse calls`() {
        // Un solo registro inmutable y sin estado de ParserOptions
        val registry = NodeMapperRegistry.createDefault()
        val buttonMapper = registry["flexButton"]
        assertNotNull(buttonMapper)

        // Invalid reader with blank actionId to trigger mapping failure
        val invalidReader = FakePropertyReader(
            path = "root.button",
            id = "btn_1",
            strings = mapOf("text" to "Click me", "actionId" to "   ")
        )

        // 1. Invocation with STRICT options: returns Left (failure not suppressed)
        val strictWarnings = mutableListOf<ParserWarning>()
        val strictScope = givenScope(ParserMode.STRICT, strictWarnings)
        val strictResult = buttonMapper.map(invalidReader, strictScope)
        assertIs<Either.Left<Nel<ParserError>>>(strictResult)
        assertTrue(strictWarnings.isEmpty())

        // 2. Invocation with TOLERANT options: same mapper returns Right (fallback generated, warning recorded)
        val tolerantWarnings = mutableListOf<ParserWarning>()
        val tolerantScope = givenScope(ParserMode.TOLERANT, tolerantWarnings)
        val tolerantResult = buttonMapper.map(invalidReader, tolerantScope)
        val right = assertIs<Either.Right<MappedNode>>(tolerantResult)
        assertIs<UnsupportedFallbackNode>(right.value.node)
        assertEquals(1, tolerantWarnings.size)
    }

    @Test
    fun `create detects multiple type collisions and reports all conflicting types and mappers`() {
        val mapper1 = givenDummyMapper("mapper1", setOf("shared_1", "shared_2", "unique_1"))
        val mapper2 = givenDummyMapper("mapper2", setOf("shared_1", "unique_2"))
        val mapper3 = givenDummyMapper("mapper3", setOf("shared_2", "unique_3"))

        val exception = assertThrows<IllegalStateException> {
            NodeMapperRegistry.create(listOf(mapper1, mapper2, mapper3))
        }

        val message = exception.message.orEmpty()
        assertTrue(message.contains("shared_1"))
        assertTrue(message.contains("shared_2"))
        assertTrue(message.contains("Duplicate type registration collisions detected"))
    }

    @Test
    fun `create throws IllegalArgumentException when mapper declares empty supported types`() {
        val emptyTypesMapper = givenDummyMapper("emptyMapper", emptySet())

        val exception = assertThrows<IllegalArgumentException> {
            NodeMapperRegistry.create(listOf(emptyTypesMapper))
        }

        val message = exception.message.orEmpty()
        assertTrue(message.contains("must declare at least one supported type"))
    }

    @Test
    fun `create allows registering custom third-party mappers adhering to OCP`() {
        val customMapper = givenDummyMapper("custom_card", setOf("myCustomCard", "custom_card_alias"))
        val registry = NodeMapperRegistry.create(listOf(customMapper))

        assertEquals(customMapper, (registry["myCustomCard"] as TolerantNodeMapperDecorator).delegate)
        assertEquals(customMapper, (registry["custom_card_alias"] as TolerantNodeMapperDecorator).delegate)
        assertTrue(registry.contains("myCustomCard"))
        assertTrue(registry.contains("custom_card_alias"))
        assertEquals(2, registry.supportedTypes.size)
    }

    @Test
    fun `get returns null and contains returns false for unknown component type`() {
        val registry = NodeMapperRegistry.createDefault()

        assertNull(registry["nonExistentType"])
        assertFalse(registry.contains("nonExistentType"))
    }
}
