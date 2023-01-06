package me.okonecny.markdowneditor

import org.intellij.markdown.ast.ASTNode

class MarkdownSyntaxError(message: String, wrongNode: ASTNode) : RuntimeException(
    "Syntax error at ${wrongNode.startOffset} - ${wrongNode.startOffset}: $message"
)