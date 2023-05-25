package me.okonecny.markdowneditor

import com.vladsch.flexmark.util.ast.Document
import java.nio.file.Path

class MarkdownDocument(
    val sourceText: String,
    val ast: Document,
    val basePath: Path,
    val links: List<MarkdownLink>
)

class MarkdownLink // TODO