package me.okonecny.markdowneditor.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.HorizontalRule
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiHorizontalRule : BlockRenderer<HorizontalRule, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<HorizontalRule>) {
        val lineStyle = DocumentTheme.current.lineStyle
        Box(
            modifier = Modifier.fillMaxWidth(1f)
                .then(Modifier.height(lineStyle.width * 2))
                .then(Modifier.border(lineStyle))
        )
    }
}