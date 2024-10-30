package me.okonecny.wysiwyg.ast

import java.nio.file.Path

/**
 * Parses the input into our internal AST represented by VisualNode instances.
 */
interface Parser<in IN, Document> {
    fun parse(input: IN, basePath: Path): VisualNode<Document, Document>
}