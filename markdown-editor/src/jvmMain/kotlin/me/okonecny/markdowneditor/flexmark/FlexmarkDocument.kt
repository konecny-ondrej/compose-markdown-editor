package me.okonecny.markdowneditor.flexmark

import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.MarkdownReference
import java.nio.file.Path

data class FlexmarkDocument(
    val rootNode: Node,
    private val references: Map<String, MarkdownReference>,
    val basePath: Path
) {
    fun resolveReference(reference: String): MarkdownReference? = references[reference.lowercase()]
}
