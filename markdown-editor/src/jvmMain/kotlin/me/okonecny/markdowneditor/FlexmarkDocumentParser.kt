package me.okonecny.markdowneditor

import com.vladsch.flexmark.parser.Parser
import me.tatarka.inject.annotations.Inject

@Inject
class FlexmarkDocumentParser(
    private val flexmarkParser: Parser
) : DocumentParser {
    override fun parse(sourceText: String): Document {
        return MarkdownDocument(
            sourceText,
            parse(flexmarkParser.parse(sourceText)),
            listOf() // TODO: parse links
        )
    }

    private fun parse(documentNode: com.vladsch.flexmark.util.ast.Document): MdDocument {
        return MdDocument(0,0, emptyList())
    }
}