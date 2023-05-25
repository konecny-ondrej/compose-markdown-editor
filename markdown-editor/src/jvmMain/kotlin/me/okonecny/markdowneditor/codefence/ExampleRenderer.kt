package me.okonecny.markdowneditor.codefence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.okonecny.interactivetext.DisabledInteractiveContainer
import me.okonecny.markdowneditor.CodeFenceRenderer
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.MarkdownView
import java.nio.file.Path

/**
 * Renders "example" fenced code block from commonMarkSpec.md.
 */
class ExampleRenderer : CodeFenceRenderer {
    override val codeFenceType: String = "example"

    @Composable
    override fun render(code: MappedText, basePath: Path) {
        val styles = DocumentTheme.current.styles
        val lines = code.text.text.lines()
        val splitterLineNo = lines.indexOfFirst { line -> line.trim() == "." }
        val markdownCode = lines
            .subList(0, splitterLineNo.coerceAtLeast(0))
            .joinToString(System.lineSeparator())
            .trim()
        val output = lines
            .subList((splitterLineNo + 1).coerceAtMost(lines.lastIndex), lines.size)
            .joinToString(System.lineSeparator())
            .trim()

        Column(
            modifier = Modifier.padding(0.dp, 10.dp)
        ) {
            Text(
                text = "Example",
                style = styles.h6
            )
            Row(
                modifier = Modifier.height(IntrinsicSize.Max)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .background(Color(0xAAAAAAFF))
                ) {
                    DisabledInteractiveContainer {
                        MarkdownView(markdownCode, basePath, scrollable = false)
                    }
                }
                Text(
                    text = output,
                    style = styles.codeBlock.textStyle,
                    modifier = Modifier
                        .background(Color(0xAAAAFFAA))
                        .weight(0.5f)
                        .fillMaxHeight()
                )
            }
        }
    }
}