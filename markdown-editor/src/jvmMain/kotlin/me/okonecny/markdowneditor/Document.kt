package me.okonecny.markdowneditor

import com.vladsch.flexmark.util.ast.Document
import java.nio.file.Path

class MarkdownDocument(
    val sourceText: String,
    val ast: Document,
    val basePath: Path,
    private val references: Map<String, MarkdownReference>
) {
    fun resolveReference(reference: String): MarkdownReference? = references[reference.lowercase()]
}

data class MarkdownReference(
    val name: String,
    val url: String,
    val title: String?
)