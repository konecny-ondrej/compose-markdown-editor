package me.okonecny.markdowneditor.ast

import me.okonecny.markdowneditor.ast.data.Heading
import me.okonecny.wysiwyg.ast.VisualNode

interface AnchorNameGenerator {
    fun generateAnchorName(nodePlainText: String): String
    fun <D : Any> generateAnchorName(node: VisualNode<Heading, D>): String
}