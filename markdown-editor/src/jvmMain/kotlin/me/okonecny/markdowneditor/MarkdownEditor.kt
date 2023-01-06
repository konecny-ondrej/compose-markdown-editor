package me.okonecny.markdowneditor

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.res.useResource
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

/**
 * A simple WYSIWYG editor for Markdown.
 */
@Composable
fun MarkdownEditor(documentTheme: DocumentTheme) {
    val markdown = remember { MarkdownEditorComponent::class.create() }
    val parser = remember { markdown.parser }

    val sourceText = remember {
        useResource("/short.md") { md ->
            md.bufferedReader().readText()
        }
    }
    val markdownRoot = remember { parser.buildMarkdownTreeFromString(sourceText) }

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme
    ) {
        RenderedNode(markdownRoot, sourceText)
    }
}

/**
 * Component to render any Markdown Element.
 * @see MarkdownElementTypes
 * @see MarkdownTokenTypes
 * @see GFMElementTypes
 * @see GFMTokenTypes
 */
@Composable
private fun RenderedNode(rootNode: ASTNode, sourceText: String) {
    when (rootNode.type) {
        MarkdownElementTypes.MARKDOWN_FILE -> File(rootNode, sourceText)
        MarkdownElementTypes.ATX_1 -> AtxH1(rootNode, sourceText)
    }
}

@Composable
private fun File(fileNode: ASTNode, sourceText: String) {
    fileNode.children.forEach { node -> RenderedNode(node, sourceText) }
}

@Composable
private fun AtxH1(h1Node: ASTNode, sourceText: String) {
    val headerContent = h1Node.getChildByType(MarkdownTokenTypes.ATX_CONTENT)

    Text(headerContent.text(sourceText), style = DocumentTheme.current.styles.h1)
}