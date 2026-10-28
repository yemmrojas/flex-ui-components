package com.libs.flex.ui.parser

import com.libs.flex.ui.core.domain.error.DomainError
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParserErrorTest {

    @Test
    fun `ParserWarning stores path code and message correctly`() {
        val warning = ParserWarning(
            path = "root.children[0]",
            code = "STYLE_DEPRECATED",
            message = "Property 'elevation' is deprecated"
        )

        assertEquals("root.children[0]", warning.path)
        assertEquals("STYLE_DEPRECATED", warning.code)
        assertEquals("Property 'elevation' is deprecated", warning.message)
    }

    @Test
    fun `InvalidJson error contains path and readable message`() {
        val error: ParserError = ParserError.InvalidJson(
            path = "root",
            reason = "Unexpected token '}' at offset 12"
        )

        assertEquals("root", error.path)
        assertTrue(error.message.contains("Unexpected token"))
    }

    @Test
    fun `PayloadTooLarge error formats size and max allowed`() {
        val error: ParserError = ParserError.PayloadTooLarge(
            sizeBytes = 2048L,
            maxBytes = 1024L
        )

        assertEquals("", error.path)
        assertTrue(error.message.contains("2048"))
        assertTrue(error.message.contains("1024"))
    }

    @Test
    fun `MaxDepthExceeded error reports current and limit`() {
        val error: ParserError = ParserError.MaxDepthExceeded(
            path = "root.children[0].children[0]",
            currentDepth = 21,
            maxDepth = 20
        )

        assertEquals("root.children[0].children[0]", error.path)
        assertTrue(error.message.contains("21"))
        assertTrue(error.message.contains("20"))
    }

    @Test
    fun `MissingRequiredProperty error reports path and property`() {
        val error: ParserError = ParserError.MissingRequiredProperty(
            path = "root.button",
            property = "text"
        )

        assertEquals("root.button", error.path)
        assertEquals("text", (error as ParserError.MissingRequiredProperty).property)
        assertTrue(error.message.contains("text"))
    }

    @Test
    fun `InvalidPropertyValue preserves DomainError Value when provided`() {
        val domainError = DomainError.Value.InvalidDimension("Negative padding")
        val error = ParserError.InvalidPropertyValue(
            path = "root.padding",
            property = "padding",
            reason = "Negative dimension",
            domainError = domainError
        )

        assertEquals("root.padding", error.path)
        assertEquals("padding", error.property)
        assertEquals("Negative dimension", error.reason)
        assertEquals(domainError, error.domainError)
    }

    @Test
    fun `InvalidPropertyValue allows null DomainError Value`() {
        val error = ParserError.InvalidPropertyValue(
            path = "root.color",
            property = "color",
            reason = "Invalid hex color format"
        )

        assertNull(error.domainError)
        assertEquals("Invalid hex color format", error.reason)
    }

    @Test
    fun `DuplicateComponentId reports duplicate id and path`() {
        val error = ParserError.DuplicateComponentId(
            path = "root.children[1]",
            id = "btn_submit"
        )

        assertEquals("root.children[1]", error.path)
        assertEquals("btn_submit", error.id)
        assertTrue(error.message.contains("btn_submit"))
    }

    @Test
    fun `UnknownComponentType reports unrecognized type and path`() {
        val error = ParserError.UnknownComponentType(
            path = "root.children[0]",
            type = "customWidget3000"
        )

        assertEquals("root.children[0]", error.path)
        assertEquals("customWidget3000", error.type)
        assertTrue(error.message.contains("customWidget3000"))
    }

    @Test
    fun `CoreStructuralError maps domain structural error to readable message without reflection`() {
        val componentId = com.libs.flex.ui.core.domain.value.ComponentId.of("root_node").fold({ error("invalid id") }, { it })
        val structuralError = DomainError.Structural.MaxNestingDepthExceeded(id = componentId, depth = 22)
        val error = ParserError.CoreStructuralError(
            path = "root",
            structuralError = structuralError
        )

        assertEquals("root", error.path)
        assertEquals(structuralError, error.structuralError)
        assertTrue(error.message.contains("22"))
        assertTrue(error.message.contains("root_node"))
    }
}
