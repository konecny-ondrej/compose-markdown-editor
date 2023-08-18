package me.okonecny.markdowneditor

import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.html.renderer.HeaderIdGenerator
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.ast.TextCollectingVisitor
import me.tatarka.inject.annotations.Inject
import java.nio.file.Path

@Inject
class DocumentParser(
    private val flexmarkParser: Parser,
    private val headerIdGenerator: HeaderIdGenerator
) {
    fun parse(sourceText: String, basePath: Path): MarkdownDocument {
        val flexmarkDocument = flexmarkParser.parse(sourceText)
        headerIdGenerator.generateIds(flexmarkDocument)

        val references = Parser.REFERENCES.get(flexmarkDocument)
            .mapValues { (name, reference) ->
                MarkdownReference(
                    name = name,
                    url = reference.url.toString(),
                    title = reference.title?.toString()
                )
            }
            .mapKeys { (name, _) ->
                name.lowercase()
            }

        return MarkdownDocument(
            sourceText,
            flexmarkDocument,
            basePath,
            references + parseInlineReferences(flexmarkDocument.children)
        )
    }

    private fun parseInlineReferences(nodes: Iterable<Node>): Map<String, MarkdownReference> {
        if (nodes.none()) return emptyMap()
        return nodes.map { node ->
            if (node is Link && node.isAnchor && node.url?.toString() == "@") {
                val referenceName = node.unformattedText.lowercase()
                mapOf(
                    referenceName to MarkdownReference(
                        name = referenceName,
                        url = "#" + node.anchorRefId!!,
                        title = ""
                    )
                )
            } else {
                if (node.hasChildren()) {
                    parseInlineReferences(node.children)
                } else {
                    emptyMap()
                }
            }
        }.reduce { m1, m2 -> m1 + m2 }
    }
}

private val Node.unformattedText: String
    get() {
        val builder = TextCollectingVisitor()
        builder.collect(this)
        return builder.text
    }

internal val Link.isAnchor: Boolean get() = url?.toString()?.startsWith("@") ?: false

internal val Link.anchorRefId: String?
    get() {
        if (!isAnchor) return null
        val rawUrl = url?.toString() ?: "@"

        return if (rawUrl == "@") {
            HeaderIdGenerator.generateId(
                unformattedText,
                null,
                null,
                true,
                true
            )
        } else {
            rawUrl.substring(1)
        }
    }