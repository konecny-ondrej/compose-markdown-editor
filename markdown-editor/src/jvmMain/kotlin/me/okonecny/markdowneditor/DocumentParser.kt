package me.okonecny.markdowneditor

import com.vladsch.flexmark.parser.Parser
import me.tatarka.inject.annotations.Inject

@Inject
class DocumentParser(
    private val flexmarkParser: Parser
) {
    fun parse(sourceText: String): MarkdownDocument {
        return MarkdownDocument(
            sourceText,
            flexmarkParser.parse(sourceText),
            listOf() // TODO: parse links
        )
    }
}