package me.okonecny.markdowneditor

import com.vladsch.flexmark.html.renderer.HeaderIdGenerator
import com.vladsch.flexmark.parser.Parser
import me.tatarka.inject.annotations.Inject

@Inject
class DocumentParser(
    private val flexmarkParser: Parser,
    private val headerIdGenerator: HeaderIdGenerator
) {
    fun parse(sourceText: String): MarkdownDocument {
        val flexmarkDocument = flexmarkParser.parse(sourceText)
        headerIdGenerator.generateIds(flexmarkDocument)

        return MarkdownDocument(
            sourceText,
            flexmarkDocument,
            listOf() // TODO: parse links
        )
    }
}