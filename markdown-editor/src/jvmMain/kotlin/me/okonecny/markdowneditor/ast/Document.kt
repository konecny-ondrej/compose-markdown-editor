package me.okonecny.markdowneditor.ast

import me.okonecny.markdowneditor.flexmark.MarkdownReference

interface Document {
    val anchorNameGenerator: AnchorNameGenerator
    fun resolveReference(reference: String): MarkdownReference?
}