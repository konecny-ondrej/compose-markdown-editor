package me.okonecny.markdowneditor

import com.vladsch.flexmark.util.ast.Document

class MarkdownDocument(
    val sourceText: String,
    val ast: Document,
    val links: List<MarkdownLink>
)

class MarkdownLink // TODO