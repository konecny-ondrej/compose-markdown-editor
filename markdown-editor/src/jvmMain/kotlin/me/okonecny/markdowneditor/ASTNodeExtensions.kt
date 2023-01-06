package me.okonecny.markdowneditor

import org.intellij.markdown.IElementType
import org.intellij.markdown.ast.ASTNode

/**
 * Extract text corresponding to the ASTNode from source text.
 */
internal fun ASTNode?.text(sourceText: String): String =
    if (this == null) "" else sourceText.substring(startOffset, endOffset)

internal fun ASTNode.findChildByType(type: IElementType): ASTNode? = children.find { node ->
    node.type == type
}