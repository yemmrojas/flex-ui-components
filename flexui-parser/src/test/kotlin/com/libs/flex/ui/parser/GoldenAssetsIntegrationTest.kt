package com.libs.flex.ui.parser

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.node.ButtonNode
import com.libs.flex.ui.core.domain.model.node.CheckNode
import com.libs.flex.ui.core.domain.model.node.FloatingButtonLayoutNode
import com.libs.flex.ui.core.domain.model.node.HorizontalLayoutNode
import com.libs.flex.ui.core.domain.model.node.ImageNode
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.model.node.ListLayoutNode
import com.libs.flex.ui.core.domain.model.node.ScrollLayoutNode
import com.libs.flex.ui.core.domain.model.node.SelectNode
import com.libs.flex.ui.core.domain.model.node.SliderLayoutNode
import com.libs.flex.ui.core.domain.model.node.SliderNode
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.value.ComponentId
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Pruebas de caracterización e integración con los 5 Golden Assets del sistema.
 *
 * Valida que los JSONs reales de producción ([sample_form.json], [sample_complex.json],
 * [sample_list.json], [sample_floating_button.json], [sample_slider.json]) se deserializan,
 * validan y construyen árboles inmutables de `:flexui-core` con 0 errores y completa integridad de tipos.
 */
class GoldenAssetsIntegrationTest {

    // === MÉTODOS PROVEEDORES (Object Mother Pattern) ===

    private fun givenParser(): LayoutParser = LayoutParserFactory.create()

    private fun givenStrictOptions(): ParserOptions = ParserOptions(mode = ParserMode.STRICT)

    private fun givenTolerantOptions(): ParserOptions = ParserOptions(mode = ParserMode.TOLERANT)

    private val goldenFiles = listOf(
        "sample_form.json",
        "sample_complex.json",
        "sample_list.json",
        "sample_floating_button.json",
        "sample_slider.json"
    )

    private fun givenAssetContent(fileName: String): String {
        val stream = javaClass.getResourceAsStream("/assets/$fileName")
            ?: javaClass.getResourceAsStream("/$fileName")
            ?: Thread.currentThread().contextClassLoader.getResourceAsStream("assets/$fileName")
            ?: Thread.currentThread().contextClassLoader.getResourceAsStream(fileName)

        if (stream != null) {
            return stream.bufferedReader().use { it.readText() }
        }

        val candidatePaths = listOf(
            "src/test/resources/assets/$fileName",
            "flexui-parser/src/test/resources/assets/$fileName",
            "../app/src/main/assets/$fileName",
            "app/src/main/assets/$fileName"
        )
        for (path in candidatePaths) {
            val file = File(path)
            if (file.exists()) {
                return file.readText()
            }
        }

        throw IllegalArgumentException("No se pudo encontrar el asset: $fileName")
    }

    private fun givenComponentId(id: String): ComponentId =
        ComponentId.of(id).fold({ error("ComponentId inválido: $id") }, { it })

    // === PRUEBAS GENERALES DE CARACTERIZACIÓN (STRICT y TOLERANT) ===

    @Test
    fun `parse parses all 5 golden assets in STRICT mode with zero errors`() {
        val parser = givenParser()
        val options = givenStrictOptions()

        for (fileName in goldenFiles) {
            val json = givenAssetContent(fileName)
            val result = parser.parse(json, options)

            val parseResult = assertIs<Either.Right<ParseResult>>(
                result,
                message = "Se esperaba éxito al parsear $fileName en modo STRICT"
            ).value

            assertNotNull(parseResult.document, "Documento nulo en $fileName")
            assertNotNull(parseResult.document.root, "Raíz nula en $fileName")
            assertTrue(parseResult.document.root.id.value.isNotBlank())
            assertTrue(parseResult.warnings.isEmpty(), "No se esperaban advertencias en modo STRICT para $fileName")
        }
    }

    @Test
    fun `parse parses all 5 golden assets in TOLERANT mode with zero errors`() {
        val parser = givenParser()
        val options = givenTolerantOptions()

        for (fileName in goldenFiles) {
            val json = givenAssetContent(fileName)
            val result = parser.parse(json, options)

            val parseResult = assertIs<Either.Right<ParseResult>>(
                result,
                message = "Se esperaba éxito al parsear $fileName en modo TOLERANT"
            ).value

            assertNotNull(parseResult.document, "Documento nulo en $fileName")
            assertNotNull(parseResult.document.root, "Raíz nula en $fileName")
        }
    }

    // === PRUEBAS DETALLADAS DE CADA GOLDEN ASSET ===

