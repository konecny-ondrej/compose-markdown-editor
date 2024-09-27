package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.interactivetext.UserData
import me.okonecny.markdowneditor.DocumentTheme

internal class UiParagraph : BlockRenderer<Paragraph, Node> {
    @Composable
    override fun RenderContext<Node>.render(block: Paragraph) {
        val inlines = parseInlines(block.children)
        val styles = DocumentTheme.current.styles
        InteractiveText(
            interactiveId = document.getInteractiveId(block),
            text = inlines.text,
            textMapping = inlines.textMapping,
            style = styles.paragraph,
            inlineContent = inlines.inlineContent,
            activeAnnotationTags = activeAnnotationTags,
            onAnnotationCLick = handleLinks(),
            userData = UserData.of(Node::class, block)
        )
    }
}