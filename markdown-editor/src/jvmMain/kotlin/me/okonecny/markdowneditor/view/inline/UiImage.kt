package me.okonecny.markdowneditor.view.inline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.ImageRef
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.markdowneditor.*
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import java.util.concurrent.atomic.AtomicLong

internal class UiImage : InlineRenderer<Image, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: Image): MappedText = buildMappedString {
        var imageState by rememberImageState(
            url = inlineNode.url.toString(),
            title = inlineNode.title.toString()
        )
        appendImage(inlineNode, imageState) { newState ->
            imageState = newState
        }
    }
}

internal class UiImageRef : InlineRenderer<ImageRef, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: ImageRef): MappedText = buildMappedString {
        val reference = document.resolveReference(inlineNode.reference.toString())
        var imageState by rememberImageState(
            url = reference?.url ?: "",
            title = reference?.title ?: ""
        )
        appendImage(inlineNode, imageState) { newState ->
            imageState = newState
        }
    }
}


private const val IMAGE_INLINE_ELEMENT_TYPE = "me.okonecny.markdowneditor.inline.Image"

private val imageCount =
    AtomicLong(0L) // TODO: use the node number from me.okonecny.markdowneditor.MarkdownDocument.getInteractiveId.


private data class ImageState(
    val url: String,
    val painter: Painter,
    val title: String = "",
    val loaded: Boolean = false
) {
    val imagePixelSize: Size = painter.intrinsicSize
}

@Composable
private fun rememberImageState(
    url: String,
    title: String,
    unloadedImage: Painter = LocalMarkdownEditorComponent.current.imageLoader.unloadedImage(
        url,
        LocalDocument.current.basePath
    ) ?: painterResource("/image-load.svg")
): MutableState<ImageState> {
    return rememberSaveable(url, title) {
        mutableStateOf(
            ImageState(
                url = url,
                painter = unloadedImage,
                title = title
            )
        )
    }
}

@Composable
private fun UiImage(
    imageState: ImageState,
    onStateChange: (newState: ImageState) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!imageState.loaded) {
        val editorComponent = LocalMarkdownEditorComponent.current
        val basePath = LocalDocument.current.basePath
        val failedImage = painterResource("/image-failed.svg")
        LaunchedEffect(Unit) {
            onStateChange(
                imageState.copy(
                    painter = try {
                        editorComponent.imageLoader.load(imageState.url, basePath)
                    } catch (e: Exception) {
                        Logger.e(e) { "Failed to load image." }
                        failedImage
                    },
                    loaded = true
                )
            )
        }
    }

    val style = DocumentTheme.current.styles.image
    Box(
        modifier = style.modifier
    ) {
        androidx.compose.foundation.Image(
            painter = imageState.painter,
            contentDescription = if (imageState.loaded) "Unloaded image" else imageState.title, // TODO: i18n
            modifier = modifier.size(imageState.imagePixelSize.dp),
            contentScale = ContentScale.FillBounds
        )
        if (imageState.title.isNotBlank()) {
            Text(
                text = imageState.title,
                style = style.title.textStyle,
                modifier = style.title.modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private val Size.dp: DpSize get() = DpSize(width.dp, height.dp)

/* Loading from file with java.io API */



@Composable
private fun MappedText.Builder.appendImage(
    image: Node,
    imageState: ImageState,
    onStateChange: (newState: ImageState) -> Unit
) {
    if (visualLength == 0) {
        append(
            MappedText(
                text = ZERO_WIDTH_SPACE,
                textMapping = BoundedBlockTextMapping(
                    coveredSourceRange = TextRange(image.startOffset, image.endOffset),
                    visualTextRange = TextRange(0, 1)
                )
            )
        ) // So we don't have an empty paragraph.
    }
    // Consider the image size to be in DP so images still occupy the same space visually in the document,
    // at the expense of potentially reduced quality.
    val deviceImageSize = imageState.imagePixelSize.dp
    // Placeholder is sized relative to the text.
    val placeholder = with(LocalDensity.current) {
        Placeholder(
            deviceImageSize.width.toSp(),
            deviceImageSize.height.toSp(),
            PlaceholderVerticalAlign.AboveBaseline
        )
    }

    val imageId = remember { imageCount.getAndIncrement() }
    appendInlineContent(
        BoundedBlockTextMapping(
            coveredSourceRange = TextRange(image.startOffset, image.endOffset),
            visualTextRange = TextRange(visualLength, visualLength + 1)
        ),
        IMAGE_INLINE_ELEMENT_TYPE + imageId
    ) {
        InlineTextContent(placeholder) {
            UiImage(imageState, onStateChange)
        }
    }
}