    @Test
    fun `sample_form parses complete form structure with all input controls and buttons`() {
        // Arrange
        val parser = givenParser()
        val json = givenAssetContent("sample_form.json")

        // Act
        val result = parser.parse(json, givenStrictOptions())

        // Assert
        val parseResult = assertIs<Either.Right<ParseResult>>(result).value
        val document = parseResult.document

        // Raíz es un scroll vertical con id "main_screen"
        val root = assertIs<ScrollLayoutNode>(document.root)
        assertEquals("main_screen", root.id.value)
        assertEquals("vertical", root.orientation)
        assertEquals(2, root.children.size)

        // Hijo 1: Header de texto
        val header = assertIs<TextNode>(root.children[0])
        assertEquals("header", header.id.value)
        assertEquals("FlexUI Demo - User Registration", header.text)

        // Hijo 2: Contenedor vertical del formulario
        val formContainer = assertIs<VerticalLayoutNode>(root.children[1])
        assertEquals("form_container", formContainer.id.value)
        assertEquals(6, formContainer.children.size)

        // Controles de formulario
        val nameInput = assertIs<InputNode>(formContainer.children[0])
        assertEquals("name_input", nameInput.id.value)
        assertEquals("Full Name", nameInput.label)

        val emailInput = assertIs<InputNode>(formContainer.children[1])
        assertEquals("email_input", emailInput.id.value)
        assertEquals("Email Address", emailInput.label)

        val ageSlider = assertIs<SliderNode>(formContainer.children[2])
        assertEquals("age_slider", ageSlider.id.value)
        assertEquals(18.0, ageSlider.range.min)
        assertEquals(100.0, ageSlider.range.max)

        val countrySelect = assertIs<SelectNode>(formContainer.children[3])
        assertEquals("country_select", countrySelect.id.value)
        assertEquals("Country", countrySelect.label)
        assertEquals(5, countrySelect.options.size)
        val firstOption = countrySelect.options.head
        assertEquals("United States", firstOption.label)
        assertEquals("us", firstOption.value)

        val subscribeCheck = assertIs<CheckNode>(formContainer.children[4])
        assertEquals("subscribe_check", subscribeCheck.id.value)
        assertEquals("Subscribe to newsletter", subscribeCheck.label)
        assertEquals(false, subscribeCheck.defaultChecked)

        // Botones de acción horizontal
        val buttonsContainer = assertIs<HorizontalLayoutNode>(formContainer.children[5])
        assertEquals("buttons_container", buttonsContainer.id.value)
        assertEquals(2, buttonsContainer.children.size)

        val cancelButton = assertIs<ButtonNode>(buttonsContainer.children[0])
        assertEquals("cancel_button", cancelButton.id.value)
        assertEquals("Cancel", cancelButton.text)
        assertEquals("cancel_action", cancelButton.actionId.value)

        val submitButton = assertIs<ButtonNode>(buttonsContainer.children[1])
        assertEquals("submit_button", submitButton.id.value)
        assertEquals("Submit", submitButton.text)
        assertEquals("submit_action", submitButton.actionId.value)

        // Búsqueda O(1) por ID en el árbol del LayoutDocument
        assertEquals(nameInput, document.findNode(givenComponentId("name_input")))
        assertEquals(submitButton, document.findNode(givenComponentId("submit_button")))
    }

    @Test
    fun `sample_complex parses multi-section layout with hero, feature cards and demo form`() {
        // Arrange
        val parser = givenParser()
        val json = givenAssetContent("sample_complex.json")

        // Act
        val result = parser.parse(json, givenStrictOptions())

        // Assert
        val parseResult = assertIs<Either.Right<ParseResult>>(result).value
        val document = parseResult.document

        val root = assertIs<ScrollLayoutNode>(document.root)
        assertEquals("complex_screen", root.id.value)
        assertEquals(3, root.children.size)

        // Sección 1: Hero
        val hero = assertIs<VerticalLayoutNode>(root.children[0])
        assertEquals("hero_section", hero.id.value)
        assertEquals(2, hero.children.size)
        val heroTitle = assertIs<TextNode>(hero.children[0])
        assertEquals("hero_title", heroTitle.id.value)
        assertEquals("Welcome to FlexUI", heroTitle.text)

        // Sección 2: Features
        val features = assertIs<VerticalLayoutNode>(root.children[1])
        assertEquals("features_section", features.id.value)

        // Sección 3: Demo
        val demo = assertIs<VerticalLayoutNode>(root.children[2])
        assertEquals("demo_section", demo.id.value)
        assertEquals(4, demo.children.size)

        val demoButton = assertIs<ButtonNode>(demo.children[3])
        assertEquals("demo_button", demoButton.id.value)
        assertEquals("Get Started", demoButton.text)
        assertEquals("get_started", demoButton.actionId.value)

        // Indexación por ID verificada
        assertNotNull(document.findNode(givenComponentId("feature_1_icon")))
        assertNotNull(document.findNode(givenComponentId("demo_input")))
    }

