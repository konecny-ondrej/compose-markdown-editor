package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.interactivetext.UserData
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.LocalDocument

internal class UiHeading : BlockRenderer<Heading, Node> {
    @Composable
    override fun RenderContext<Node>.render(block: Heading) {
        val inlines = parseInlines(block.children)
        val styles = DocumentTheme.current.styles
        val document = LocalDocument.current
        InteractiveText(
            interactiveId = document.getInteractiveId(block),
            text = inlines.text,
            textMapping = inlines.textMapping,
            inlineContent = inlines.inlineContent,
            style = when (block.level) {
                1 -> styles.h1
                2 -> styles.h2
                3 -> styles.h3
                4 -> styles.h4
                5 -> styles.h5
                6 -> styles.h6
                else -> styles.h1
            },
            activeAnnotationTags = activeAnnotationTags,
            onAnnotationCLick = handleLinks(),
            userData = UserData.of(Node::class, block)
        )
    }
}