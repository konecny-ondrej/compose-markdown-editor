package me.okonecny.markdowneditor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownElementTypes.ATX_1
import org.intellij.markdown.MarkdownElementTypes.ATX_2
import org.intellij.markdown.MarkdownElementTypes.ATX_3
import org.intellij.markdown.MarkdownElementTypes.ATX_4
import org.intellij.markdown.MarkdownElementTypes.ATX_5
import org.intellij.markdown.MarkdownElementTypes.ATX_6
import org.intellij.markdown.MarkdownElementTypes.CODE_BLOCK
import org.intellij.markdown.MarkdownElementTypes.LIST_ITEM
import org.intellij.markdown.MarkdownElementTypes.MARKDOWN_FILE
import org.intellij.markdown.MarkdownElementTypes.ORDERED_LIST
import org.intellij.markdown.MarkdownElementTypes.PARAGRAPH
import org.intellij.markdown.MarkdownElementTypes.SETEXT_1
import org.intellij.markdown.MarkdownElementTypes.SETEXT_2
import org.intellij.markdown.MarkdownElementTypes.UNORDERED_LIST
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

/**
 * A simple WYSIWYG editor for Markdown.
 */
@Composable
fun MarkdownEditor(sourceText: String, documentTheme: DocumentTheme = DocumentTheme.default) {
    val markdown = remember { MarkdownEditorComponent::class.create() }
    val parser = remember { markdown.parser }
    val markdownRoot = parser.buildMarkdownTreeFromString(sourceText)

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme
    ) {
        LazyColumn {
            items(markdownRoot.children) { node ->
                RenderedNode(node, sourceText)
            }
        }
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
private fun RenderedNode(node: ASTNode, sourceText: String) {
    val styles = DocumentTheme.current.styles
    when (node.type) {
        MARKDOWN_FILE -> File(node, sourceText)
        ATX_1 -> AtxHeader(node, sourceText, styles.h1)
        SETEXT_1 -> SetextHeader(node, sourceText, styles.h1)
        ATX_2 -> AtxHeader(node, sourceText, styles.h2)
        SETEXT_2 -> SetextHeader(node, sourceText, styles.h2)
        ATX_3 -> AtxHeader(node, sourceText, styles.h3)
        ATX_4 -> AtxHeader(node, sourceText, styles.h4)
        ATX_5 -> AtxHeader(node, sourceText, styles.h5)
        ATX_6 -> AtxHeader(node, sourceText, styles.h6)
        PARAGRAPH -> Paragraph(node, sourceText)
        CODE_BLOCK -> CodeBlock(node, sourceText)
        ORDERED_LIST -> MarkdownList(node, sourceText)
        UNORDERED_LIST -> MarkdownList(node, sourceText)
        LIST_ITEM -> MarkdownListItem(node, sourceText)
    }
}

@Composable
private fun File(fileNode: ASTNode, sourceText: String) {
    fileNode.children.forEach { node -> RenderedNode(node, sourceText) }
}

@Composable
private fun AtxHeader(headerNode: ASTNode, sourceText: String, headerStyle: TextStyle) {
    val headerContent = headerNode.findChildByType(MarkdownTokenTypes.ATX_CONTENT)
    Text(headerContent.text(sourceText).trim(), style = headerStyle)
}

@Composable
private fun SetextHeader(headerNode: ASTNode, sourceText: String, headerStyle: TextStyle) {
    val headerContent = headerNode.findChildByType(MarkdownTokenTypes.SETEXT_CONTENT)
    Text(headerContent.text(sourceText).trim(), style = headerStyle)
}

@Composable
private fun Paragraph(paragraphNode: ASTNode, sourceText: String) {
    Text(
        // TODO: Use AnnotatedString, Ignore newlines and indents.
        paragraphNode
            .text(sourceText)
            .replace(Regex("\\s+|\n+"), " "),
        style = DocumentTheme.current.styles.paragraph
    )
    // TODO: inline elements (images, links, highlights)
}

@Composable
private fun CodeBlock(blockNode: ASTNode, sourceText: String) {
    Text(
        blockNode.text(sourceText),
        style = DocumentTheme.current.styles.code
    )
}

private val ListNumber = compositionLocalOf { 0 }

@Composable
private fun MarkdownList(listNode: ASTNode, sourceText: String) {
    Column {
        var currentListItem = 0
        listNode.children.forEach { itemNode ->
            if (itemNode.type == LIST_ITEM) currentListItem++
            CompositionLocalProvider(
                ListNumber provides currentListItem
            ) {
                RenderedNode(itemNode, sourceText)
            }
        }
    }
}

@Composable
fun MarkdownListItem(itemNode: ASTNode, sourceText: String) {
    Row {
        Text(
            "${ListNumber.current}. ", // TODO: unordered
            style = DocumentTheme.current.styles.listNumber
        )
        Column {
            itemNode.children.forEach { itemContentNode ->
                RenderedNode(itemContentNode, sourceText)
            }
        }
    }
}