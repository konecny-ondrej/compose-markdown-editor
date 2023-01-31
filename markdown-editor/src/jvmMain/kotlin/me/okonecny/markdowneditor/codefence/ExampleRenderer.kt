package me.okonecny.markdowneditor.codefence

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.okonecny.markdowneditor.CodeFenceRenderer
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MarkdownEditor

/**
 * Renders "example" fenced code block from commonMarkSpec.md.
 */
class ExampleRenderer : CodeFenceRenderer {
    override val codeFenceType: String = "example"

    @Composable
    override fun render(code: String) {
        val styles = DocumentTheme.current.styles
        val lines = code.lines()
        val splitterLineNo = lines.indexOfFirst { line -> line.trim() == "." }
        val markdownCode = lines
            .subList(0, splitterLineNo.coerceAtLeast(0))
            .joinToString(System.lineSeparator())
        val output = lines
            .subList((splitterLineNo + 1).coerceAtMost(lines.lastIndex), lines.size)
            .joinToString(System.lineSeparator())

        Column {
            Text(
                text = "Example",
                style = styles.h6
            )
            Row {
                Box(
                    modifier = styles.codeBlock.modifier.fillMaxWidth(0.5f)
                ) {
                    MarkdownEditor(markdownCode, scrollable = false)
                }
                Box(
                    modifier = Modifier.fillMaxWidth(1f)
                ) {
                    Text(
                        text = output,
                        style = styles.codeBlock.textStyle,
                        modifier = styles.codeBlock.modifier
                    )
                }
            }
        }
    }
}