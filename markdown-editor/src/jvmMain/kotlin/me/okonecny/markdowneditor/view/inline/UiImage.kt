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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ZERO_WIDTH_SPACE
import me.okonecny.markdowneditor.ast.data.Image
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.internal.ImageLoader
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong

internal class UiImage : InlineRenderer<Image, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<Image, FlexmarkDocument>): MappedText =
        buildMappedString {
            val basePath = document.basePath
            val imageData = inlineNode.data
            var imageState by rememberImageState(
                loader = document.imageLoader,
                url = imageData.url,
                title = imageData.title ?: "",
                basePath = basePath,
                unloadedImage = document.imageLoader.unloadedImage(
                    imageData.url,
                    basePath
                ) ?: painterResource("/image-load.svg")
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
    val loader: ImageLoader,
    val url: String,
    val painter: Painter,
    val title: String = "",
    val loaded: Boolean = false,
    val basePath: Path
) {
    val imagePixelSize: Size = painter.intrinsicSize
}

@Composable
private fun rememberImageState(
    loader: ImageLoader,
    url: String,
    title: String,
    basePath: Path,
    unloadedImage: Painter
): MutableState<ImageState> {
    return rememberSaveable(url, title) {
        mutableStateOf(
            ImageState(
                loader = loader,
                url = url,
                painter = unloadedImage,
                title = title,
                basePath = basePath
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
        val failedImage = painterResource("/image-failed.svg")
        LaunchedEffect(Unit) {
            onStateChange(
                imageState.copy(
                    painter = try {
                        imageState.loader.load(imageState.url, imageState.basePath)
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
    image: VisualNode<Image, FlexmarkDocument>,
    imageState: ImageState,
    onStateChange: (newState: ImageState) -> Unit
) {
    if (visualLength == 0) {
        append(
            MappedText(
                text = ZERO_WIDTH_SPACE
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
        IMAGE_INLINE_ELEMENT_TYPE + imageId
    ) {
        InlineTextContent(placeholder) {
            UiImage(imageState, onStateChange)
        }
    }
}
