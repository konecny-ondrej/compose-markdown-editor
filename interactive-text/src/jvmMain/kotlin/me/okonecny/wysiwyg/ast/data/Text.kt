package me.okonecny.wysiwyg.ast.data

/**
 * Represents text. Just the text and nothing more. Usually this will be the data of a leaf VisualNode.
 * Formatting will be handled by parents of the node.
 */
data class Text(
    override val text: String
) : HasText
