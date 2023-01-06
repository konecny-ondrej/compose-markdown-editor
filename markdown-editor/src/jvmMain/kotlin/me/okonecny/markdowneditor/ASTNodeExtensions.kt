package me.okonecny.markdowneditor

import org.intellij.markdown.IElementType
import org.intellij.markdown.ast.ASTNode

/**
 * Extract text corresponding to the ASTNode from source text.
 */
internal fun ASTNode.text(sourceText: String) = sourceText.substring(startOffset, endOffset)

internal fun ASTNode.getChildByType(type: IElementType): ASTNode = children.find { node ->
    node.type == type
} ?: throw MarkdownSyntaxError("Expected ${type.name}.", this)