package me.okonecny.markdowneditor.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vladsch.flexmark.ast.ThematicBreak
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.DocumentTheme

internal class UiHorizontalRule : BlockRenderer<ThematicBreak, Node> {
    @Composable
    override fun RenderContext<Node>.render(block: ThematicBreak) {
        val lineStyle = DocumentTheme.current.lineStyle
        Box(
            modifier = Modifier.fillMaxWidth(1f)
                .then(Modifier.height(lineStyle.width * 2))
                .then(Modifier.border(lineStyle))
        )
    }
}