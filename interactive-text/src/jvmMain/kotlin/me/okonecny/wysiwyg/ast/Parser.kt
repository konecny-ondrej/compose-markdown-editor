package me.okonecny.wysiwyg.ast

import java.nio.file.Path

/**
 * Parses the input into our internal AST represented by VisualNode instances.
 */
interface Parser<in IN, Document : Any> {

    /**
     * Parses the whole document. There are two use cases for this method:
     * 1. Parsing of the whole document.
     * 2. Parsing of a markdown fragment when pasting from clipboard.
     */
    fun parse(input: IN, basePath: Path): VisualNode<Document, Document>
}