package me.okonecny.markdowneditor.inline

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
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.markdowneditor.*
import java.util.concurrent.atomic.AtomicLong

private const val IMAGE_INLINE_ELEMENT_TYPE = "me.okonecny.markdowneditor.inline.Image"

private val imageCount = AtomicLong(0L) // TODO: store this in the mapped text builder?

@Composable
internal fun MappedText.Builder.appendImage(
    image: Image,
    imageState: ImageState,
    onStateChange: (newState: ImageState) -> Unit
) {
    if (visualLength == 0) {
        append(
            MappedText(
                text = ZERO_WIDTH_SPACE,
                textMapping = BoundedBlockTextMapping(
                    coveredSourceRange = TextRange(image.startOffset, image.endOffset),
                    visualTextRange = TextRange(visualLength, visualLength + 1)
                )
            )
        ) // So we don't have an empty paragraph.
    }
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

data class ImageState(
    val url: String,
    val painter: Painter,
    val title: String = "",
    val loaded: Boolean = false
) {
    val imagePixelSize: Size = painter.intrinsicSize
}

@Composable
fun rememberImageState(
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

