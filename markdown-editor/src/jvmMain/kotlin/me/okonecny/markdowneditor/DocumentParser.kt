package me.okonecny.markdowneditor

import me.tatarka.inject.annotations.Inject
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.parser.MarkdownParser

@Inject
class DocumentParser(
    private val markdownParser: MarkdownParser
) {
    fun parse(sourceText: String): Document {
        return MarkdownDocument(
            sourceText,
            parse(markdownParser.buildMarkdownTreeFromString(sourceText)),
            listOf() // TODO: parse links
        )
    }

    fun parse(rootNode: ASTNode): MdDocument {
        val mdChildren = mutableListOf<MdFileChild>()
        // TODO

        return MdDocument(
            rootNode.startOffset,
            rootNode.endOffset,
            mdChildren
        )
    }
}