package me.okonecny.markdowneditor.inline

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import me.okonecny.interactivetext.ConstantTextMapping
import me.okonecny.markdowneditor.LocalMarkdownEditorComponent
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ZERO_WIDTH_SPACE
import java.util.concurrent.atomic.AtomicLong

private const val IMAGE_INLINE_ELEMENT_TYPE = "me.okonecny.markdowneditor.inline.Image"

private val imageCount = AtomicLong(0L) // TODO: store this in the mapped text builder?

@Composable
internal fun MappedText.Builder.appendImage(
    image: Image,
    imageState: ImageState,
    onStateChange: (newState: ImageState) -> Unit
) {
    append(ZERO_WIDTH_SPACE) // So we don't have an empty paragraph.
    // TODO: load image
    // TODO: image cache so we don't load the image multiple times

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

    appendInlineContent(
        image.altText(visualLength),
        IMAGE_INLINE_ELEMENT_TYPE + remember { imageCount.getAndIncrement() }
    ) {
        InlineTextContent(placeholder) {
            UiImage(imageState, onStateChange)
        }
    }
}

data class ImageState(
    val url: String,
    val painter: Painter,
    val title: MappedText = MappedText.empty,
    val loaded: Boolean = false
) {
    val imagePixelSize: Size = painter.intrinsicSize
}

@Composable
private fun UiImage(
    imageState: ImageState,
    onStateChange: (newState: ImageState) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!imageState.loaded) {
        val editorComponent = LocalMarkdownEditorComponent.current
        val failedImage = painterResource("/image-failed.svg")
        LaunchedEffect(Unit) {
            // TODO: load local images from disk if the URL does not start with "http://" or "https://".
            onStateChange(
                imageState.copy(
                    painter = try {
                        editorComponent.imageLoader.load(imageState.url)
                    } catch (e: RuntimeException) {
                        Logger.e(e) { "Failed to load image." }
                        failedImage
                    },
                    loaded = true
                )
            )
        }
    }

    androidx.compose.foundation.Image(
        painter = imageState.painter,
        contentDescription = if (imageState.loaded) "TODO: i18n" else imageState.title.text.text,
        modifier = modifier.size(imageState.imagePixelSize.dp),
        contentScale = ContentScale.FillBounds
    )
    // TODO: render image title
}

private fun Image.altText(visualOffset: Int): MappedText = MappedText(
    text = " ",
    textMapping = ConstantTextMapping(
        coveredSourceRange = TextRange(startOffset, endOffset),
        visualTextRange = TextRange(visualOffset, visualOffset + 1)
    )
)

private val Size.dp: DpSize get() = DpSize(width.dp, height.dp)

/* Loading from file with java.io API */
