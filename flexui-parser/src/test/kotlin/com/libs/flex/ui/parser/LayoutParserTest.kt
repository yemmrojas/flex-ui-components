package com.libs.flex.ui.parser

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.SchemaVersion
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class LayoutParserTest {

    @Test
    fun `LayoutParser factory creates default implementation`() {
        val parser = LayoutParserFactory.create()
        assertNotNull(parser)
    }

    @Test
    fun `LayoutParser interface allows test substitution DIP`() {
        val fakeDocument = LayoutDocument.create(
            schemaVersion = SchemaVersion.CURRENT,
            root = TextNode(
                id = ComponentId.of("fake_root").fold({ error("invalid") }, { it }),
                text = "Mocked"
            )
        ).fold({ error("doc fail") }, { it })

        val fakeParser = object : LayoutParser {
            override fun parse(
                jsonString: String,
                options: ParserOptions
            ): Either<Nel<ParserError>, ParseResult> {
                return Either.Right(ParseResult(fakeDocument))
            }
        }

        val result = fakeParser.parse("""{"type":"text"}""")
        val parseResult = assertIs<Either.Right<ParseResult>>(result).value
        assertEquals("fake_root", parseResult.document.root.id.value)
    }
}