    @Test
    fun `sample_list parses lazy list extracting itemTemplate into document templates`() {
        // Arrange
        val parser = givenParser()
        val json = givenAssetContent("sample_list.json")

        // Act
        val result = parser.parse(json, givenStrictOptions())

        // Assert
        val parseResult = assertIs<Either.Right<ParseResult>>(result).value
        val document = parseResult.document

        val root = assertIs<VerticalLayoutNode>(document.root)
        assertEquals("list_screen", root.id.value)
        assertEquals(2, root.children.size)

        val header = assertIs<TextNode>(root.children[0])
        assertEquals("list_header", header.id.value)
        assertEquals("Product List Demo", header.text)

        val productList = assertIs<ListLayoutNode>(root.children[1])
        assertEquals("product_list", productList.id.value)
        assertEquals(5, productList.itemsCount)
        assertEquals("list_item", productList.itemTemplateId?.value)

        // Plantilla extraída y registrada en document.templates
        assertEquals(1, document.templates.size)
        val template = document.templates[givenComponentId("list_item")]
        assertNotNull(template)
        val horizontalTemplate = assertIs<HorizontalLayoutNode>(template)
        assertEquals("list_item", horizontalTemplate.id.value)
        assertEquals(2, horizontalTemplate.children.size)

        val itemName = assertIs<TextNode>(horizontalTemplate.children[0])
        assertEquals("item_name", itemName.id.value)
        assertEquals("{{name}}", itemName.text)

        val itemPrice = assertIs<TextNode>(horizontalTemplate.children[1])
        assertEquals("item_price", itemPrice.id.value)
        assertEquals("{{price}}", itemPrice.text)
    }

    @Test
    fun `sample_floating_button parses FAB layout with icon, actionId and nested scrollable content`() {
        // Arrange
        val parser = givenParser()
        val json = givenAssetContent("sample_floating_button.json")

        // Act
        val result = parser.parse(json, givenStrictOptions())

        // Assert
        val parseResult = assertIs<Either.Right<ParseResult>>(result).value
        val document = parseResult.document

        val root = assertIs<FloatingButtonLayoutNode>(document.root)
        assertEquals("fab_screen", root.id.value)
        assertEquals("add", root.fabIcon)
        assertEquals("add_item", root.actionId.value)
        assertTrue(root.isEnabled)
        assertEquals(1, root.children.size)

        val scrollContent = assertIs<ScrollLayoutNode>(root.children[0])
        assertEquals("fab_content", scrollContent.id.value)
        assertEquals(3, scrollContent.children.size)

        val fabHeader = assertIs<TextNode>(scrollContent.children[0])
        assertEquals("fab_header", fabHeader.id.value)
        assertEquals("Floating Action Button Demo", fabHeader.text)

        val contentSection = assertIs<VerticalLayoutNode>(scrollContent.children[2])
        assertEquals("content_section", contentSection.id.value)
        assertEquals(3, contentSection.children.size)
    }

    @Test
    fun `sample_slider parses carousel layout generating image nodes from items URLs`() {
        // Arrange
        val parser = givenParser()
        val json = givenAssetContent("sample_slider.json")

        // Act
        val result = parser.parse(json, givenStrictOptions())

        // Assert
        val parseResult = assertIs<Either.Right<ParseResult>>(result).value
        val document = parseResult.document

        val root = assertIs<VerticalLayoutNode>(document.root)
        assertEquals("slider_screen", root.id.value)
        assertEquals(3, root.children.size)

        val imageSlider = assertIs<SliderLayoutNode>(root.children[1])
        assertEquals("image_slider", imageSlider.id.value)
        assertTrue(imageSlider.autoPlay)
        assertEquals(3000L, imageSlider.intervalMs)

        // Se deben haber generado 4 nodos ImageNode a partir del array de URLs
        assertEquals(4, imageSlider.children.size)
        val firstImage = assertIs<ImageNode>(imageSlider.children[0])
        assertEquals("https://picsum.photos/800/400?random=1", firstImage.url)
        assertEquals("image_slider_item_0", firstImage.id.value)

        val fourthImage = assertIs<ImageNode>(imageSlider.children[3])
        assertEquals("https://picsum.photos/800/400?random=4", fourthImage.url)
        assertEquals("image_slider_item_3", fourthImage.id.value)
    }
}
